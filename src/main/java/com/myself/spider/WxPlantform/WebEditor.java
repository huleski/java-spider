package com.myself.spider.WxPlantform;

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
import org.junit.Test;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Holeski
 * @Date: 2019/8/1 09:34
 * @Description:
 */
@Component
public class WebEditor {
    private final static Logger logger = LoggerFactory.getLogger(WebEditor.class);
    private static OkHttpClient okHttpClient = null;
    public static final MediaType FORM_CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final Set<Cookie> cookieRepo = new HashSet<>();
    private static boolean flag = true;
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
//    private String date = "2019-07-22";

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(1, TimeUnit.MINUTES);
        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)));
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                if (cookies != null && cookies.size() > 0) {
                    cookieRepo.addAll(cookies);
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                /*if (flag) {
                    Cookie cookie1 = Cookie.parse(url,"UM_distinctid=16c24267b871d9-0f7185f9ce85e7-c343162-1fa400-16c24267b88bb7; path=/; domain=.96weixin.com; Expires=Tue, 19 Jan 2038 03:14:07 GMT;");
                    Cookie cookie2 = Cookie.parse(url,"CNZZDATA1273278930=667572361-1563970976-%7C1564663759; path=/; domain=.96weixin.com; Expires=Tue, 19 Jan 2038 03:14:07 GMT;");
                    cookieRepo.add(cookie1);
                    cookieRepo.add(cookie2);
                    flag = false;
                }*/
                return new ArrayList<>(cookieRepo);
            }
        });
        okHttpClient = builder.build();
    }

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

    @Value("${path.pic}")
    private String filePath;

    @Value("${netmask}")
    private String netMask;

    @Autowired
    private Configuration configuration;

    @Test
    public void begin() {
        String imgpath = "E:/Document/picture/avatar.jpg";
        login();
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
                        String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                                " •「" + picture.getIllustId() + "」" + "." + getExtension(url);
                        File parentPath = new File(filePath + date);
                        if (!parentPath.exists()) {
                            parentPath.mkdirs();
                        }

                        File file = new File(parentPath, pictureName);
                        picture.setFile(file);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        logger.info("图片【" + ++PicVariable.original_count + "】下载成功...");

                        if (PicVariable.original_count > PicVariable.pictures.size()) {
                            // 下载完成
                            writeArticle();
                        }
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

    private void writeArticle() {
        login();
        uploadImage();
        try {
            saveArticle();
        } catch (Exception e) {
            logger.error("文章保存出错", e);
        }
        // 同步 TODO
    }

    /**
     * 文件同步下载
     */
    public void downloadPictureSyn(List<Picture> pics) {
        for (int count = 0; count < pics.size(); count++) {
            Picture picture = pics.get(count);
            String url = picture.getOriginalImg();
            //构建request对象
            Request request = new Request.Builder().url(url).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("file(" + url + ") request is not OK---------", response);
                    return;
                }

                FileOutputStream fileOutputStream = null;
                try {
                    InputStream inputStream = response.body().byteStream();
                    String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                            " •「" + picture.getIllustId() + "」" + "." + getExtension(url);
                    File parentPath = new File(filePath + date + File.separator + "originalImg");
                    if (!parentPath.exists()) {
                        parentPath.mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(new File(parentPath, pictureName));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    logger.info("图片【" + (count + 1) + "】下载成功...");

                } catch (Exception e) {
                    logger.error("file(" + url + ") download failed---------", e);
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
        ;
    }

    /**
     * generate file
     *
     * @param pics
     * @throws Exception
     */
    public void generateFile(List<Picture> pics) throws Exception {
        pics.forEach(e -> {
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

    /**
     * 登录
     */
    public void login() {
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
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            logger.info((String) jsonObject.get("info"));
        } catch (Exception e) {
            logger.error("登录请求失败", e);
        }
    }

    /**
     * 上传图片
     *
     * @return 新图片的路径
     * @throws Exception
     */
    public void uploadImage() {
        for (int i = 0; i < PicVariable.pictures.size(); i++) {
            Picture picture = PicVariable.pictures.get(i);
            File file = picture.getFile();
            generateThumbnail(file);
            RequestBody image = RequestBody.create(MediaType.parse("image/*"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upfile", file.getAbsolutePath(), image)
                    .addFormDataPart("name", file.getName())
                    .addFormDataPart("size", String.valueOf(file.length()))
                    .addFormDataPart("id", "WU_FILE_"+ i)
                    .addFormDataPart("type", "image/png")
                    .addFormDataPart("lastModifiedDate", new Date().toString())
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("图片上传失败", response);
                }
                JSONObject jsonObject = JSON.parseObject(response.body().string());
                picture.setUploadImg((String) jsonObject.get("url"));
                logger.info("图片【" + file.getName() + "】上传异常");
            } catch (IOException e) {
                logger.error("图片【" + file.getName() + "】上传异常", e);
            }
        }
    }

    /**
     * 保存
     */
    public void saveArticle() throws Exception {
        PicVariable.pictures.forEach(picture -> {
            picture.setUserAvator(netMask + picture.getUserAvator());
        });
        Map map = new HashMap<>();
        map.put("pics", PicVariable.pictures);
        Template template = configuration.getTemplate("wx.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        HashMap params = new HashMap<>();
        params.put("cate_id", "0");
        params.put("id", "6092731");
        params.put("name", "【每日精选】2019-07-25 精选图集");
        params.put("summary", "精选图集");
        params.put("thumbnail", "http://bj96weixin-1252078571.file.myqcloud.com/ueditor/20190718/156341003510137683337349.jpg");
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
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            logger.info(jsonObject.toJSONString());
        } catch (Exception e) {
            logger.error("文章保存请求异常", e);
        }
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

    private static void generateThumbnail(File file) {
        while (file.length() > 2 * 1024 * 1024) {
            try {
                Thumbnails.of(file)
                        // 图片缩放率，不能和size()一起使用
                        .scale(0.8d)
                        // 图片压缩质量
                        .outputQuality(0.5d)
                        // 缩略图保存目录,该目录需存在，否则报错
                        .toFile(file);
            } catch (Exception e) {
                logger.error("图片【" + file.getName() + "】压缩失败", e);
            }
        }
    }
}
