package com.myself.spider.WxPlantform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
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



    @Test
    public void begin() {
        String imgpath = "E:/Document/picture/avatar.jpg";
        login();
        String s = uploadImage(imgpath);
        logger.info(s);
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
     * 保存
     */
    public void saveArticle() {
        HashMap params = new HashMap<>();
        params.put("cate_id", "0");
        params.put("id", "6092731");
        params.put("name", "【每日精选】2019-07-25 精选图集");
        params.put("summary", "精选图集");
        params.put("thumbnail", "http://bj96weixin-1252078571.file.myqcloud.com/ueditor/20190718/156341003510137683337349.jpg");
        params.put("author", "CryCat");
        params.put("artcover", "0");
        params.put("original", "true");
        params.put("content", "cc");

        Request request = new Request.Builder()
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(getRequestBody(params)).url(saveUrl).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("保存失败", response);
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            logger.info(jsonObject.toJSONString());
        } catch (Exception e) {
            logger.error("保存请求异常", e);
        }
    }

    /**
     * 上传图片
     *
     * @param imagePath 图片路径
     * @return 新图片的路径
     * @throws Exception
     */
    public String uploadImage(String imagePath) {
        File file = new File(imagePath);
        RequestBody image = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upfile", imagePath, image)
                .addFormDataPart("name", "avatar.jpg")
                .addFormDataPart("size", "267264")
                .addFormDataPart("id", "WU_FILE_0")
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
            return (String) jsonObject.get("url");
        } catch (IOException e) {
            logger.error("图片上传异常", e);
        }
        return null;
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
}
