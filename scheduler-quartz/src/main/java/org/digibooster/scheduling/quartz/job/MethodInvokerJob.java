package org.digibooster.scheduling.quartz.job;

import org.digibooster.scheduling.commons.annotation.ScheduledMethodInvoker;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.digibooster.scheduling.quartz.annotation.QuartzBasedSchedulerMethodRegistrar;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class MethodInvokerJob implements Job {

    @Autowired
    protected ScheduledMethodInvoker invoker;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        ScheduledMethodInfo methodInformation = (ScheduledMethodInfo) jobExecutionContext.getMergedJobDataMap().get(QuartzBasedSchedulerMethodRegistrar.TARGET_METHOD_INFO);
        invoker.invoke(methodInformation);
    }
}
