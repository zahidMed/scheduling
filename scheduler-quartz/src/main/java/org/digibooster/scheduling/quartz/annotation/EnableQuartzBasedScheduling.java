package org.digibooster.scheduling.quartz.annotation;

import org.springframework.context.annotation.Import;

@Import(QuartzBasedScheduledMethodConfiguration.class)
public @interface EnableQuartzBasedScheduling {
}
