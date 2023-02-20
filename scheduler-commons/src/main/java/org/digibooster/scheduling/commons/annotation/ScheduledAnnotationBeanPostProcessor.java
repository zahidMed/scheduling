package org.digibooster.scheduling.commons.annotation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.digibooster.scheduling.commons.util.ScheduleType;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
public class ScheduledAnnotationBeanPostProcessor implements BeanPostProcessor,
        ApplicationListener<ContextRefreshedEvent>,
        ApplicationContextAware,
        EmbeddedValueResolverAware {

    protected StringValueResolver embeddedValueResolver;

    protected ScheduledMethodRegistrar registrar;

    public ScheduledAnnotationBeanPostProcessor(ScheduledMethodRegistrar registrar){
        this.registrar=registrar;
    }

    private ApplicationContext applicationContext;

    protected final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    @Getter
    protected final List<ScheduledMethodInfo> scheduledMethodInfoSet = Collections.synchronizedList(new ArrayList<>());


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AopInfrastructureBean ) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass) &&
                AnnotationUtils.isCandidateClass(targetClass, Arrays.asList(Scheduled.class, Schedules.class))) {
            Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<Set<Scheduled>>) method -> {
                        Set<Scheduled> scheduledMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                                method, Scheduled.class, Schedules.class);
                        return (!scheduledMethods.isEmpty() ? scheduledMethods : null);
                    });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
                //log.trace("No @Scheduled annotations found on bean class: " + targetClass);

            }
            else {
                // Non-empty set of methods
                annotatedMethods.forEach((method, scheduledMethods) ->
                        scheduledMethods.forEach(scheduled -> processScheduled(scheduled, method, targetClass, beanName)));
                //log.trace(annotatedMethods.size() + " @Scheduled methods processed on bean '" + beanName +
                //            "': " + annotatedMethods);
            }
        }
        return bean;
    }


    protected void processScheduled(Scheduled scheduled, Method method, Class<?> targetClass, String beanName) {
        try {
            boolean processedSchedule = false;
            String errorMessage =
                    "Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";

            // Determine initial delay
            long initialDelay = scheduled.initialDelay();
            String initialDelayString = scheduled.initialDelayString();
            if (StringUtils.hasText(initialDelayString)) {
                Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both");
                if (this.embeddedValueResolver != null) {
                    initialDelayString = this.embeddedValueResolver.resolveStringValue(initialDelayString);
                }
                if (StringUtils.hasLength(initialDelayString)) {
                    try {
                        initialDelay = parseDelayAsLong(initialDelayString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into long");
                    }
                }
            }

            // Check cron expression
            String cron = scheduled.cron();
            if (StringUtils.hasText(cron)) {
                String zone = scheduled.zone();
                if (this.embeddedValueResolver != null) {
                    cron = this.embeddedValueResolver.resolveStringValue(cron);
                    zone = this.embeddedValueResolver.resolveStringValue(zone);
                }
                if (StringUtils.hasLength(cron)) {
                    Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
                    processedSchedule = true;
                    if (!Scheduled.CRON_DISABLED.equals(cron)) {
                        TimeZone timeZone;
                        if (StringUtils.hasText(zone)) {
                            timeZone = StringUtils.parseTimeZoneString(zone);
                        }
                        else {
                            timeZone = TimeZone.getDefault();
                        }
                        scheduledMethodInfoSet.add(ScheduledMethodInfo
                                .builder()
                                        .scheduleType(ScheduleType.CRON)
                                        .beanName(beanName)
                                        .targetClass(targetClass.getName())
                                        .method(method.getName())
                                        .cron(cron)
                                        .timeZone(timeZone)
                                .build());
                        //tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
                    }
                }
            }

            // At this point we don't need to differentiate between initial delay set or not anymore
            if (initialDelay < 0) {
                initialDelay = 0;
            }

            // Check fixed delay
            long fixedDelay = scheduled.fixedDelay();
            if (fixedDelay >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;

                scheduledMethodInfoSet.add(ScheduledMethodInfo
                        .builder()
                        .scheduleType(ScheduleType.FIXED_DELAY)
                        .beanName(beanName)
                        .targetClass(targetClass.getName())
                        .method(method.getName())
                        .fixedDelay(fixedDelay)
                        .initialDelay(initialDelay)
                        .build());
                //tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
            }
            String fixedDelayString = scheduled.fixedDelayString();
            if (StringUtils.hasText(fixedDelayString)) {
                if (this.embeddedValueResolver != null) {
                    fixedDelayString = this.embeddedValueResolver.resolveStringValue(fixedDelayString);
                }
                if (StringUtils.hasLength(fixedDelayString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedDelay = parseDelayAsLong(fixedDelayString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedDelayString value \"" + fixedDelayString + "\" - cannot parse into long");
                    }
                    scheduledMethodInfoSet.add(ScheduledMethodInfo
                            .builder()
                            .scheduleType(ScheduleType.FIXED_DELAY)
                            .beanName(beanName)
                            .targetClass(targetClass.getName())
                            .method(method.getName())
                            .fixedDelay(fixedDelay)
                            .initialDelay(initialDelay)
                            .build());
                    //tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
                }
            }

            // Check fixed rate
            long fixedRate = scheduled.fixedRate();
            if (fixedRate >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;

                scheduledMethodInfoSet.add(ScheduledMethodInfo
                        .builder()
                        .scheduleType(ScheduleType.FIXED_RATE)
                        .beanName(beanName)
                        .targetClass(targetClass.getName())
                        .method(method.getName())
                        .fixedRate(fixedRate)
                        .initialDelay(initialDelay)
                        .build());
                //tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
            }
            String fixedRateString = scheduled.fixedRateString();
            if (StringUtils.hasText(fixedRateString)) {
                if (this.embeddedValueResolver != null) {
                    fixedRateString = this.embeddedValueResolver.resolveStringValue(fixedRateString);
                }
                if (StringUtils.hasLength(fixedRateString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedRate = parseDelayAsLong(fixedRateString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
                    }

                    scheduledMethodInfoSet.add(ScheduledMethodInfo
                            .builder()
                            .scheduleType(ScheduleType.FIXED_RATE)
                            .beanName(beanName)
                            .targetClass(targetClass.getName())
                            .method(method.getName())
                            .fixedRate(fixedRate)
                            .initialDelay(initialDelay)
                            .build());
                    //tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
                }
            }

            // Check whether we had any attribute set
            Assert.isTrue(processedSchedule, errorMessage);

        }
        catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() == this.applicationContext) {
            // Running in an ApplicationContext -> register tasks this late...
            // giving other ContextRefreshedEvent listeners a chance to perform
            // their work at the same time (e.g. Spring Batch's job registration).
            registrar.schedule(scheduledMethodInfoSet);
        }
    }

    private static long parseDelayAsLong(String value) throws RuntimeException {
        if (value.length() > 1 && (isP(value.charAt(0)) || isP(value.charAt(1)))) {
            return Duration.parse(value).toMillis();
        }
        return Long.parseLong(value);
    }

    private static boolean isP(char ch) {
        return (ch == 'P' || ch == 'p');
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        this.embeddedValueResolver=embeddedValueResolver;
    }
}
