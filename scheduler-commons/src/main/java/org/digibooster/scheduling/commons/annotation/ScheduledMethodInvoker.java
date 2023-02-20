package org.digibooster.scheduling.commons.annotation;

import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;

import java.util.Set;

public interface ScheduledMethodInvoker {
    void invoke(ScheduledMethodInfo scheduledMethodInfo);
}
