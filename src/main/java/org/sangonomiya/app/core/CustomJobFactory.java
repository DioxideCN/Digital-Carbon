package org.sangonomiya.app.core;

import org.jetbrains.annotations.NotNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

import javax.annotation.Resource;

/**
 * @author Dioxide.CN
 * @date 2023/4/6 10:18
 * @since 1.0
 */
@Configuration
public class CustomJobFactory extends AdaptableJobFactory {

    @Resource
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    /**
     * Create the job instance, populating it with property values taken
     * from the scheduler context, job data map and trigger data map.
     */
    @NotNull
    @Override
    protected Object createJobInstance(@NotNull TriggerFiredBundle bundle) throws Exception {
        Object jobInstance = super.createJobInstance(bundle);
        autowireCapableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }

}
