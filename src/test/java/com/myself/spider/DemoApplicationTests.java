package com.myself.spider;

import com.myself.spider.plantform.Editor;
import com.myself.spider.plantform.PicVariable;
import com.myself.spider.plantform.PictureVo;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DemoApplicationTests {
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private static OkHttpClient okHttpClient = null;
    private static String cookieStr = null;

    @Autowired
    private Editor editor;

//    @Before
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
        List<PictureVo> pics = new ArrayList<>(2);
        PictureVo p1 = new PictureVo("1", "holeski", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "http://img.96weixin.com/ueditor/20190920/1568948364754606.jpg");
        PictureVo p2 = new PictureVo("2", "holeski", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "http://img.96weixin.com/ueditor/20190920/1568948365556753.jpg");
        pics.add(p1);
        pics.add(p2);
        PicVariable.voList = pics;
        editor.login();
        editor.saveArticle();
        editor.transferArticle();
    }
}
