package org.vivek.job.scheduler.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@SuperBuilder
public class TriggerTime implements Serializable {
    Long minute;
    Long hour;
}
