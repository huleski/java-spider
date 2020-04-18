package com.myself.spider.plantform;

/**
 * @Auther: Holeski
 * @Date: 2020/4/18 17:10
 * @Description:
 */
public enum Type {
    /**
     * 类型
     */
    PICTURE(1),
    WANTED_PICTURE(2),
    ;

    Type(Integer type) {
        this.type = type;
    }

    private Integer type;
}
