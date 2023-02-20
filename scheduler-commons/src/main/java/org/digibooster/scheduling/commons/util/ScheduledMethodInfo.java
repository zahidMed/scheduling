package org.digibooster.scheduling.commons.util;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.TimeZone;

@Data
@Builder
public class ScheduledMethodInfo implements Serializable {

    private static final long serialVersionUID = 2128945642437792221L;

    private ScheduleType scheduleType;

    private String beanName;

    private String targetClass;

    private String method;

    private String cron;

    private TimeZone timeZone;

    private long fixedDelay;

    private long initialDelay;

    private long fixedRate;
}
