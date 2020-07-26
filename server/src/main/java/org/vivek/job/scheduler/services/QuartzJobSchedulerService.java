package org.vivek.job.scheduler.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.vivek.job.scheduler.JobSchedulerService;
import org.vivek.job.scheduler.model.*;
import org.vivek.job.scheduler.model.Job;
import org.vivek.job.scheduler.quartz.jobs.QuartzRestJob;

import java.util.*;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.vivek.job.scheduler.quartz.jobs.QuartzRestJob.*;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableAutoConfiguration
public class QuartzJobSchedulerService implements JobSchedulerService {

    private final Scheduler scheduler;
    private static final String JOB_TYPE = "JOB_TYPE";

    @Override
    public JobResponse insertJob(Job job) {
        boolean success = false;
        String jobId = null;
        switch (job.getJobType()) {
            case REST:
                jobId = insertRestJob(job);
                success = true;
                break;
            case SCRIPT:
                jobId = insertScriptJob(job);
                success = true;
                break;
            default:
                log.error("Failed to insert rest job for quartz scheduler");
        }
        return JobResponse.builder().success(success).jobId(jobId).build();
    }

    private String insertRestJob(Job job) {
        String jobId = null;
        try {
            RestJob restJob = (RestJob) job;
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(REST_JOB_URL, restJob.getJobUrl());
            jobDataMap.put(REST_JOB_BODY_REQUEST, restJob.getJobBodyRequest());
            jobDataMap.put(JOB_TYPE, restJob.getJobType().name());
            jobId = generateJobId();
            JobDetail jobDetail = JobBuilder.newJob(QuartzRestJob.class)
                    .withIdentity(jobId, REST_JOB_GROUP)
                    .withDescription(restJob.getJobDescription())
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();
            scheduler.addJob(jobDetail, false);
        } catch (SchedulerException e) {
            log.error("Failed to insert rest job for quartz scheduler", e);
        }
        return jobId;
    }

    private String insertScriptJob(Job job) {
        throw new UnsupportedOperationException("script job not supported for now");
    }

    @Override
    public JobResponse removeJob(String jobId) {
        try {
            JobKey jobKey = new JobKey(jobId, REST_JOB_GROUP);
            scheduler.deleteJob(jobKey);
            return JobResponse.builder().success(true).jobId(jobId).build();
        } catch (SchedulerException e) {
            log.error("Failed to remove job for quartz scheduler", e);
            return JobResponse.builder().success(false).jobId(jobId).build();
        }
    }

    @Override
    public JobTriggerResponse insertJobTrigger(JobTrigger jobTrigger) {
        String triggerId = null;
        try {
            String timeZone = jobTrigger.getTriggerTimeZone();
            if (StringUtils.isEmpty(jobTrigger.getCronExpression()) && jobTrigger.getTriggerTime() == null) {
                throw new RuntimeException("Invalid job trigger, cron expression or trigger time should be defined");
            }
            triggerId = generateTriggerId();
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                    .withIdentity(triggerId, REST_JOB_GROUP)
                    .forJob(jobTrigger.getJobId(), REST_JOB_GROUP)
                    .startNow();

            if(StringUtils.isEmpty(jobTrigger.getCronExpression())) {
                triggerBuilder.withSchedule(cronSchedule(toCronExpression(jobTrigger.getTriggerTime())).inTimeZone(TimeZone.getTimeZone(timeZone)));
            } else {
                triggerBuilder.withSchedule(cronSchedule(jobTrigger.getCronExpression()).inTimeZone(TimeZone.getTimeZone(timeZone)));
            }
            scheduler.scheduleJob(triggerBuilder.build());
            return JobTriggerResponse.builder().success(true).triggerId(triggerId).build();
        } catch (Exception e) {
            log.error("Failed to insert job trigger for quartz scheduler", e);
            return JobTriggerResponse.builder().success(false).triggerId(triggerId).build();
        }
    }

