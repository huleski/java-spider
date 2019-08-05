package com.myself.spider.WxPlantform;

import okhttp3.OkHttpClient;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
public class PicController {
    private static final Logger logger = LoggerFactory.getLogger(PicController.class);
    private final static OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(1, TimeUnit.MINUTES).build();


    @Autowired
    private PictureService pictureService;

    @Autowired
    private WebEditor webEditor;

    @RequestMapping(value = "/pic/add")
    @ResponseBody
    public Object addPic(@RequestBody List<Picture> pics) throws Exception {
        // 排序
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvator().compareTo(o2.getUserAvator());
        }).forEach(picture -> {
            picture.setCreateDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd"));
        });
        pictureService.saveAll(pics);
        PicVariable.pictures = pics;

        // 异步下载
        webEditor.downloadOriginalImg(pics);
        webEditor.generateFile(pics);
        return "OK";
    }




    @RequestMapping("/test")
    @ResponseBody
    public String testFreemarker(ModelMap modelMap){
        modelMap.addAttribute("name", "Hello dalaoyang , this is freemarker");
        return "wx";
    }
}

