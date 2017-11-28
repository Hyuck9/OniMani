package com.lhg1304.onimani.common.utils;

import java.util.Calendar;

/**
 * Created by Nexmore on 2017-11-28.
 */

public class DateUtil {

    /**
     * 현재 시간 리턴
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.KOREA);
        return formatter.format(calendar.getTime());
    }

}
