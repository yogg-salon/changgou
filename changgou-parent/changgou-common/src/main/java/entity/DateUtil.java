package entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author yanming
 * @version 1.0 2021/1/18
 */
public class DateUtil {
    public static final String PATTERN_YYYYMMDDHH = "yyyyMMddHH";
    public static final String PATTERN_YYYY_MM_DDHHMM = "yyyy-MM-dd HH:mm";


    /**
     * 从yyyy-MM-dd HH:mm格式转成yyyyMMddHH格式
     * @param dateStr
     * @param opattern
     * @param npattern
     * @return
     */
    public static String formatStr(String dateStr,String opattern,String npattern ){
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat(opattern);
        try {
            Date date = simpleDateFormat.parse(dateStr);
            simpleDateFormat = new SimpleDateFormat(npattern);
            return  simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  null;
    }


    /**
     * 获取时间菜单
     * @return
     */
    public static List<Date> getDateMenus(){
        //定义List<Date>集合，存储所有时间段
        List<Date> dates =getDate(12);
        Date now =new Date();
        for (Date date : dates) {
            if(date.getTime()<=now.getTime()&& now.getTime()<addDateHours(date,2).getTime()){
                now =date;
                break;
            }
        }
        //当前需要显示的时间菜单
        List<Date> dateMenus =new ArrayList<Date>();
        for (int i = 0; i <5 ; i++) {
            dateMenus.add(addDateHours(now,i*2));
        }
        return  dateMenus;
    }

    /**
     * 指定时间往后的N个间隔时间
     * @param hours
     * @return
     */
    public static List<Date> getDate(int hours) {
        List<Date> dates =new ArrayList<Date>();
        //循环12次
        Date date =toDayStartHours(new Date());//获取指定日期的凌晨凌晨
        for (int i = 0; i < hours; i++) {
            //每次递增2小时，将每次递增的时间存入到List<Date>集合中
            dates.add(addDateHours(date,i*2));
        }
        return dates;
    }

    /**
     * 指定时间增加N小时
     * @param date
     * @param hours
     * @return
     */
    public static Date addDateHours(Date date, int hours) {
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR ,hours);
        date =calendar.getTime();
        return  date;
    }

    /**
     * 指定时间增加N分钟
     * @param date
     * @param minutes
     * @return
     */
    public static Date addDateMinute(Date date, int minutes) {
    Calendar calendar =Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.MINUTE ,minutes);
    date =calendar.getTime();
    return  date;

    }


    /**
     * 获取指定日期的凌晨
     * @param date
     * @return
     */
    public static Date toDayStartHours(Date date) {
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Date time = calendar.getTime();
        return time;

    }
    public static String date2Str(Date date,String pattern){
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat();
        return  simpleDateFormat.format(date);
    }



}
