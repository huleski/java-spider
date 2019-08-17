package com.myself.spider.plantform;

import freemarker.template.Configuration;
import freemarker.template.Template;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
@RequestMapping("/pic")
public class PicController {
    private static final Logger logger = LoggerFactory.getLogger(PicController.class);
    private final static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(1, TimeUnit.MINUTES).build();
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    @Value("${netmask}")
    private String netMask;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private WebEditor webEditor;

    @Autowired
    private Configuration configuration;

    @Value("${path.pic}")
    private String filePath;

    /*@RequestMapping(value = "/generate")
    @ResponseBody
    public Object generateArticle(@RequestBody List<Picture> pics) throws Exception {
        // 排序
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(date);
        });
        pictureService.saveAll(pics);
        generateFile(pics);
        return "OK";
    }*/

    @RequestMapping(value = "/synchronize")
    @ResponseBody
    public Object synchronizeArticle(@RequestBody List<Picture> pics) throws Exception {
        // 排序
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(date);
        });
        pictureService.saveAll(pics);
        PicVariable.pictures = pics;
        webEditor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(@RequestBody List<Picture> pics) throws Exception {
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(date);
        });
        pictureService.saveAll(pics);
        return "OK";
    }

    @RequestMapping("/today")
    @ResponseBody
    public String today() throws Exception {
        List<Picture> pics = pictureService.selectToday(date);
        PicVariable.pictures = pics;
        webEditor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/write")
    @ResponseBody
    public String write(String dateStr) throws Exception {
        List<Picture> pics = pictureService.selectToday(dateStr);
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        });
        PicVariable.pictures = pics;
        webEditor.downloadOriginalImg();
        return "OK";
    }

    /**
     * 文件异步下载
     */
    public void downloadOriginalImg(List<Picture> pics) {
        PicVariable.original_count = 0;
        pics.forEach(picture -> {
            String url = picture.getOriginalImg();
            //构建request对象
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("请求失败", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        InputStream inputStream = response.body().byteStream();
                        String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                                " •「" + picture.getIllustId() + "」" + "." + getExtension(url);
                        File parentPath = new File(filePath + date);
                        if (!parentPath.exists()) {
                            parentPath.mkdirs();
                        }

                        File file = new File(parentPath, pictureName);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        logger.info("图片【" + ++PicVariable.original_count + "】下载成功...");
                    } catch (Exception e) {
                        logger.error("file(" + url + ") download failed---------", e);
                    } finally {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            logger.error("close stream failed", e);
                        }
                    }
                }

            });
        });
    }

    /**
     * generate file
     * @param pics
     * @throws Exception
     */
    private void generateFile(List<Picture> pics) throws Exception {
        pics.forEach(e-> {
            e.setUserAvatar(netMask + e.getUserAvatar());
        });
        Map map = new HashMap<>();
        map.put("pics", pics);
        Template template = configuration.getTemplate("wx2.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        File path = new File(filePath + date);
        if (!path.exists()) {
            path.mkdirs();
        }
        FileUtils.writeStringToFile(new File(path, date + ".html"), content);
        logger.info("生成文件成功");
    }

    /**
     * 获取文件名的后缀
     *
     * @param url 文件url
     * @return 后缀名
     */
    public static final String getExtension(String url) {
        String extension = FilenameUtils.getExtension(url);
        if (StringUtils.isEmpty(extension)) {
            extension = "jpg";
        }
        return extension;
    }
}

