package com.myself.spider.plantform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import net.coobird.thumbnailator.Thumbnails;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Holeski
 * @Date: 2019/8/1 09:34
 * @Description:
 */
@Component
public class WebEditor {
    private final static Logger logger = LoggerFactory.getLogger(WebEditor.class);
    private static OkHttpClient okHttpClient = null;
    public static final MediaType FORM_CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final Set<Cookie> cookieRepo = new HashSet<>();
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(30, TimeUnit.SECONDS);
//        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)));
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                if (cookies != null && cookies.size() > 0) {
                    cookieRepo.addAll(cookies);
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return new ArrayList<>(cookieRepo);
            }
        });
        okHttpClient = builder.build();
    }

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
     * 文件异步下载
     */
    public void downloadOriginalImg() {
        PicVariable.original_count = 0;
        PicVariable.voList.clear();
        logger.info("开始下载图片.................................");
        for (Picture picture : PicVariable.pictures) {
            String url = picture.getOriginalImg();
            File parentPath = new File(filePath + date);
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                    " •「" + picture.getIllustId() + "」" + "." + getExtension(url);
            File file = new File(parentPath, pictureName);
            if (file.exists()) {
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
                downloadSuccess();
                continue;
            }

            //构建request对象
            Request request = new Request.Builder().url(url).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    downloadPictureSyn(picture);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        InputStream inputStream = response.body().byteStream();
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
                    } catch (Exception e) {
                        downloadPictureSyn(picture);
                    } finally {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            logger.error("close stream failed", e);
                        }
                    }
                    downloadSuccess();
                }
            });
        }
    }

    /**
     * 头像下载
     */
    /*public void downloadImg() {
        PicVariable.avatar_count = 0;
        PicVariable.pictures.forEach(picture -> {
            String url = netMask + picture.getUserAvatar();

            File parentPath = new File(filePath + date + File.separator + "avatars");
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            String avatarName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") + "." + getExtension(url);
            File file = new File(parentPath, avatarName);
            if (file.exists()) {
                picture.setAvatarFile(file);
                downloadOriginalImg();
                return;
            }

            //构建request对象
            Request request = new Request.Builder().url(url).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("请求失败", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    FileOutputStream fileOutputStream = null;
                    try {
                        InputStream inputStream = response.body().byteStream();
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        picture.setAvatarFile(file);
                    } catch (Exception e) {
                        downloadPictureSyn(picture);
                    } finally {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            logger.error("close stream failed", e);
                        }
                    }
                    downloadOriginalImg();
                }
            });
        });
    }*/
    private synchronized void downloadSuccess() {
        if (++PicVariable.original_count >= PicVariable.pictures.size()) {
            try {
                logger.info("下载图片完成, Link Start!!!----------------------");
                login();
                uploadImage();
                generateFile();
                saveArticle();
//                transferArticle();
            } catch (Exception e) {
                logger.error("操作失败", e);
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
        HashMap params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", password);
        params.put("remember", "0");

        Request request = new Request.Builder()
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(getRequestBody(params)).url(loginUrl).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("登录失败", response);
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            PicVariable.isLogin = true;
            logger.info("登录成功------------------------------");
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
            generateThumbnail(file);
            RequestBody image = RequestBody.create(MediaType.parse("image/*"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upfile", file.getAbsolutePath(), image)
                    .addFormDataPart("name", file.getName())
                    .addFormDataPart("size", String.valueOf(file.length()))
                    .addFormDataPart("id", "WU_FILE_" + i)
                    .addFormDataPart("type", "image/png")
                    .addFormDataPart("lastModifiedDate", new Date().toString())
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.info("图片【" + file.getName() + "】上传异常");
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject jsonObject = JSON.parseObject(response.body().string());
                pictureVo.setUploadImg(imgPrefix + jsonObject.get("url"));
            }
        }
        logger.info("图片上传完成------------------------------");
    }

    /**
     * 保存
     */
    public void saveArticle() throws Exception {
        Map map = new HashMap<>();
        map.put("pics", PicVariable.voList);
        Template template = configuration.getTemplate("article.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        File path = new File(filePath + date);
        if (!path.exists()) {
            path.mkdirs();
        }
        FileUtils.writeStringToFile(new File(path, date + ".html"), content);

        HashMap params = new HashMap<>();
        params.put("cate_id", "0");
        params.put("id", "6092731");
        params.put("name", "【每日精选】" + getTomorrow() + " 精选图集");
        params.put("summary", "精选图集");
        params.put("thumbnail", "http://img.96weixin.com/ueditor/20190815/156587409910137683325518.jpg");
        params.put("author", "CryCat");
        params.put("artcover", "0");
        params.put("original", "true");
        params.put("content", content);

        Request request = new Request.Builder()
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(getRequestBody(params)).url(saveUrl).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("文章保存失败", response);
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            logger.info("文章保存完成------------------------------");
        }
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
            HashMap params = new HashMap<>();
            params.put("art_id", "6092731");
            params.put("wechat_id", "179085");
            params.put("type", type);

            Request request = new Request.Builder()
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .post(getRequestBody(params)).url(transferUrl).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    transferArticle();
                }
                String result = response.body().string();
                JSONObject jsonObject = JSON.parseObject(result);
//                logger.info(jsonObject.toJSONString());
            }
        }
        logger.info("文章同步完成------------------------------");
    }

    private static RequestBody getRequestBody(Map<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            //删除最后的一个"&"
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RequestBody.create(FORM_CONTENT_TYPE, stringBuffer.toString());
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
        //构建request对象
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("file(" + url + ") request is not OK---------", response);
                return;
            }

            FileOutputStream fileOutputStream = null;
            String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                    " •「" + picture.getIllustId() + "」" + "." + getExtension(url);
            File parentPath = new File(filePath + date);
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            File file = new File(parentPath, pictureName);
            try {
                InputStream inputStream = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[2048];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                }
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), netMask + picture.getUserAvatar(), file));
            } catch (Exception e) {
                logger.error("图片【" + url + "】download failed---------", e);
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.error("close stream failed", e);
                }
            }
        } catch (IOException e) {
            logger.error("file(" + url + ") request failed---------", e);
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
        logger.info("生成文件成功");
    }

    private static void generateThumbnail(File file) {
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
            logger.error("图片【" + file.getName() + "】压缩失败", e);
        }
    }
}
