package com.tiny.scheduling.service;

import com.tiny.scheduling.model.SchedulingDag;
import com.tiny.scheduling.job.DagExecutionJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quartz 调度器服务
 * 负责管理 Quartz Job 和 Trigger
 */
@Service
public class QuartzSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(QuartzSchedulerService.class);

    private final Scheduler scheduler;

    public QuartzSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 创建或更新 DAG 的 Quartz Job（用于定时调度）
     */
    @Transactional
    public void createOrUpdateDagJob(SchedulingDag dag, String cronExpression) throws SchedulerException {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            logger.debug("DAG {} 没有配置 cron 表达式，跳过创建 Job", dag.getId());
            return;
        }

        String jobKey = "dag-" + dag.getId();
        String triggerKey = "dag-trigger-" + dag.getId();
        JobKey jobKeyObj = JobKey.jobKey(jobKey, "dag-group");
        TriggerKey triggerKeyObj = TriggerKey.triggerKey(triggerKey, "dag-trigger-group");

        // 如果 Job 已存在，先删除
        if (scheduler.checkExists(jobKeyObj)) {
            scheduler.deleteJob(jobKeyObj);
            logger.debug("删除已存在的 DAG Job, dagId: {}, jobKey: {}", dag.getId(), jobKey);
        }

        // 创建新的 JobDetail
        JobDetail jobDetail = JobBuilder.newJob(DagExecutionJob.class)
                .withIdentity(jobKeyObj)
                .usingJobData("dagId", dag.getId())
                .storeDurably(true)
                .build();

        // 创建 Cron Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKeyObj)
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("创建/更新 DAG 定时调度 Job, dagId: {}, jobKey: {}, cron: {}", dag.getId(), jobKey, cronExpression);
    }

    /**
     * 立即触发 DAG 执行（创建一次性 Job）
     * @param dag DAG 对象
     * @param dagRunId DAG 运行实例 ID（手动触发时传递，定时触发时为 null）
     * @param dagVersionId DAG 版本 ID（手动触发时传递，定时触发时为 null）
     */
    @Transactional
    public void triggerDagNow(SchedulingDag dag, Long dagRunId, Long dagVersionId) throws SchedulerException {
        String jobKey = "dag-trigger-now-" + dag.getId() + "-" + System.currentTimeMillis();
        
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("dagId", dag.getId());
        if (dagRunId != null && dagRunId > 0) {
            jobDataMap.put("dagRunId", dagRunId);
        }
        if (dagVersionId != null && dagVersionId > 0) {
            jobDataMap.put("dagVersionId", dagVersionId);
        }
        
        JobDetail jobDetail = JobBuilder.newJob(DagExecutionJob.class)
                .withIdentity(jobKey, "dag-trigger-group")
                .usingJobData(jobDataMap)
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + jobKey, "dag-trigger-group")
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("立即触发 DAG 执行, dagId: {}, dagRunId: {}, dagVersionId: {}, jobKey: {}", 
                dag.getId(), dagRunId, dagVersionId, jobKey);
    }

    /**
     * 删除 DAG 的 Quartz Job
     */
    @Transactional
    public void deleteDagJob(Long dagId) throws SchedulerException {
        String jobKey = "dag-" + dagId;
        JobKey jobKeyObj = JobKey.jobKey(jobKey, "dag-group");
        
        if (scheduler.checkExists(jobKeyObj)) {
            scheduler.deleteJob(jobKeyObj);
            logger.info("删除 DAG Job, dagId: {}, jobKey: {}", dagId, jobKey);
        }
    }

    /**
     * 暂停 DAG 的 Quartz Job
     */
    @Transactional
    public void pauseDagJob(Long dagId) throws SchedulerException {
        String jobKey = "dag-" + dagId;
        JobKey jobKeyObj = JobKey.jobKey(jobKey, "dag-group");
        
        if (scheduler.checkExists(jobKeyObj)) {
            scheduler.pauseJob(jobKeyObj);
            logger.info("暂停 DAG Job, dagId: {}, jobKey: {}", dagId, jobKey);
        }
    }

    /**
     * 恢复 DAG 的 Quartz Job
     */
    @Transactional
    public void resumeDagJob(Long dagId) throws SchedulerException {
        String jobKey = "dag-" + dagId;
        JobKey jobKeyObj = JobKey.jobKey(jobKey, "dag-group");
        
        if (scheduler.checkExists(jobKeyObj)) {
            scheduler.resumeJob(jobKeyObj);
            logger.info("恢复 DAG Job, dagId: {}, jobKey: {}", dagId, jobKey);
        }
    }

    /**
     * 更新 DAG 的 Cron 表达式
     */
    @Transactional
    public void updateDagCron(Long dagId, String cronExpression) throws SchedulerException {
        String triggerKey = "dag-trigger-" + dagId;
        TriggerKey triggerKeyObj = TriggerKey.triggerKey(triggerKey, "dag-trigger-group");
        
        if (scheduler.checkExists(triggerKeyObj)) {
            CronTrigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKeyObj)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
            scheduler.rescheduleJob(triggerKeyObj, newTrigger);
            logger.info("更新 DAG Cron, dagId: {}, cron: {}", dagId, cronExpression);
        }
    }
}

