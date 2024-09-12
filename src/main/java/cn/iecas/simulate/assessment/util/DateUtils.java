package cn.iecas.simulate.assessment.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



/**
 * 时间工具类
 */
public class DateUtils {

    private static Calendar calendar;


    /**
     * 将秒转换为时间字符串
     * @param second
     * @return
     */
    public static String secToTime(int second) {
        StringBuffer strTime = new StringBuffer();
        int min;
        int hour;
        if (second < 60) {
            strTime.append(second + "s").toString();
        }
        if (second >= 60) {
            min = second / 60;
            if (min < 60) {
                strTime.append(min+"m"+(second%60)+"s").toString();
            }
            if (min >= 60) {
                hour = min / 60;
                strTime.append(hour+"h"+(min%60)+"m"+(second-hour*3600-(min%60)*60)+"s").toString();
            }
        }
        return strTime.toString();
    }


    /**
     * 将毫秒秒转换为时间字符串
     * @param millis
     * @return
     */
    public static String millisToTime(long millis) {
        int second = (int) Math.round((double) millis / 1000);
        String time = secToTime(second);
        return time;
    }


    /**
     * 获取指定时间的当天最后时间
     * @param date
     * @return
     */
    public static Date getDayLastTime(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));

        return calendar.getTime();
    }


    /**
     * 获取本周最后时刻
     * @param date
     * @return
     */
    public static Date getWeekLastTime(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));

        return calendar.getTime();
    }


    /**
     * 获取指定时间的当月最后时刻
     * @param date
     * @return
     */
    public static Date getMonthLastTime(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));

        return calendar.getTime();
    }


    /**
     * 获取指定时间的当年最后时刻
     * @param date
     * @return
     */
    public static Date getYearLastTime(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));

        return calendar.getTime();
    }


    /**
     * 根据参数返回固定格式的日期
     * @param date
     * @param standard
     * @return
     * @throws ParseException
     */
    public static String getValidDate(Date date, String standard) throws ParseException {
        SimpleDateFormat dateFormat = null;
        if (standard.toUpperCase().equals("DAY")) { // 筛选维度
            dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        } else if (standard.toUpperCase().equals("WEEK")) {
            dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        } else if (standard.toUpperCase().equals("MONTH")) {
            dateFormat = new SimpleDateFormat("yyyy年MM月");
        } else if (standard.toUpperCase().equals("YEAR")) {
            dateFormat = new SimpleDateFormat("yyyy年");
        }

        return dateFormat.format(date);
    }


    /**
     * 获取指定参数的明天的0点时间
     * @param date
     * @return
     */
     public static Date getTomorrowStartTime(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
