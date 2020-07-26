package org.vivek.job.scheduler.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class JobTriggerResponse implements Serializable {
    String triggerId;
    boolean success;
}
