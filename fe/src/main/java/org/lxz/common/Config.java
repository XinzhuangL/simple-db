package org.lxz.common;

/**
 * A Simple Config contains some static config
 */
public class Config {

    public static final Integer max_connection_scheduler_threads_num = 4096;

    public static int max_mysql_service_task_threads_num = 4096;

    public static int mysql_service_io_threads_num = 4;

    /**
     * The backlog_num for mysql nio server
     * When you enlarge this backlog_num, you should ensure its value larger than
     * the linux /proc/sys/net/core/somaxconn config
     */
    public static int mysql_nio_backlog_num = 1024;

    public static final Long connection_wait_timeout_milliseconds = 28800L;

    public static final Long query_timeout_milliseconds = 120L * 1000;

    public static final Long query_pending_timeout_milliseconds = 120L * 60 * 1000;

    public static String mysql_server_version = "5.1.0";

    public static final Integer parse_tokens_limit = 3500000;
    public static final Integer expr_children_limit = 10000;

}
