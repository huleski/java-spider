package com.myself.spider.plantform;

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
    private String userAvatar;
    private File file;
    private String uploadImg;

    public PictureVo() {
    }

    public PictureVo(Integer illustId, String user, String userAvatar, File file) {
        this.illustId = illustId;
        this.user = user;
        this.userAvatar = userAvatar;
        this.file = file;
    }
}
