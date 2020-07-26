package org.vivek.job.scheduler;

import org.vivek.job.scheduler.model.Job;
import org.vivek.job.scheduler.model.JobResponse;
import org.vivek.job.scheduler.model.JobTrigger;
import org.vivek.job.scheduler.model.JobTriggerResponse;

import java.util.List;

public interface JobSchedulerService {

    JobResponse insertJob(Job job);
    JobResponse removeJob(String jobId);
    JobTriggerResponse insertJobTrigger(JobTrigger jobTrigger);
    JobTriggerResponse removeJobTrigger(String jobTriggerId);
    Job getJobByJobId(String jobId);
    List<Job> getJobs();

}
