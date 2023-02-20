package org.digibooster.scheduling.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.digibooster.scheduling.commons.util.ScheduleType;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.digibooster.scheduling.quartz.annotation.AbstractJobScheduler;
import org.digibooster.scheduling.quartz.annotation.QuartzBasedSchedulerMethodRegistrar;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Slf4j
public class FixedDelayJobListener extends AbstractJobScheduler implements JobListener {

    public FixedDelayJobListener(SchedulerFactoryBean schedulerFactoryBean) {
        super(schedulerFactoryBean);
    }

    @Override
    public String getName() {
        return "fixedDelayJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    protected SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        ScheduledMethodInfo methodInformation = (ScheduledMethodInfo) context.getMergedJobDataMap().get(QuartzBasedSchedulerMethodRegistrar.TARGET_METHOD_INFO);
        if(ScheduleType.FIXED_RATE.equals(methodInformation.getScheduleType())){
            try {
                schedule(schedulerFactoryBean.getScheduler(),methodInformation,false);
            } catch (SchedulerException e) {
                log.error("Error while scheduling fixed delay task after the previous execution, info: {}",methodInformation,e);
            }
        }
    }
}
