package dev.w0fv1.vaadmin.view.tools;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Timer {

    private final long delayMillis;
    private final Runnable task;
    private volatile Thread timerThread;
    private volatile boolean started = false;

    /**
     * 构造一个 Timer 实例
     * @param delayMillis 延迟执行时间，单位毫秒
     * @param task        到点执行的任务
     */
    public Timer(long delayMillis, Runnable task) {
        this.delayMillis = delayMillis;
        this.task = task;
    }

    /**
     * 启动计时器，延迟 delayMillis 后执行任务
     * 如果已经启动则不重复启动
     */
    public synchronized void start() {
        if (started) {
            log.debug("Timer already started, skipping start.");
            return;
        }
        started = true;
        log.debug("Starting timer with delay {} ms", delayMillis);
        // 启动一个虚拟线程
        timerThread = Thread.startVirtualThread(() -> {
            try {
                log.debug("Timer thread started, sleeping for {} ms", delayMillis);
                Thread.sleep(delayMillis);
                log.debug("Timer thread woke up, executing task.");
                task.run();
            } catch (InterruptedException e) {
                log.debug("Timer thread was interrupted, task will not be executed.");
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 取消计时任务，如果任务尚未执行，则中断虚拟线程
     */
    public synchronized void cancel() {
        if (timerThread != null && timerThread.isAlive()) {
            log.debug("Cancelling timer.");
            timerThread.interrupt();
        } else {
            log.debug("Timer is not active or already executed, no need to cancel.");
        }
    }
}
