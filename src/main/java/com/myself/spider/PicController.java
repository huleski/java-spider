package com.myself.spider;

import freemarker.template.Configuration;
import freemarker.template.Template;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
public class PicController {
    private static final Logger logger = LoggerFactory.getLogger(PicController.class);
    private final static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(30, TimeUnit.SECONDS).build();
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    @Autowired
    private PictureService pictureService;

    @Autowired
    private Configuration configuration;

    @Value("${path.pic}")
    private String filePath;

    @Value("${netmask}")
    private String netMask;

    @RequestMapping(value = "/pic/add")
    @ResponseBody
    public Object save(@RequestBody List<Picture> pics) throws Exception {
//        pictureService.insert(picture);
        // 排序
        Collections.sort(pics, (o1, o2) -> {
            return o1.getUserAvator().compareTo(o2.getUserAvator());
        });
        generateFile(pics);
        downloadPicture(pics);
        return "OK";
    }

    /**
     * generate file
     * @param pics
     * @throws Exception
     */
    private void generateFile(List<Picture> pics) throws Exception {
        pics.forEach(e-> {
            e.setUserAvator(netMask + e.getUserAvator());
        });
        Map map = new HashMap<>();
        map.put("pics", pics);
        Template template = configuration.getTemplate("wx.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        File path = new File(filePath + date);
        if (!path.exists()) {
            path.mkdirs();
        }
        FileUtils.writeStringToFile(new File(path, date + ".html"), content);
        logger.info("生成文件成功");
    }

    @RequestMapping("/test")
    @ResponseBody
    public String testFreemarker(ModelMap modelMap){
        modelMap.addAttribute("name", "Hello dalaoyang , this is freemarker");
        return "wx";
    }

    /**
     * 文件下载
     */
    public void downloadPicture(List<Picture> pics) {
        pics.forEach(picture -> {
            String url = picture.getOriginalImg();
            //构建request对象
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        InputStream inputStream = response.body().byteStream();
                        String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                                " •「" + picture.getIllustId() + "」" + url.substring(url.lastIndexOf("."));
                        fileOutputStream = new FileOutputStream(new File(filePath + date, pictureName));
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        logger.info("文件下载成功...");
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

    @Test
    public void test(){
        String substring = "asel.jpg".substring("asel.jpg".lastIndexOf("."));
        System.out.println(substring);
    }
}

