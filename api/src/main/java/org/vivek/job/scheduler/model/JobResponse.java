package org.vivek.job.scheduler.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class JobResponse implements Serializable {
    String jobId;
    boolean success;
}
