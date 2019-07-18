package com.myself.spider;

import lombok.Data;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private Integer illustId;
    private Integer sort;
    private String user;
    private String userAvator;
    private String title;
    private String caption;
    private String tags;
    private String originalImg;
    private String fixedImg;
    private String createDate;
    private String rankDate;

    public Picture() {
    }

    public Picture(Integer userId, Integer illustId, Integer sort, String user, String userAvator, String title, String originalImg, String fixedImg, String createDate) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvator = userAvator;
        this.title = title;
        this.originalImg = originalImg;
        this.fixedImg = fixedImg;
        this.createDate = createDate;
    }

    public Picture(Integer userId, Integer illustId, Integer sort, String user, String userAvator, String title, String caption, String tags, String originalImg, String fixedImg, String createDate) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvator = userAvator;
        this.title = title;
        this.caption = caption;
        this.tags = tags;
        this.originalImg = originalImg;
        this.fixedImg = fixedImg;
        this.createDate = createDate;
    }

}
