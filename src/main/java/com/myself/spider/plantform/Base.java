package com.myself.spider.plantform;

import lombok.Data;

import java.util.Objects;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:08
 * @Description:
 */
@Data
public class Base {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base base = (Base) o;
        return Objects.equals(illustId, base.illustId) &&
                Objects.equals(sort, base.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(illustId, sort);
    }
}
