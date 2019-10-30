package com.myself.spider.plantform;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    private void execute(List<Picture> pictures){
        PicVariable.pictures = pictures;
//         默认为明天的日期
//        editor.articleDate = "2019-10-30";
        editor.articleDate = editor.getTomorrow();
        editor.downloadOriginalImg();
    }


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
        execute(pictures);
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
    public String today(String createDate) throws Exception {
        if (StringUtils.isEmpty(createDate)) {
            createDate = date;
        }
        List<Picture> pics = pictureService.findAllByCreateDate(createDate);
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        });
        execute(pics);
        return "OK";
    }
}

