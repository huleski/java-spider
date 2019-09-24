package com.myself.spider.plantform;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Author: Holeski
 * @Date: 2019/9/23 09:34
 * @Description:
 */
//@Component
@Slf4j
public class RestTemplateEditor {
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    private List<String> cookie;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${path.pic}")
    private String filePath;

    @Value("${netmask}")
    private String netMask;

    @Value("${platform.phone}")
    private String phone;

    @Value("${platform.password}")
    private String password;

    @Value("${platform.loginUrl}")
    private String loginUrl;

    @Value("${platform.uploadUrl}")
    private String uploadUrl;

    @Value("${platform.saveUrl}")
    private String saveUrl;

    @Value("${platform.transferUrl}")
    private String transferUrl;

    @Value("${platform.imgPrefix}")
    private String imgPrefix;

    @Autowired
    private Configuration configuration;

    @Autowired
    private PictureRepository pictureDao;

    /**
     * 图片异步下载
     */
    public void downloadOriginalImg() {
        PicVariable.original_count = 0;
        PicVariable.voList.clear();
        log.info("开始下载图片.................................");
        for (Picture picture : PicVariable.pictures) {
            String url = picture.getOriginalImg();
            File parentPath = new File(filePath + date);
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                    " •「" + picture.getIllustId() + "(" + picture.getSort() + ")」" + "." + getExtension(url);
            File file = new File(parentPath, pictureName);
            if (file.exists()) {
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
                downloadSuccess();
                continue;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Resource> httpEntity = new HttpEntity<Resource>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
            try {
                FileCopyUtils.copy(response.getBody(), file);
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
            } catch (IOException e) {
                downloadPictureSyn(picture);
            }
            downloadSuccess();
        }
    }

    private synchronized void downloadSuccess() {
        if (++PicVariable.original_count >= PicVariable.pictures.size()) {
            try {
                log.info("下载图片完成, Link Start!!!----------------------");
                login();
                uploadImage();
                generateFile();
                saveArticle();
                transferArticle();
            } catch (Exception e) {
                log.error("操作失败", e);
            }
        }
    }

    /**
     * 登录
     */
    public void login() throws Exception {
        if (PicVariable.isLogin) {
            return;
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
        params.put("phone", Collections.singletonList(phone));
        params.put("password", Collections.singletonList(password));
        params.put("remember", Collections.singletonList("0"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "XMLHttpRequest");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, requestEntity, JSONObject.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            cookie = responseEntity.getHeaders().get(HttpHeaders.COOKIE);
            if (ObjectUtils.isNotEmpty(cookie)) {
                log.info("登录成功------------------------------");
            }
        }
    }

    /**
     * 上传图片
     *
     * @return 新图片的路径
     * @throws Exception
     */
    public void uploadImage() throws Exception {
        for (int i = 0; i < PicVariable.voList.size(); i++) {
            PictureVo pictureVo = PicVariable.voList.get(i);
            File file = pictureVo.getFile();
            // 压缩图片
            thumbnailImage(file);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Requested-With", "XMLHttpRequest");
            headers.put(HttpHeaders.COOKIE, cookie);
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("upfile", resource);
            params.add("name", file.getName());
            params.add("size", String.valueOf(file.length()));
            params.add("id", "WU_FILE_" + i);
            params.add("type", "image/png");
            params.add("lastModifiedDate", new Date().toString());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
            ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(uploadUrl, HttpMethod.GET, requestEntity, JSONObject.class, params);
            JSONObject body = responseEntity.getBody();
            pictureVo.setUploadImg(imgPrefix + body.get("url"));
            log.info("图片上传完成------------------------------");
        }
    }

    /**
     * 保存
     */
    public void saveArticle() throws Exception {
        Map map = new HashMap<>();
        map.put("pics", PicVariable.voList);
        Template template = configuration.getTemplate("article.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.put(HttpHeaders.COOKIE, cookie);
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.put("cate_id", Collections.singletonList("0"));
        params.put("id", Collections.singletonList("6092731"));
        params.put("name", Collections.singletonList("【每日精选】" + getTomorrow() + " 精选图集"));
        params.put("summary", Collections.singletonList("精选图集"));
        params.put("thumbnail", Collections.singletonList("http://img.96weixin.com/ueditor/20190815/156587409910137683325518.jpg"));
        params.put("author", Collections.singletonList("CryCat"));
        params.put("artcover", Collections.singletonList("0"));
        params.put("original", Collections.singletonList("true"));
        params.put("content", Collections.singletonList(content));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params, headers);
        restTemplate.postForEntity(saveUrl, httpEntity, String.class);
        log.info("文章保存完成------------------------------");
    }

    /**
     * 同步
     */
    private void transferArticle() throws Exception {
        for (int i = 0; i < 3; i++) {
            String type;
            if (i == 0) {
                type = "thumbnail";
            } else if (i == 1) {
                type = "img";
            } else {
                type = "art";
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Requested-With", "XMLHttpRequest");
            headers.put(HttpHeaders.COOKIE, cookie);
            LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.put("art_id", Collections.singletonList("6092731"));
            params.put("wechat_id", Collections.singletonList("179085"));
            params.put("type", Collections.singletonList(type));
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(params, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(transferUrl, httpEntity, String.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                transferArticle();
            }
        }
        log.info("文章同步完成------------------------------");
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

    /**
     * 文件同步下载
     */
    public void downloadPictureSyn(Picture picture) {
        String url = picture.getFixedImg();

        String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                " •「" + picture.getIllustId() + "(" + picture.getSort() + ")」" + "." + getExtension(url);
        File parentPath = new File(filePath + date);
        if (!parentPath.exists()) {
            parentPath.mkdirs();
        }
        File file = new File(parentPath, pictureName);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Resource> httpEntity = new HttpEntity<Resource>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
        try {
            FileCopyUtils.copy(response.getBody(), file);
            PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
        } catch (IOException e) {
            log.error("file(" + url + ") request is not OK---------", response);
        }
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
     * generate file
     *
     * @throws Exception
     */
    private void generateFile() throws Exception {
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

    /**
     * 压缩图片
     *
     * @param file
     */
    private static void thumbnailImage(File file) {
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
}
