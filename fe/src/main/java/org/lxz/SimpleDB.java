package org.lxz;

import org.lxz.common.Config;

public class SimpleDB {

    /*
    mysql -uroot -P9030 -h127.0.0.1 -proot123456
     */
    public static void main(String[] args) throws InterruptedException {
        ConnectScheduler connectScheduler = new ConnectScheduler(Config.max_connection_scheduler_threads_num);
        QeService qeService = new QeService(9030, true, connectScheduler);

        qeService.start();

        while (true) {
            Thread.sleep(1000);
        }

    }
}
