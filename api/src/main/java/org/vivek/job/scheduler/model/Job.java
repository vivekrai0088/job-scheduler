package org.vivek.job.scheduler.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "id")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RestJob.class, name = "rest-job"),
        @JsonSubTypes.Type(value = ScriptJob.class, name = "script-job")
})
@Data
@NoArgsConstructor
@SuperBuilder
public abstract class Job implements Serializable {
    String id;
    JobType jobType;
    String jobId;
    String jobDescription;
    List<JobTrigger> jobTriggers;
}
