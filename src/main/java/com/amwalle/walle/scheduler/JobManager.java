package com.amwalle.walle.scheduler;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class JobManager {

    private static JobManager jobManager;

//    @Autowired
//    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private Scheduler scheduler;

    public void test() throws SchedulerException {
//        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        ScheduleJob scheduleJob = new ScheduleJob();
        scheduleJob.setJobName("test");
        scheduleJob.setJobGroup("1");

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        //不存在，创建一个
        if (null == trigger) {
            JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class)
                    .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            jobDetail.getJobDataMap().put("scheduleJob", scheduleJob);

            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob
                    .getCronExpression());

            //按新的cronExpression表达式构建一个新的trigger
            trigger = TriggerBuilder.newTrigger().withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                    .withSchedule(scheduleBuilder).build();

            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            // Trigger已存在，那么更新相应的定时设置
            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob
                    .getCronExpression());

            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder).build();

            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

}
