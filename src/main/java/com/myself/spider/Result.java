package com.myself.spider;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/6/21 09:08
 * @Description:
 */
public class Result {
    String message;
    Data data;

    public Result() {
    }

    @Override
    public String toString() {
        return "Result{" +
                "message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

class Data {
    List<Illustrations> illustrations;

    @Override
    public String toString() {
        return "Data{" +
                "illustrations=" + illustrations +
                '}';
    }
}

class Illustrations{
    int id;
    int illust_id;
    String title;
    String type;
    String caption;
    User user;
    List<Tag> tags;
    List<String> tools;
    String create_date;
    int page_count;
    int rank;
    String dateOfThisRank;
    int sanity_level;
    SinglePage meta_single_page;
    List<Page> meta_pages;

    @Override
    public String toString() {
        return "Illustrations{" +
                "id=" + id +
                ", illust_id=" + illust_id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", caption='" + caption + '\'' +
                ", user=" + user +
                ", tags=" + tags +
                ", tools=" + tools +
                ", create_date='" + create_date + '\'' +
                ", page_count=" + page_count +
                ", rank=" + rank +
                ", dateOfThisRank='" + dateOfThisRank + '\'' +
                ", sanity_level=" + sanity_level +
                ", meta_single_page=" + meta_single_page +
                ", meta_pages=" + meta_pages +
                '}';
    }
}

class User {
    int id;
    String name;
    String account;
    Avator profile_image_urls;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", account='" + account + '\'' +
                ", profile_image_urls=" + profile_image_urls +
                '}';
    }
}

class Avator {
    String medium;

    @Override
    public String toString() {
        return "Avator{" +
                "medium='" + medium + '\'' +
                '}';
    }
}

class Tag {
    String name;

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                '}';
    }
}

class SinglePage {
    String original_image_url;
    String large_image_url;

    @Override
    public String toString() {
        return "SinglePage{" +
                "original_image_url='" + original_image_url + '\'' +
                ", large_image_url='" + large_image_url + '\'' +
                '}';
    }
}

class Page {
    ImgUrl image_urls;

    @Override
    public String toString() {
        return "Page{" +
                "image_urls=" + image_urls +
                '}';
    }
}

class ImgUrl {
    String original;
    String large;

    @Override
    public String toString() {
        return "ImgUrl{" +
                "original='" + original + '\'' +
                ", large='" + large + '\'' +
                '}';
    }
}
