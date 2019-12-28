package com.myself.spider.plantform;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: Holeski
 * @Date: 2019/8/2 19:55
 * @Description:
 */
public abstract class PicVariable {
    public volatile static int original_count = 0;
    public volatile static int avatar_count = 0;
    public volatile static List<Picture> pictures;
    public volatile static List<PictureVo> voList = new ArrayList<>();
    public volatile static boolean isLogin = false;
    public volatile static boolean isLanzouLogin = false;
}
