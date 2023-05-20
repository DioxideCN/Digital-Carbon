package org.sangonomiya.app.config;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.sangonomiya.app.core.CustomJobFactory;
import org.sangonomiya.app.core.quartz.CleanCompanyJob;
import org.sangonomiya.app.core.quartz.SQLGenerateJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Dioxide.CN
 * @date 2023/4/6 10:19
 * @since 1.0
 */
@Configuration
public class QuartzConfig {

    @Resource
    private CustomJobFactory customJobFactory;

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        // 自定义 JobFactory 使得在 Quartz Job 中可以使用 @Autowired
        scheduler.setJobFactory(customJobFactory);
        scheduler.start();
        return scheduler;
    }

    @Bean
    public JobDetail cleanCompanyJob() {
        return JobBuilder.newJob(CleanCompanyJob.class)
                .withIdentity("cleanCompanyJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger cronTrigger() {
        // 简单的调度计划的构造器
        return TriggerBuilder
                .newTrigger()
                .forJob(cleanCompanyJob())
                .withIdentity("cronTrigger")
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule("0 0 0/1 * * ? *"))
                .build();
    }

    @Bean
    public JobDetail sqlGenerateJob() {
        return JobBuilder.newJob(SQLGenerateJob.class)
                .withIdentity("sqlGenerateJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger cronTrigger2() {
        // 简单的调度计划的构造器
        return TriggerBuilder
                .newTrigger()
                .forJob(sqlGenerateJob())
                .withIdentity("cronTrigger2")
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule("0 0/1 * * * ? *"))
                .build();
    }

}
