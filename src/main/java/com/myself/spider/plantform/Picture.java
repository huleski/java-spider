package com.myself.spider.plantform;

import lombok.Data;

import javax.persistence.*;
import java.io.File;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:08
 * @Description:
 */
@Entity
@Table
@Data
public class Picture {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer userId;
    private String illustId;
    private Integer sort;
    private String user;
    private String userAvatar;
    private String title;
    private String caption;
    private String tags;
    private String originalImg;
    private String fixedImg;
    private String uploadImg;
    private String createDate;
    private String rankDate;

    @Transient
    private File avatarFile;

    public Picture() {
    }

    public Picture(String illustId, String user, String userAvatar, String uploadImg) {
        this.illustId = illustId;
        this.user = user;
        this.userAvatar = userAvatar;
        this.uploadImg = uploadImg;
    }

    public Picture(Integer userId, String illustId, Integer sort, String user, String userAvatar, String title, String originalImg, String fixedImg, String createDate) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvatar = userAvatar;
        this.title = title;
        this.originalImg = originalImg;
        this.fixedImg = fixedImg;
        this.createDate = createDate;
    }

    public Picture(Integer userId, String illustId, Integer sort, String user, String userAvatar, String title, String caption, String tags, String originalImg, String fixedImg, String createDate) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvatar = userAvatar;
        this.title = title;
        this.caption = caption;
        this.tags = tags;
        this.originalImg = originalImg;
        this.fixedImg = fixedImg;
        this.createDate = createDate;
    }

}
