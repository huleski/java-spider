package com.myself.spider.plantform;

import lombok.Data;

import javax.persistence.*;

/**
 * @Auther: Holeski
 * @Date: 2020/4/18 09:08
 * @Description:
 */
@Entity
@Data
public class WantedPicture extends Base {
    @Id
    @GeneratedValue(generator = "seq_wanted")
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
    private String pixImg;
    private String createDate;
    private String rankDate;
    private String searchKey;

}
