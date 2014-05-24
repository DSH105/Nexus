package com.dsh105.nexus.server.threading;

public class SleepForeverThread extends Thread {

    public SleepForeverThread() {
        super("Sleeping beauty");
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // Swallow
                e.printStackTrace();
            }
        }
    }
}
