package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
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

    public UITimer(long delayMillis, Runnable task) {
        this(UI.getCurrent(), delayMillis, task);
        if (this.ui == null) {
            throw new IllegalStateException("Cannot automatically obtain UI. Make sure you're calling from a UI thread.");
        }
    }

    public UITimer(Runnable task) {
        this(1000, task);
    }

    /**
     * 使用Vaadin推荐的带detachHandler的accessLater方法。
     */
    private Runnable createUITask(Runnable originalTask) {
        return ui.accessLater(
                () -> {
                    log.debug("UITimer executing task safely on UI thread.");
                    originalTask.run();
                    ui.push();  // 如果你使用Push，需要显式调用push()，否则可以删除
                },
                () -> {
                    // detachHandler: 当UI被detach后如何处理
                    log.warn("UITimer: UI has been detached; task skipped.");
                }
        );
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
