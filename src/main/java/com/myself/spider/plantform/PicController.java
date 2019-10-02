package com.myself.spider.plantform;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
@RequestMapping("/pic")
public class PicController {
    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    @Autowired
    private PictureService pictureService;

    @Autowired
    private Editor editor;

    @RequestMapping(value = "/synchronize")
    @ResponseBody
    public Object synchronizeArticle(@RequestBody List<Picture> pics) throws Exception {
        // 排序
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(date);
        });
        List<Picture> pictures = pictureService.saveAll(pics);
        PicVariable.pictures = pictures;
        editor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(@RequestBody List<Picture> pics) throws Exception {
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(date);
        });
        pictureService.saveAll(pics);
        return "OK";
    }

    @RequestMapping("/today")
    @ResponseBody
    public String today(String dateStr) throws Exception {
        if (StringUtils.isEmpty(dateStr)) {
            dateStr = date;
        }
        List<Picture> pics = pictureService.findAllByCreateDate(dateStr);
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        });
        PicVariable.pictures = pics;
        editor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test() throws Exception {
        List<PictureVo> pics = new ArrayList<>(2);
        PictureVo p1 = new PictureVo("1", "holeski", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "http://img.96weixin.com/ueditor/20190920/1568948364754606.jpg");
        PictureVo p2 = new PictureVo("2", "holeski", "http://img.96weixin.com/ueditor/20190920/1568962918667079.jpg", "http://img.96weixin.com/ueditor/20190920/1568948365556753.jpg");
        pics.add(p1);
        pics.add(p2);
        PicVariable.voList = pics;
        editor.login();
        editor.saveArticle();
        return "OK";
    }

}

