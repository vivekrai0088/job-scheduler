package org.vivek.job.scheduler.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.vivek.job.scheduler.JobSchedulerRestService;
import org.vivek.job.scheduler.model.Job;
import org.vivek.job.scheduler.model.JobResponse;
import org.vivek.job.scheduler.model.JobTrigger;
import org.vivek.job.scheduler.model.JobTriggerResponse;
import org.vivek.job.scheduler.services.QuartzJobSchedulerService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = JobSchedulerRestService.JOB_SCHEDULER_SERVICE + "/" + JobSchedulerRestService.QUARTZ_SCHEDULER)
@RequiredArgsConstructor
public class QuartzJobSchedulerRestService implements JobSchedulerRestService {

    private final QuartzJobSchedulerService quartzJobSchedulerService;

    @PostMapping(value = JobSchedulerRestService.JOB)
    @Override
    public JobResponse insertJob(@RequestBody Job job) {
        log.info("InsertJob triggered with request = {}", job.toString());
        return quartzJobSchedulerService.insertJob(job);
    }

    @DeleteMapping(value = JobSchedulerRestService.JOB + "/{jobId}")
    @Override
    public JobResponse removeJob(@PathVariable String jobId) {
        log.info("RemoveJob triggered with jobId = {}", jobId);
        return quartzJobSchedulerService.removeJob(jobId);
    }

    @PostMapping(value = JobSchedulerRestService.JOB + "/" + JobSchedulerRestService.TRIGGER)
    @Override
    public JobTriggerResponse insertJobTrigger(@RequestBody JobTrigger jobTrigger) {
        log.info("InsertJobTrigger triggered with request = {}", jobTrigger.toString());
        return quartzJobSchedulerService.insertJobTrigger(jobTrigger);
    }

    @DeleteMapping(value = JobSchedulerRestService.JOB + "/" + JobSchedulerRestService.TRIGGER + "/{jobTriggerId}")
    @Override
    public JobTriggerResponse removeJobTrigger(@PathVariable String jobTriggerId) {
        log.info("RemoveJobTrigger triggered with jobTriggerId = {}", jobTriggerId);
        return quartzJobSchedulerService.removeJobTrigger(jobTriggerId);
    }

    @GetMapping(value = JobSchedulerRestService.JOB + "/{jobId}")
    @Override
    public Job getJobByJobId(@PathVariable String jobId) {
        log.info("GetJobByJobId triggered with jobId = {}", jobId);
        return quartzJobSchedulerService.getJobByJobId(jobId);
    }

    @GetMapping(value = JobSchedulerRestService.JOBS)
    @Override
    public List<Job> getJobs() {
        log.info("GetJobs triggered");
        return quartzJobSchedulerService.getJobs();
    }
}
