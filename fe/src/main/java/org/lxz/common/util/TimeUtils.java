package org.lxz.common.util;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lxz.ConnectContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class TimeUtils {
    private static final Logger LOG = LogManager.getLogger(TimeUtils.class);

    public static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    private static final TimeZone TIME_ZONE;

    // set CST to +08:00 instead of America/Chicago
    public static final ImmutableMap<String, String> TIME_ZONE_ALIAS_MAP = ImmutableMap.of(
            "CST", DEFAULT_TIME_ZONE, "PRC", DEFAULT_TIME_ZONE
    );

    // NOTICE: Date formats are not synchronized.
    // it must be used as synchronized externally.
    private static final SimpleDateFormat DATE_FORMAT;
    private static final SimpleDateFormat DATETIME_FORMAT;
    private static final SimpleDateFormat TIME_FORMAT;

    private static final Pattern DATETIME_FORMAT_REG =
            Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?"
                    + "((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?"
                    + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|("
                    + "\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))"
                    + "[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?"
                    + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))"
                    + "(\\s(((0?[0-9])|([1][0-9])|([2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");

    private static final Pattern TIMEZONE_OFFSET_FORMAT_REG = Pattern.compile("^[+-]{0,1}\\d{1,2}\\:\\d{2}$");

    public static Date MIN_DATE = null;
    public static Date MAX_DATE = null;

    public static Date MIN_DATETIME = null;
    public static Date MAX_DATETIME = null;

    // It's really hard to define max unix timestamp because of timezone.
    // so this value is 253402329599(UTC 9999-12-31 23:59:59) - 24 * 3600(for all timezones)
    public static Long MAX_UNIX_TIMESTAMP = 253402243199L;

    static {
        TIME_ZONE = new SimpleTimeZone(8 * 3600 * 1000, "");

        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        DATE_FORMAT.setTimeZone(TIME_ZONE);

        DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DATETIME_FORMAT.setTimeZone(TIME_ZONE);

        TIME_FORMAT = new SimpleDateFormat("HH");
        TIME_FORMAT.setTimeZone(TIME_ZONE);
        try {
            MIN_DATE = DATE_FORMAT.parse("0000-01-01");
            MAX_DATE = DATE_FORMAT.parse("9999-12-31");

            MIN_DATETIME = DATETIME_FORMAT.parse("0000-01-01 00:00:00");
            MAX_DATETIME = DATETIME_FORMAT.parse("9999-12-31 23:59:59");
        } catch (ParseException e) {
            LOG.error("invalid date format", e);
            System.exit(-1);
        }
    }

    public static long getStartTime() {
        return System.nanoTime();
    }

    public static long getEstimatedTime(long startTime) {
        return System.nanoTime() - startTime;
    }

    public static synchronized String getCurrentFormatTime() {
        return DATE_FORMAT.format(new Date());
    }

    public static TimeZone getTimeZone() {
        String timezone;
        // todo return default now
        return TimeZone.getTimeZone(ZoneId.of(ZoneId.systemDefault().getId(), TIME_ZONE_ALIAS_MAP));
    }

}
