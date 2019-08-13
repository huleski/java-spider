package com.myself.spider.WxPlantform;

import lombok.Data;

import java.io.File;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:08
 * @Description:
 */
@Data
public class PictureVo {
    private Integer illustId;
    private String user;
    private String userAvator;
    private File file;
    private String uploadImg;

    public PictureVo() {
    }

    public PictureVo(Integer illustId, String user, String userAvator, File file) {
        this.illustId = illustId;
        this.user = user;
        this.userAvator = userAvator;
        this.file = file;
    }
}
