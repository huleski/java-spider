package com.myself.spider.plantform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Holeski
 * @Date: 2019/8/1 09:34
 * @Description:
 */
//@Component
@Slf4j
public class OKHttpEditor extends Editor {
    private static OkHttpClient okHttpClient = null;
    public static final MediaType FORM_CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final Set<Cookie> cookieRepo = new HashSet<>();

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

    /**
     * 图片异步下载
     */
    @Override
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
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), mask + picture.getUserAvatar(), file));
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

                    try {
                        FileCopyUtils.copy(response.body().bytes(), file);
                        PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), mask + picture.getUserAvatar(), file));
                    } catch (Exception e) {
                        downloadPictureSyn(picture);
                    }
                    downloadSuccess();
                }
            });
        }
    }

    /**
     * 登录
     */
    @Override
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
                log.error("登录失败", response);
                throw new IOException("Unexpected code " + response);
            }
            PicVariable.isLogin = true;
            log.info("登录成功------------------------------");
        }
    }

    /**
     * 上传图片
     *
     * @return 新图片的路径
     * @throws Exception
     */
    @Override
    public void uploadImage() throws Exception {
        for (int i = 0; i < PicVariable.voList.size(); i++) {
            PictureVo pictureVo = PicVariable.voList.get(i);
            File file = pictureVo.getFile();
            // 压缩图片
            thumbnailImage(file);
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
                    log.info("图片【" + file.getName() + "】上传异常");
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject jsonObject = JSON.parseObject(response.body().string());
                pictureVo.setUploadImg(imgPrefix + jsonObject.get("url"));
            }
        }
        log.info("图片上传完成------------------------------");
    }

    /**
     * 保存
     */
    @Override
    public void saveArticle() throws Exception {
        Map map = new HashMap<>();
        map.put("pics", PicVariable.voList);
        Template template = configuration.getTemplate("article.ftl");
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        // 表单提交
        RequestBody formBody = new FormBody.Builder()
                .add("cate_id", "0")
                .add("id", "6092731")
                .add("name", "【每日精选】" + getTomorrow() + " 精选图集")
                .add("summary", "精选图集")
                .add("thumbnail", "http://img.96weixin.com/ueditor/20190815/156587409910137683325518.jpg")
                .add("author", "CryCat")
                .add("artcover", "0")
                .add("original", "true")
                .add("content", content)
                .build();

        Request request = new Request.Builder()
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(formBody).url(saveUrl).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("文章保存失败", response);
                throw new IOException("Unexpected code " + response);
            }
            String result = response.body().string();
            JSONObject jsonObject = JSON.parseObject(result);
            log.info("文章保存完成------------------------------");
        }
    }

    /**
     * 同步
     */
    @Override
    public void transferArticle() throws Exception {
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
            }
        }
        log.info("文章同步完成------------------------------");
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
     * 文件同步下载
     */
    @Override
    public void downloadPictureSyn(Picture picture) {
        String url = picture.getFixedImg();
        //构建request对象
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("file(" + url + ") request is not OK---------", response);
                return;
            }

            String pictureName = picture.getUser().replaceAll("[//\\\\:*?\"<>|]", "") +
                    " •「" + picture.getIllustId() + "(" + picture.getSort() + ")」" + "." + getExtension(url);
            File parentPath = new File(filePath + date);
            if (!parentPath.exists()) {
                parentPath.mkdirs();
            }
            File file = new File(parentPath, pictureName);
            try {
                FileCopyUtils.copy(response.body().bytes(), file);
                PicVariable.voList.add(new PictureVo(picture.getIllustId(), picture.getUser(), mask + picture.getUserAvatar(), file));
            } catch (Exception e) {
                log.error("图片【" + url + "】download failed---------", e);
            }
        } catch (IOException e) {
            log.error("file(" + url + ") request failed---------", e);
        }
    }

}
