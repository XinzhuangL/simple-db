package org.lxz;

public class SimpleDB {

    public static void main(String[] args) throws InterruptedException {
        ConnectScheduler connectScheduler = new ConnectScheduler(Config.max_connection_scheduler_threads_num);
        QeService qeService = new QeService(9030, false, connectScheduler);

        qeService.start();

        while (true) {
            Thread.sleep(1000);
        }

    }
}
