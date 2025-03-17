package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.component.UI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UITimer {

    private final Timer timer;
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
        this.timer = new Timer(delayMillis, createUITask(task));
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
     * 自动获取当前UI实例、默认延迟1000ms的构造函数（仅在UI线程调用有效）
     *
     * @param task 到点执行的任务
     */
    public UITimer(Runnable task) {
        this(1000, task);
    }

    /**
     * 创建封装UI安全调用的任务
     *
     * @param originalTask 原始任务
     * @return UI安全封装后的任务
     */
    private Runnable createUITask(Runnable originalTask) {
        return () -> {
            log.debug("UITimer executing task safely on UI thread.");
            ui.access(() -> {
                originalTask.run();
                ui.push();
            });
        };
    }

    /**
     * 启动计时器，延迟后执行任务（UI安全）
     */
    public synchronized void start() {
        log.debug("UITimer starting.");
        ui.setPollInterval(1000);
        timer.start();
    }

    /**
     * 取消计时任务
     */
    public synchronized void cancel() {
        log.debug("UITimer cancelling.");
        timer.cancel();
    }
}
