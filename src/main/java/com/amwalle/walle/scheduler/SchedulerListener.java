package com.amwalle.walle.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerListener implements JobListener {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerListener.class);

    private static final String LISTENER_NAME = "QuartSchedulerListener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    /**
     * 任务调度之前
     *
     * @param jobExecutionContext Job Execution Context
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        String jobName = jobExecutionContext.getJobDetail().getKey().toString();
        logger.info("Job [" + jobName + "] is going to be executed >>>>>>>>>>");
    }

    /**
     * 任务调度被拒绝
     *
     * @param jobExecutionContext Job Execution Context
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        String jobName = jobExecutionContext.getJobDetail().getKey().toString();
        logger.info("Job [" + jobName + "] execution is vetoed!");
    }

    /**
     * 任务调度之后
     *
     * @param jobExecutionContext Job Execution Context
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException exception) {
        String jobName = jobExecutionContext.getJobDetail().getKey().toString();
        logger.info("Job [" + jobName + "] execute finished! <<<<<<<<<<<<");
        if (null != exception) {
            logger.info("Exception thrown by [" + jobName + "]");
            logger.info("Exception message: " + (exception.getMessage().equals("") ? "" : exception.getMessage()));
        }
    }
}
