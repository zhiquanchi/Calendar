package cn.rygel.gd.utils.calendar;

import java.util.Calendar;

public class CalendarUtils {

    private static final int[] LEAP_YEAR_DAYS_OF_MONTH = {31,29,31,30,31,30,31,31,30,31,30,31};
    private static final int[] COMMON_YEAR_DAYS_OF_MONTH = {31,28,31,30,31,30,31,31,30,31,30,31};

    /**
     * 获取当天日期
     * @return
     */
    public static LunarUtils.Solar today(){
        Calendar today = Calendar.getInstance();
        return new LunarUtils.Solar(today.get(Calendar.YEAR),today.get(Calendar.MONTH) + 1,today.get(Calendar.DATE));
    }

    /**
     * 获取某月的天数
     * @param year
     * @param month
     * @return
     */
    public static int getMonthDay(int year,int month){
        return isLeapYear(year) ? LEAP_YEAR_DAYS_OF_MONTH[month - 1] : COMMON_YEAR_DAYS_OF_MONTH[month - 1];
    }

    /**
     * 星期,0开始，0对应周日
     * @param solar
     * @return
     */
    public static int getWeekDay(LunarUtils.Solar solar){
        return getIntervalDaysToBase(solar) % 7;
    }

    /**
     * 是否闰年
     * @param year
     * @return
     */
    public static boolean isLeapYear(int year){
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * 一年内的第几天
     * @param day
     * @return
     */
    public static int getDaysInYear(LunarUtils.Solar day){
        final boolean isLeapYear = isLeapYear(day.solarYear);
        int daysInYear = day.solarDay;
        for(int i = 0;i < day.solarMonth - 1;i++){
            if(isLeapYear){
                daysInYear += LEAP_YEAR_DAYS_OF_MONTH[i];
            }else{
                daysInYear += COMMON_YEAR_DAYS_OF_MONTH[i];
            }
        }
        return daysInYear;
    }

    /**
     * 求当年的第几天的日期
     * @param days
     * @param year
     * @return
     */
    private static LunarUtils.Solar dayInYear(int days, int year){
        //System.out.println(" days : " + days + " year : " + year);
        final boolean isLeapYear = isLeapYear(year);
        LunarUtils.Solar solar = new LunarUtils.Solar(year,0,days);
        while(days > 0){
            solar.solarDay = days;
            solar.solarMonth++;
            if(isLeapYear){
                days -= LEAP_YEAR_DAYS_OF_MONTH[solar.solarMonth - 1];
            }else {
                days -= COMMON_YEAR_DAYS_OF_MONTH[solar.solarMonth - 1];
            }
        }
        return solar;
    }

    /**
     * 判断两个日期哪个更大
     * @param date0
     * @param date1
     * @return
     */
    public static boolean compare(LunarUtils.Solar date0, LunarUtils.Solar date1){
        return ((date0.solarYear << 9) | (date0.solarMonth << 5) | date0.solarDay) -
                ((date1.solarYear << 9) | (date1.solarMonth << 5) | date1.solarDay) >= 0;
    }

    /**
     * 昨天
     * @param solar
     * @return
     */
    public static LunarUtils.Solar yesterday(LunarUtils.Solar solar){
        solar.solarDay--;
        if(solar.solarDay < 1){
            solar.solarMonth--;
            if(solar.solarMonth == 0){
                solar.solarYear--;
                solar.solarMonth = 12;
            }
            solar.solarDay = getMonthDay(solar.solarYear,solar.solarMonth);
        }
        return solar;
    }

    /**
     * 明天
     * @param solar
     * @return
     */
    public static LunarUtils.Solar tomorrow(LunarUtils.Solar solar){
        solar.solarDay++;
        if(solar.solarDay > getMonthDay(solar.solarYear,solar.solarMonth)){
            solar.solarDay = 1;
            solar.solarMonth++;
            if(solar.solarMonth == 13){
                solar.solarMonth = 1;
                solar.solarYear++;
            }
        }
        return solar;
    }

    /**
     * 日期间隔
     * @param start
     * @param end
     * @return
     */
    public static int getIntervalDays(LunarUtils.Solar start, LunarUtils.Solar end){
        return getIntervalDaysToBase(end) - getIntervalDaysToBase(start);
    }

    /**
     * 根据日期间隔计算日期
     * @param start
     * @param interval
     * @return
     */
    public static LunarUtils.Solar getDayByInterval(LunarUtils.Solar start,int interval){
        final int daysToBase = getIntervalDaysToBase(start);
        final int newDayToBase = daysToBase + interval;
        int year = newDayToBase / 365;  //大致估计年份
        final int leapOffset = getLeapYearCount(year);  //大致估计闰年年份(因为年份是不准确的，所以这里也是不准确的)
        final int yearOffset = leapOffset / 365;    //大致估计年份的偏差
        year -= yearOffset; //粗略修正偏差
        LunarUtils.Solar yearBase = new LunarUtils.Solar(year,1,1);
        int yearBaseDaysToBase = getIntervalDaysToBase(yearBase); //计算当年的年初到标准年的间隔
        int offset = newDayToBase - yearBaseDaysToBase + 1; //误差
        int daysOfYear = isLeapYear(year) ? 366 : 365;
        while (offset < 0){
            yearBase = new LunarUtils.Solar(--year,1,1);
            yearBaseDaysToBase = getIntervalDaysToBase(yearBase); //计算当年的年初到标准年的间隔
            offset = newDayToBase - yearBaseDaysToBase + 1; //误差
        }
        while (offset > daysOfYear){
            yearBase = new LunarUtils.Solar(++year,1,1);
            yearBaseDaysToBase = getIntervalDaysToBase(yearBase); //计算当年的年初到标准年的间隔
            offset = newDayToBase - yearBaseDaysToBase + 1; //误差
            daysOfYear = isLeapYear(year) ? 366 : 365;
        }
        return dayInYear(offset,year);
    }

    /**
     * 计算到基准年的日期间隔
     * @param day
     * @return
     */
    private static int getIntervalDaysToBase(LunarUtils.Solar day){
        final int leapYearCount = getLeapYearCountWithoutThisYear(day.solarYear);
        int daysInYear = getDaysInYear(day);
        return leapYearCount + (day.solarYear - 1) * 365 + daysInYear;
    }

    /**
     * 闰年的年数，不包含今年
     * @param year
     * @return
     */
    private static int getLeapYearCountWithoutThisYear(int year){
        return getLeapYearCount(--year);
    }

    /**
     * 闰年的年数，包含今年
     * @param year
     * @return
     */
    private static int getLeapYearCount(int year){
        return year / 4 - year / 100 + year / 400;
    }

    public static LunarUtils.Solar clone(LunarUtils.Solar solar){
        return new LunarUtils.Solar(solar.solarYear,solar.solarMonth,solar.solarDay);
    }

    public static LunarUtils.Lunar clone(LunarUtils.Lunar lunar){
        return new LunarUtils.Lunar(lunar.isLeap,lunar.lunarYear,lunar.lunarMonth,lunar.lunarDay);
    }

}