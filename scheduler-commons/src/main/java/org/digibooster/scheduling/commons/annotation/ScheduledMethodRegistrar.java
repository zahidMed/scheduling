package org.digibooster.scheduling.commons.annotation;

import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;

import java.util.Collection;

public interface ScheduledMethodRegistrar {

    void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo);
}
