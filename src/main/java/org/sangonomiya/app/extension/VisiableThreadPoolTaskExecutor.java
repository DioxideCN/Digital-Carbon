package org.sangonomiya.app.extension;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 重写线程池方法在线程池启动前通过日志输出线程池配置信息
 *
 * @author Dioxide.CN
 * @date 2023/3/8 15:23
 * @since 1.0
 */
public class VisiableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisiableThreadPoolTaskExecutor.class);

    // 线程池状态监听
    private void showThreadPoolInfo(String prefix) {
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
        //打印线程池使用日志
        LOGGER.info("{}, {},taskCount [{}], completedTaskCount [{}], activeCount [{}], queueSize [{}]",
                this.getThreadNamePrefix(),
                prefix,
                threadPoolExecutor.getTaskCount(),           //任务总数
                threadPoolExecutor.getCompletedTaskCount(),  //已完成任务数
                threadPoolExecutor.getActiveCount(),         //活跃线程数
                threadPoolExecutor.getQueue().size()         //队列大小
        );
    }

    @Override
    public void execute(@NotNull Runnable task) {
        showThreadPoolInfo("1. do execute");
        super.execute(task);
    }

    @NotNull
    @Override
    public Future<?> submit(@NotNull Runnable task) {
        showThreadPoolInfo("1. do submit");
        return super.submit(task);
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Callable<T> task) {
        showThreadPoolInfo("2. do submit");
        return super.submit(task);
    }

    @NotNull
    @Override
    public ListenableFuture<?> submitListenable(@NotNull Runnable task) {
        showThreadPoolInfo("1. do submitListenable");
        return super.submitListenable(task);
    }

    @NotNull
    @Override
    public <T> ListenableFuture<T> submitListenable(@NotNull Callable<T> task) {
        showThreadPoolInfo("2. do submitListenable");
        return super.submitListenable(task);
    }

}
