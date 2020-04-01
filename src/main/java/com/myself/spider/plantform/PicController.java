package com.myself.spider.plantform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Controller
@RequestMapping("/pic")
public class PicController {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private Editor editor;

    @RequestMapping(value = "/synchronize")
    @ResponseBody
    public Object synchronizeArticle(@RequestBody List<Picture> pics) {
        // 排序
        List<Picture> collect = pics.stream().sorted(Comparator.comparing(Picture::getUser)).collect(Collectors.toList());
        collect.forEach(picture -> picture.setCreateDate(editor.date));
        PicVariable.pictures = pictureService.saveAllUnsaved(collect);
        editor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(@RequestBody List<Picture> pics) {
        List<Picture> collect = pics.stream().sorted(Comparator.comparing(Picture::getUser)).collect(Collectors.toList());
        collect.forEach(picture -> picture.setCreateDate(editor.date));
        pictureService.saveAllUnsaved(collect);
        return "OK";
    }

    @RequestMapping("/today")
    @ResponseBody
    public String today() {
        PicVariable.pictures = pictureService.findAllByCreateDate(editor.date);
        editor.downloadOriginalImg();
        return "OK";
    }

}

