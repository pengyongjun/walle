package com.amwalle.walle.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalleJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WalleJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        String jobGroup = context.getJobDetail().getKey().getGroup();
        String jobName = context.getJobDetail().getKey().getName();

        logger.info("Job [" + jobGroup + "." + jobName + "] execute started...");

        // TODO 这里是定时任务具体要做的事

        logger.info("Job [" + jobGroup + "." + jobName + "] execute End...");
    }
}
