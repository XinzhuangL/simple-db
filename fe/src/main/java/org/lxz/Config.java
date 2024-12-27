package org.lxz;

/**
 * A Simple Config contains some static config
 */
public class Config {

    public static final Integer max_connection_scheduler_threads_num = 4096;

    public static final Long connection_wait_timeout_milliseconds = 28800L;

    public static final Long query_timeout_milliseconds = 120L * 1000;

    public static final Long query_pending_timeout_milliseconds = 120L * 60 * 1000;

    public static String mysql_server_version = "5.1.0";

}
