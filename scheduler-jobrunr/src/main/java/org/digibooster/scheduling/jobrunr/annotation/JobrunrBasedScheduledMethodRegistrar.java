package org.digibooster.scheduling.jobrunr.annotation;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import org.digibooster.scheduling.commons.annotation.ScheduledMethodRegistrar;
import org.digibooster.scheduling.commons.util.JobUtils;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.PageRequest;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobrunrBasedScheduledMethodRegistrar implements ScheduledMethodRegistrar {

    public static final String JOB_GROUP = "SCHEDULED_GROUP";

    private JobScheduler jobScheduler;

    private StorageProvider storageProvider;
    @Override
    public void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo) {

        cleanJobs(storageProvider,scheduledMethodInfo);
        for(ScheduledMethodInfo info: scheduledMethodInfo){

        }
        storageProvider.getJobById()
        jobScheduler.scheduleRecurrently()
    }

    protected void schedule(ScheduledMethodInfo info){

        switch (info.getScheduleType()){
            case CRON:
                jobScheduler.scheduleRecurrently(JobUtils.getJobName(info),info.getTimeZone(),info.getCron(),)
                Cron springCron = parser.parse(info.getCron());
                CronMapper cronMapper = CronMapper.fromSpringToQuartz();
                Cron quartzCron= cronMapper.map(springCron);
                return CronScheduleBuilder.cronSchedule(quartzCron.asString())
                        .inTimeZone(info.getTimeZone());
            case FIXED_DELAY:
                return SimpleScheduleBuilder
                        .repeatSecondlyForever((int)info.getFixedDelay()/1000)
                        .withIntervalInMilliseconds(info.getInitialDelay());
            default:
                return SimpleScheduleBuilder
                        .repeatSecondlyForTotalCount(1,(int)info.getFixedDelay()/1000)
                        .withIntervalInMilliseconds(info.getInitialDelay());
    }

    protected void cleanJobs(StorageProvider storageProvider, Collection<ScheduledMethodInfo> scheduledMethodInfo){
        Set<String> ids= scheduledMethodInfo
                .stream()
                .map(info-> JOB_GROUP+JobUtils.getJobName(info))
                .collect(Collectors.toSet());

        RecurringJobsResult recurringJobs= storageProvider.getRecurringJobs();

        recurringJobs.forEach(rj->{
            if(ids.contains(rj.getId()))
                jobScheduler.delete(rj.getId());
        });

    }
}
