package com.myself.spider.plantform;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
//    public static String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    public static String date = getTomorrow();

    /**
     * 返回明天日期
     *
     * @return
     */
    public static String getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd");
    }
}
