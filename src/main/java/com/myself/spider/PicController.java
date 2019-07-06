package com.myself.spider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/7/5 08:58
 * @Description:
 */
@RestController
public class PicController {
    @Autowired
    private PictureService pictureService;

    @RequestMapping(value = "/pic/add")
    public Object save(@RequestBody List<Picture> pics){
//        pictureService.insert(picture);
        System.out.println(pics);
        return 0;
    }
}
