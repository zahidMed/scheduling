package org.digibooster.scheduling.quartz.annotation;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import lombok.AllArgsConstructor;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.digibooster.scheduling.quartz.job.MethodInvokerJob;
import org.digibooster.scheduling.commons.util.JobUtils;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Date;

@AllArgsConstructor
public abstract class AbstractJobScheduler {

    public static final String JOB_GROUP = "SCHEDULED_GROUP";
    public static final String TARGET_METHOD_INFO = "TARGET_METHOD_INFO";

    protected final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));

    protected final SchedulerFactoryBean schedulerFactoryBean;

    public void schedule(Scheduler scheduler, ScheduledMethodInfo scheduledMethodInfo, boolean useInitDelay) throws SchedulerException {

        JobKey jobKey= JobKey.jobKey(JobUtils.getJobName(scheduledMethodInfo),JOB_GROUP);

        if(scheduler.checkExists(jobKey)){
            return;
        }
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(TARGET_METHOD_INFO, scheduledMethodInfo);
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName(),jobKey.getGroup())
                .usingJobData(dataMap);
        triggerBuilder.withSchedule(getScheduleBuilder(scheduledMethodInfo));

        if(useInitDelay && scheduledMethodInfo.getInitialDelay()>0){
            triggerBuilder.startAt(new Date(System.currentTimeMillis()+ scheduledMethodInfo.getInitialDelay()));
        }
        else{
            triggerBuilder.startNow();
        }

        JobDetail jobDetail = JobBuilder.newJob(MethodInvokerJob.class)
                .withIdentity(jobKey)
                .build();

        scheduler.scheduleJob(jobDetail,triggerBuilder.build());
    }

    protected ScheduleBuilder getScheduleBuilder(ScheduledMethodInfo info){
        switch (info.getScheduleType()){
            case CRON:
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
    }
}
