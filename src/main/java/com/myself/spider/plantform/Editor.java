package com.myself.spider.plantform;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Holeski
 * @date 2019/9/24 20:39
 */
@Slf4j
public abstract class Editor {

    @Value("${path.pic}")
    String filePath;

    @Value("${netmask}")
    String netMask;

    @Value("${platform.phone}")
    String phone;

    @Value("${platform.password}")
    String password;

    @Value("${platform.loginUrl}")
    String loginUrl;

    @Value("${platform.uploadUrl}")
    String uploadUrl;

    @Value("${platform.saveUrl}")
    String saveUrl;

    @Value("${platform.transferUrl}")
    String transferUrl;

    @Value("${platform.imgPrefix}")
    String imgPrefix;

    @Autowired
    Configuration configuration;

    String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    public abstract void login() throws Exception;

    public abstract void downloadOriginalImg();

    public abstract void uploadImage() throws Exception;

    public abstract void saveArticle() throws Exception;

    public abstract void transferArticle() throws Exception;

    public abstract void downloadPictureSyn(Picture picture);

    public abstract void downloadSuccess();

    /**
     * 获取文件名的后缀
     *
     * @param url 文件url
     * @return 后缀名
     */
    public String getExtension(String url) {
        String extension = FilenameUtils.getExtension(url);
        if (StringUtils.isEmpty(extension)) {
            extension = "jpg";
        }
        return extension;
    }

    /**
     * 返回明天日期
     *
     * @return
     */
    public String getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 压缩图片
     * @param file
     */
    void thumbnailImage(File file) {
        try {
            while (file.length() > 2 * 1024 * 1024) {
                Thumbnails.of(file)
                        // 图片缩放率，不能和size()一起使用
                        .scale(0.8d)
                        // 图片压缩质量
                        .outputQuality(0.5d)
                        // 缩略图保存目录,该目录需存在，否则报错
                        .toFile(file);
            }
        } catch (Exception e) {
            log.error("图片【" + file.getName() + "】压缩失败", e);
        }
    }

    /**
     * generate file
     *
     * @throws Exception
     */
    void generateFile() throws Exception {
        Map map = new HashMap<>();
        map.put("pics", PicVariable.voList);
        Template template = configuration.getTemplate("file.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        File path = new File(filePath + date);
        if (!path.exists()) {
            path.mkdirs();
        }
        FileUtils.writeStringToFile(new File(path, date + ".html"), content);
        log.info("生成文件成功---");
    }
}