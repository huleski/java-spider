package com.myself.spider.plantform;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@Slf4j
@Controller
@RequestMapping("/pic")
public class PicController {

    @Autowired
    private PictureService pictureService;
    @Autowired
    private WantedPictureService wantedPictureService;

    @Autowired
    private Editor editor;

    @RequestMapping(value = "/synchronize")
    @ResponseBody
    public Object synchronizeArticle(@RequestBody List<Base> pics) {
        PicVariable.pictures = pictureService.saveAllUnsaved(savePics(pics));
        editor.downloadOriginalImg();
        return "OK";
    }

    @RequestMapping("/save")
    @ResponseBody
    public String save(@RequestBody List<Base> pics) {
        savePics(pics);
        return "OK";
    }

    @RequestMapping("/today")
    @ResponseBody
    public String today() {
        if (editor.type == Type.PICTURE) {
            PicVariable.pictures = pictureService.findAllByCreateDate(editor.date);
        } else {
            PicVariable.pictures = wantedPictureService.findAllBySearchKey(editor.date);
        }

        editor.downloadOriginalImg();
        return "OK";
    }

    private List savePics(List<Base> collect) {
        if (editor.type == Type.PICTURE) {
            collect.forEach(picture -> picture.setCreateDate(editor.date));
            List<Picture> list = collect.stream().sorted(Comparator.comparing(Base::getUser)).map(base -> {
                Picture picture = new Picture();
                BeanUtil.copyProperties(base, picture);
                return picture;
            }).collect(Collectors.toList());
            return pictureService.saveAllUnsaved(list);

        } else {
            collect.forEach(picture -> {
                picture.setCreateDate(DateUtil.formatDate(new Date()));
                picture.setSearchKey(editor.date);
            });
            List<WantedPicture> list = collect.stream().distinct().sorted(Comparator.comparing(Base::getUser)).map(base -> {
                WantedPicture picture = new WantedPicture();
                BeanUtil.copyProperties(base, picture);
                return picture;
            }).collect(Collectors.toList());
            return wantedPictureService.saveAllUnsaved(list);
        }
    }

//    @Scheduled(cron = "0 0 3 * * ? ")
    public void process() throws Exception {
        editor.date = "2020-05-08";
        today();
        Thread.sleep(1000 * 60 * 6);


        System.exit(-1);
    }
}

