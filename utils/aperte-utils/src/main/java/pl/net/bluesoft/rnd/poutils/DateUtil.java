package pl.net.bluesoft.rnd.poutils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * User: POlszewski
 * Date: 2011-07-29
 * Time: 13:33:51
 */
public final class DateUtil {
    public static int MINUTES_IN_DAY = 24*60;
    public static int SECONDS_IN_DAY = 24*60*60;
    public static int MILLISECONDS_IN_DAY = 24*60*60*1000;

    public static Date createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date createDate(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    // miesiace liczymy od 1
    public static Date createDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date createDate(int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Timestamp createTimestamp(int year, int month, int day, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp createTimestamp(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    // miesiace liczymy od 1
    public static Timestamp createTimestamp(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp createTimestamp(int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), month - 1, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    public static Date beginOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date endOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
        c.add(Calendar.MILLISECOND, -1);
        return c.getTime();
    }

    public static Timestamp beginOfMonth(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp endOfMonth(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
        c.add(Calendar.MILLISECOND, -1);
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        ts.setNanos(999999999);
        return ts;
    }

    public static Date truncHours(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date truncMilliseconds(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Timestamp truncHours(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        ts.setNanos(0);
        return ts;
    }

    public static Timestamp truncMilliseconds(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        ts.setNanos(0);
        return ts;
    }

    public static Timestamp truncNanos(Timestamp timestamp) {
        Timestamp ts = new Timestamp(timestamp.getTime());
        ts.setNanos((ts.getNanos()/1000000)*1000000);
        return ts;
    }

    public static int getYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }

    public static int getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int d = c.get(Calendar.DAY_OF_WEEK);
        return d == Calendar.SATURDAY ? 7 : d - 1;
    }

    public static int getHour(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    public static int getSecond(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.SECOND);
    }

    public static int getMillisecond(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MILLISECOND);
    }

    public static int getNanos(Timestamp timestamp) {
        return timestamp.getNanos();
    }

    public static long diffMilliseconds(Date date1, Date date2) {
        return date2.getTime() - date1.getTime();
    }

    public static long diffSeconds(Date date1, Date date2) {
        return (date2.getTime() - date1.getTime())/1000;
    }

    public static long diffMinutes(Date date1, Date date2) {
        return (date2.getTime() - date1.getTime())/(60*1000);
    }

    public static long diffHours(Date date1, Date date2) {
        return (date2.getTime() - date1.getTime())/(60*60*1000);
    }

    public static long diffDays(Date date1, Date date2) {
        return (date2.getTime() - date1.getTime())/(24*60*60*1000);
    }

    private static Date add(Date date, int unit, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(unit, amount);
        return c.getTime();
    }

    public static Date addMilliseconds(Date date, int milliseconds) {
        return add(date, Calendar.MILLISECOND, milliseconds);
    }

    public static Date addSeconds(Date date, int seconds) {
        return add(date, Calendar.SECOND, seconds);
    }

    public static Date addMinutes(Date date, int minutes) {
        return add(date, Calendar.MINUTE, minutes);
    }

    public static Date addHours(Date date, int hours) {
        return add(date, Calendar.HOUR, hours);
    }

    public static Date addDays(Date date, int days) {
        return add(date, Calendar.DATE, days);
    }

    private static Timestamp add(Timestamp timestamp, int unit, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.add(unit, amount);
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        ts.setNanos(ts.getNanos() + timestamp.getNanos()%1000000);
        return ts;
    }

    public static Timestamp addMilliseconds(Timestamp timestamp, int milliseconds) {
        return add(timestamp, Calendar.MILLISECOND, milliseconds);
    }

    public static Timestamp addSeconds(Timestamp timestamp, int seconds) {
        return add(timestamp, Calendar.SECOND, seconds);
    }

    public static Timestamp addMinutes(Timestamp timestamp, int minutes) {
        return add(timestamp, Calendar.MINUTE, minutes);
    }

    public static Timestamp addHours(Timestamp timestamp, int hours) {
        return add(timestamp, Calendar.HOUR, hours);
    }

    public static Timestamp addDays(Timestamp timestamp, int days) {
        return add(timestamp, Calendar.DATE, days);
    }

    public static Date beginOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date endOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    public static Timestamp beginOfDay(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp endOfDay(Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTime(timestamp);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Timestamp ts = new Timestamp(c.getTimeInMillis());
        ts.setNanos(999999999);
        return ts;
    }

    public static boolean between(Date date, Date dateFrom, Date dateTo) {
        return (dateFrom == null || date.after(dateFrom) || date.equals(dateFrom)) &&
               (dateTo == null || date.before(dateTo) || date.equals(dateTo));
    }

    public static boolean afterInclusive(Date date, Date dateFrom) {
        return date.after(dateFrom) || date.equals(dateFrom);
    }

    public static boolean beforeInclusive(Date date, Date dateTo) {
        return date.after(dateTo) || date.equals(dateTo);
    }
}
