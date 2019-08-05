package com.myself.spider.WxPlantform;

import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/8/2 19:55
 * @Description:
 */
public abstract class PicVariable {
    public volatile static int original_count = 0;
    public volatile static int fixed_count = 0;
    public volatile static List<Picture> pictures;
}
