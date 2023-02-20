package org.digibooster.scheduling.quartz.annotation;

import lombok.extern.slf4j.Slf4j;
import org.digibooster.scheduling.commons.annotation.ScheduledMethodRegistrar;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.digibooster.scheduling.quartz.job.FixedDelayJobListener;
import org.digibooster.scheduling.commons.util.JobUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class QuartzBasedSchedulerMethodRegistrar extends AbstractJobScheduler implements ScheduledMethodRegistrar {

    protected final FixedDelayJobListener fixedDelayJobListener;

    public QuartzBasedSchedulerMethodRegistrar(SchedulerFactoryBean schedulerFactoryBean,
                                               FixedDelayJobListener fixedDelayJobListener) {
        super(schedulerFactoryBean);
        this.fixedDelayJobListener = fixedDelayJobListener;
    }

    @Override
    public void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo) {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            scheduler.getListenerManager().addJobListener(fixedDelayJobListener, GroupMatcher.jobGroupEquals(JOB_GROUP));
        } catch (SchedulerException e) {
            log.error("Could not add job listener",e);
            throw new RuntimeException(e);
        }

        try {
            cleanJobs(scheduler,scheduledMethodInfo);
        } catch (SchedulerException e) {
            log.error("Could not clean unused scheduled methods",e);
            throw new RuntimeException(e);
        }

        for(ScheduledMethodInfo info: scheduledMethodInfo){
            try {
                schedule(scheduler,info,true);
            } catch (SchedulerException e) {
                log.error("Could not schedule method with info: {}",info, e);
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * clean jobs that are not listed in the method info set
     * @param scheduler
     * @param scheduledMethodInfo
     */
    protected void cleanJobs(Scheduler scheduler, Collection<ScheduledMethodInfo> scheduledMethodInfo) throws SchedulerException {
        Set<JobKey> tmpJobKeys= scheduledMethodInfo
                .stream()
                .map(info-> JobKey.jobKey(JobUtils.getJobName(info),JOB_GROUP))
                .collect(Collectors.toSet());


        Set<JobKey> jobKeys= scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP));

        for(JobKey jobKey: jobKeys){
            if(!tmpJobKeys.contains(jobKey))
                scheduler.deleteJob(jobKey);
        }
    }


}
