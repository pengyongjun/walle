package com.amwalle.walle.scheduler;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class WalleJobManager {
    private static final Logger logger = LoggerFactory.getLogger(WalleJobManager.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    private JobListener scheduleListener;

    /**
     * 新建并开始一个定时任务
     *
     * @param cron     定时任务执行的时间表达式
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @param jobClass 任务定义的类
     * @throws SchedulerException  任务异常
     */
    public void startJob(String cron, String jobName, String jobGroup, Class<? extends Job> jobClass) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        if (null == scheduleListener) {
            scheduleListener = new SchedulerListener();
            scheduler.getListenerManager().addJobListener(scheduleListener);
        }

        JobKey jobKey = new JobKey(jobName, jobGroup);

        if (!scheduler.checkExists(jobKey)) {
            scheduleJob(cron, scheduler, jobName, jobGroup, jobClass);
        }
    }

    /**
     * 动态创建一个定时任务
     *
     * @param cron     定时任务执行的时间表达式
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @param jobClass 任务定义的类
     * @throws SchedulerException 任务执行异常
     */
    private void scheduleJob(String cron, Scheduler scheduler, String jobName, String jobGroup, Class<? extends Job> jobClass) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(scheduleBuilder).build();

        scheduler.scheduleJob(jobDetail, cronTrigger);
    }

    /**
     * 删除一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @throws SchedulerException 任务异常
     */
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);
        logger.info("Job [" + jobGroup + "." + jobName + "] was deleted!");
    }

    /**
     * 暂停一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @throws SchedulerException 任务异常
     */
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);
        logger.info("Job [" + jobGroup + "." + jobName + "] was paused!");
    }

    /**
     * 恢复一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @throws SchedulerException 任务异常
     */
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);
        logger.info("Job [" + jobGroup + "." + jobName + "] was resumed!");
    }

    /**
     * 手动出发一次定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @throws SchedulerException 任务异常
     */
    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);
        logger.info("Job [" + jobGroup + "." + jobName + "] was triggered!");
    }

    /**
     * 重新设定一个任务执行的时间表达式
     *
     * @param jobName  任务名
     * @param jobGroup 任务组
     * @throws SchedulerException 任务异常
     */
    public void resetJobCron(String jobName, String jobGroup, String cron) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(scheduleBuilder).build();
        scheduler.rescheduleJob(TriggerKey.triggerKey(jobName, jobGroup), cronTrigger);
        logger.info("Job [" + jobGroup + "." + jobName + "] was reset cron to: " + cron);
    }
}