    private String toCronExpression(TriggerTime triggerTime) {
        String second = "0";
        String minute = String.valueOf(triggerTime.getMinute());
        String hour = String.valueOf(triggerTime.getHour());
        String day = "*";
        String month = "*";
        String year = "?";

        return second + " " + minute + " " + hour + " " + day + " " + month + " " + year;
    }

    @Override
    public JobTriggerResponse removeJobTrigger(String jobTriggerId) {
        try {
            TriggerKey triggerKey = new TriggerKey(jobTriggerId, REST_JOB_GROUP);
            scheduler.unscheduleJob(triggerKey);
            return JobTriggerResponse.builder().success(true).triggerId(jobTriggerId).build();
        } catch (SchedulerException e) {
            log.error("Failed to remove job trigger for quartz scheduler", e);
            return JobTriggerResponse.builder().success(false).build();
        }
    }

    @Override
    public Job getJobByJobId(String jobId) {
        try {
            JobKey jobKey = new JobKey(jobId, REST_JOB_GROUP);
            return getJob(jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to get job with id = {} for quartz scheduler", jobId, e);
            return null;
        }
    }

    @Override
    public List<Job> getJobs() {
        List<Job> jobs = new ArrayList<>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(REST_JOB_GROUP));
            for(JobKey jobKey : jobKeys) {
                jobs.add(getJob(jobKey));
            }
        } catch (SchedulerException e) {
            log.error("Error while fetching all jobs for quartz scheduler");
        }
        return jobs;
    }

    private Job getJob(JobKey jobKey) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if(jobDetail == null) return null;

        List<JobTrigger> jobTriggers = getJobTriggers(jobKey);
        JobType jobType = JobType.valueOf(jobDetail.getJobDataMap().getString(JOB_TYPE));
        Job.JobBuilder jobBuilder = null;
        switch (jobType) {
            case REST:
                jobBuilder = getRestJob(jobDetail, jobKey);
                break;
            case SCRIPT:
                jobBuilder = getScriptJob(jobDetail, jobKey);
                break;
            default:
                throw new RuntimeException("Found Invalid job type while getting job in quartz scheduler");

        }
        if(Objects.nonNull(jobBuilder)) {
            jobBuilder.jobTriggers(jobTriggers);
            return jobBuilder.build();
        }
        return null;
    }

    private List<JobTrigger> getJobTriggers(JobKey jobKey) {
        List<JobTrigger> jobTriggers = new ArrayList<>();
        try {
            if (scheduler.getTriggersOfJob(jobKey).size() > 0) {
                List<CronTrigger> triggers = (List<CronTrigger>) scheduler.getTriggersOfJob(jobKey);
                for (CronTrigger trigger : triggers) {
                    jobTriggers.add(
                            JobTrigger.builder()
                                    .triggerId(trigger.getKey().getName())
                                    .jobId(jobKey.getName())
                                    .cronExpression(trigger.getCronExpression())
                                    .triggerTimeZone(trigger.getTimeZone().getID())
                                    .build()
                    );
                }
            }
        } catch (SchedulerException e) {
            log.error("Error while fetching jpb triggers with job key = {} for quartz scheduler", jobKey);
        }
        return jobTriggers;
    }

    private ScriptJob.ScriptJobBuilder getScriptJob(JobDetail jobDetail, JobKey jobKey) {
        return null;
    }

    private RestJob.RestJobBuilder getRestJob(JobDetail jobDetail, JobKey jobKey) {
        return RestJob.builder()
                .jobId(jobKey.getName())
                .jobUrl(jobDetail.getJobDataMap().getString(REST_JOB_URL))
                .jobType(JobType.REST)
                .jobBodyRequest(jobDetail.getJobDataMap().getString(REST_JOB_BODY_REQUEST))
                .jobDescription(jobDetail.getDescription());
    }

    private String generateJobId() {
        return "JOB_ID" + "_" + UUID.randomUUID().toString();
    }

    private String generateTriggerId() {
        return "TRIGGER_ID" + "_" + UUID.randomUUID().toString();
    }
}
