package org.digibooster.scheduling.quartz.annotation;

import org.digibooster.scheduling.commons.annotation.AbstractScheduledMethodConfiguration;
import org.digibooster.scheduling.commons.annotation.ScheduledMethodRegistrar;
import org.digibooster.scheduling.quartz.job.FixedDelayJobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class QuartzBasedScheduledMethodConfiguration extends AbstractScheduledMethodConfiguration {

    @Bean
    public FixedDelayJobListener fixedDelayJobListener(@Autowired SchedulerFactoryBean schedulerFactoryBean){
        return new FixedDelayJobListener(schedulerFactoryBean);
    }

    @Bean
    public ScheduledMethodRegistrar scheduledMethodRegistrar(@Autowired SchedulerFactoryBean schedulerFactoryBean,
                                                             @Autowired FixedDelayJobListener fixedDelayJobListener){
        return new QuartzBasedSchedulerMethodRegistrar(schedulerFactoryBean, fixedDelayJobListener);
    }
}
