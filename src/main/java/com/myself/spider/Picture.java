package com.myself.spider;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:08
 * @Description:
 */
public class Picture {
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
    private String laterImg;
    private String createTime;

    public Picture() {
    }

    public Picture(Integer userId, Integer illustId, Integer sort, String user, String userAvator, String title, String originalImg, String laterImg, String createTime) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvator = userAvator;
        this.title = title;
        this.originalImg = originalImg;
        this.laterImg = laterImg;
        this.createTime = createTime;
    }

    public Picture(Integer userId, Integer illustId, Integer sort, String user, String userAvator, String title, String caption, String tags, String originalImg, String laterImg, String createTime) {
        this.userId = userId;
        this.illustId = illustId;
        this.sort = sort;
        this.user = user;
        this.userAvator = userAvator;
        this.title = title;
        this.caption = caption;
        this.tags = tags;
        this.originalImg = originalImg;
        this.laterImg = laterImg;
        this.createTime = createTime;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getIllustId() {
        return illustId;
    }

    public void setIllustId(Integer illustId) {
        this.illustId = illustId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserAvator() {
        return userAvator;
    }

    public void setUserAvator(String userAvator) {
        this.userAvator = userAvator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalImg() {
        return originalImg;
    }

    public void setOriginalImg(String originalImg) {
        this.originalImg = originalImg;
    }

    public String getLaterImg() {
        return laterImg;
    }

    public void setLaterImg(String laterImg) {
        this.laterImg = laterImg;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
