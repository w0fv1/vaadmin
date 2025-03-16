package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UITimer {

    private final long delayMillis;
    private final Runnable task;
    private volatile Thread timerThread;
    private volatile boolean started = false;
    private final UI ui;

    /**
     * 构造一个适用于 Vaadin UI 更新的 UITimer 实例
     *
     * @param ui          Vaadin UI实例，用于安全访问UI
     * @param delayMillis 延迟执行时间，单位毫秒
     * @param task        到点执行的任务
     */
    public UITimer(UI ui, long delayMillis, Runnable task) {
        this.ui = ui;
        this.delayMillis = delayMillis;
        this.task = task;
    }

    /**
     * 自动获取当前UI实例的构造函数（仅在UI线程调用有效）
     *
     * @param delayMillis 延迟执行时间，单位毫秒
     * @param task        到点执行的任务
     */
    public UITimer(long delayMillis, Runnable task) {
        this(UI.getCurrent(), delayMillis, task);
        if (this.ui == null) {
            throw new IllegalStateException("Cannot automatically obtain UI. Make sure you're calling from a UI thread.");
        }
    }

    /**
     * 自动获取当前UI实例的构造函数（仅在UI线程调用有效）
     *
     * @param task        到点执行的任务
     */
    public UITimer(Runnable task) {
        this(UI.getCurrent(), 1000, task);
        if (this.ui == null) {
            throw new IllegalStateException("Cannot automatically obtain UI. Make sure you're calling from a UI thread.");
        }
    }

    /**
     * 启动计时器，延迟后执行任务（UI安全）
     */
    public synchronized void start() {
        if (started) {
            log.debug("UITimer already started.");
            return;
        }
        started = true;
        log.debug("UITimer starting with delay: {} ms", delayMillis);

        ui.setPollInterval(1000);

        timerThread = Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(delayMillis);
                ui.access(() -> {
                    log.debug("UITimer executing task safely on UI thread.");
                    task.run();
                    ui.push();
                });
            } catch (InterruptedException e) {
                log.debug("UITimer interrupted before executing task.");
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 取消计时任务
     */
    public synchronized void cancel() {
        if (timerThread != null && timerThread.isAlive()) {
            log.debug("UITimer cancelling.");
            timerThread.interrupt();
        } else {
            log.debug("UITimer not active or already executed.");
        }
    }
}