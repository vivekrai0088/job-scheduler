package org.vivek.job.scheduler.quartz.jobs;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class QuartzRestJob implements Job {
    public static final String REST_JOB_URL = "REST_JOB_URL";
    public static final String REST_JOB_BODY_REQUEST = "REST_JOB_BODY_REQUEST";
    public static final String REST_JOB_GROUP = "REST_JOB_GROUP";

    private final RestTemplateBuilder restTemplateBuilder;
    private RestTemplate restTemplate;

    private final Gson gson = new Gson();

    @PostConstruct
    private void postConstruct() {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            String url = jobDataMap.getString(REST_JOB_URL);
            String bodyRequest = jobDataMap.getString(REST_JOB_BODY_REQUEST);

            HttpEntity<String> entity = new HttpEntity<>(bodyRequest, httpHeaders);

            log.info("Executing REST job id " + context.getJobDetail().getKey().getName() + " url = " + url + " , request = " + bodyRequest);
            ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(url, String.class);
            log.info("Response = {}", gson.toJson(responseEntity));
            context.setResult(responseEntity);

        } catch (Exception e) {
            log.info("Unable to execute job", e);
        }
    }
}
