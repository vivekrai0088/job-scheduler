package org.vivek.job.scheduler.quartz.jobs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.vivek.job.scheduler.JobSchedulerApplication;

import java.util.UUID;

import static org.vivek.job.scheduler.quartz.jobs.QuartzRestJob.*;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(classes = JobSchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuartzRestJobTest {

    @Autowired
    private QuartzRestJob quartzRestJob;

    @Test
    public void testExecute() {
        try {
            JobExecutionContext jobExecutionContext = getJobExecutionContext();
            quartzRestJob.execute(jobExecutionContext);
        } catch (JobExecutionException e) {
            System.out.println("Unable to execute job \n" + e);
        }
    }

    private JobExecutionContext getJobExecutionContext() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(REST_JOB_URL, "https://www.google.com/");
        jobDataMap.put(REST_JOB_BODY_REQUEST, "{}");
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setJobDataMap(jobDataMap);
        jobDetail.setKey(new JobKey(UUID.randomUUID().toString(), REST_JOB_GROUP));

        return new JobExecutionContextImpl(
                null,
                new TriggerFiredBundle(
                        jobDetail,
                        new SimpleTriggerImpl(),
                        null,
                        false,
                        null,
                        null,
                        null,
                        null),
                null
        );
    }
}
