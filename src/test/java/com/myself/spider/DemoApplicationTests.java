package com.myself.spider;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//@SpringBootTest
public class DemoApplicationTests {
    private final static Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private static OkHttpClient okHttpClient = null;
    private static String loginUrl = "http://bj.96weixin.com/login/phone";
    private static String cookieStr = null;

    @Before
    public void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)));
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        okHttpClient = builder.build();
    }

    @Test
    public void contextLoads() throws Exception {
        login(loginUrl);

    }

    public void login(String url) throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("phone", "18620342044")
                .add("password", "editorHl123")
                .add("remember", "1")
                .build();

        Request request = new Request.Builder().post(formBody).url(url) .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                logger.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }
            String result = response.body().string();
            logger.info(result);
        }
    }

    public void saveArticle(String content) throws Exception {
        String saveUrl = "http://bj.96weixin.com/indexajax/saveart";
        FormBody formBody = new FormBody.Builder()
                .add("cate_id", "0")
                .add("id", "6092731")
                .add("name", "【每日精选】2019-07-22 精选图集")
                .add("summary", "精选图集")
                .add("thumbnail", "http://bj96weixin-1252078571.file.myqcloud.com/ueditor/20190720/156363356110137683329197.jpg")
                .add("author", "CryCat")
                .add("artcover", "0")
                .add("original", "true")
                .add("content", content)
                .build();

        Request request = new Request.Builder().post(formBody).url(saveUrl) .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("Unexpected code " + response);
            String result = response.body().string();
            logger.info(result);
        }
    }








    /**
     * @param cookies cookie list
     * @return cookies string
     */
    public String encodeCookie(List<Cookie> cookies) {
        if (cookies == null || cookies.size() == 0)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookies);
        } catch (IOException e) {
            logger.error("IOException in encodeCookie", e);
            return null;
        }
        byte[] bytes = os.toByteArray();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * 将字符串反序列化成cookies
     *
     * @param cookieString cookies string
     * @return cookie object
     */
    public List<Cookie> decodeCookie(String cookieString) {
        if (StringUtils.isBlank(cookieString)) {
            return null;
        }
        int len = cookieString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(cookieString.charAt(i), 16) << 4) + Character.digit(cookieString.charAt(i + 1), 16));
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return ((List<Cookie>) objectInputStream.readObject());
        } catch (Exception e) {
            logger.error("IOException in decodeCookie", e);
            return null;
        }
    }

    public void a() {
        Request request = new Request.Builder().url("https://www.zhihu.com/#signin").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Document parse = Jsoup.parse(resp);
                Elements select = parse.select("input[type=hidden]");
                Element element = select.get(0);
                String xsrf = element.attr("value");
            }
        });
    }
}
