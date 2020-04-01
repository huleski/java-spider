package com.myself.spider;

import com.myself.spider.plantform.Editor;
import com.myself.spider.plantform.PicVariable;
import com.myself.spider.plantform.PictureService;
import com.myself.spider.plantform.PictureVo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//@SpringBootTest
//@RunWith(SpringRunner.class)
@Slf4j
public class DemoApplicationTests {
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private static OkHttpClient okHttpClient = null;
    private static String cookieStr = null;

    @Autowired
    private Editor editor;

    @Autowired
    private PictureService pictureService;

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
    public void today() {
        PicVariable.pictures = pictureService.findAllByCreateDate(editor.date);
        editor.downloadOriginalImg();
    }

    @Test
    public void contextLoads() {
        List<PictureVo> pics = new ArrayList<>(2);
        PictureVo p1 = new PictureVo("75438543", "sofia", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "https://img.96weixin.com/ueditor/20200329/1585445095715927.jpg");
        PictureVo p2 = new PictureVo("75456736", "lin", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "https://img.96weixin.com/ueditor/20200329/1585445090941980.png");
        pics.add(p1);
        pics.add(p2);

//        PicVariable.voList = pics;
//        editor.generateFile();
    }

    @Test
    public void testCompress() throws Exception {
        editor.uploadZipPackage();
    }

}
