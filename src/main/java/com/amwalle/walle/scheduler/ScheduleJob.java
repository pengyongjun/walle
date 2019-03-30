package com.amwalle.walle.scheduler;

public class ScheduleJob {
    /** 任务id */
    private String jobId;
    /** 任务名称 */
    private String jobName;
    /** 任务分组 */
    private String jobGroup;
    /** 任务状态 0禁用 1启用 2删除*/
    private String jobStatus;
    /** 任务运行时间表达式 */
    private String cronExpression;
    /** 任务描述 */
    private String desc;

    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getDesc() {
        return desc;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
