package com.myself.spider.plantform;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
@RequestMapping("/pic")
public class PicController {
//    private String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");

    @Autowired
    private PictureService pictureService;

    @Autowired
    private Editor editor;

    private void execute(List<Picture> pictures){
        PicVariable.pictures = pictures;
        editor.downloadOriginalImg();
    }


    @RequestMapping(value = "/synchronize")
    @ResponseBody
    public Object synchronizeArticle(@RequestBody List<Picture> pics) throws Exception {
        // 排序
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(editor.date);
        });
        List<Picture> pictures = pictureService.saveAllUnsaved(pics);
        execute(pictures);
        return "OK";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(@RequestBody List<Picture> pics) throws Exception {
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        }).forEach(picture -> {
            picture.setCreateDate(editor.date);
        });
        pictureService.saveAllUnsaved(pics);
        return "OK";
    }

    @RequestMapping("/today")
    @ResponseBody
    public String today(String createDate) throws Exception {
        if (StringUtils.isEmpty(createDate)) {
            createDate = editor.date;
        }
        List<Picture> pics = pictureService.findAllByCreateDate(createDate);
        pics.stream().sorted((o1, o2) -> {
            return o1.getUserAvatar().compareTo(o2.getUserAvatar());
        });
        execute(pics);
        return "OK";
    }

}

