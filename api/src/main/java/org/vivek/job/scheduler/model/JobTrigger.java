package org.vivek.job.scheduler.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
public class JobTrigger implements Serializable {
    String triggerId;
    String jobId;
    String cronExpression;
    TriggerTime triggerTime;
    String triggerTimeZone;
}
