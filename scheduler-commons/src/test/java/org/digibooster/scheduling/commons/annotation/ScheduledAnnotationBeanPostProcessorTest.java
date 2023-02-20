package org.digibooster.scheduling.commons.annotation;

import org.digibooster.scheduling.commons.util.ScheduleType;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.junit.Test;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringValueResolver;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


public class ScheduledAnnotationBeanPostProcessorTest {

    private final StaticApplicationContext context = new StaticApplicationContext();

    @Test
    public void testProcessScheduledWithFixedRate(){

        BeanDefinitionBuilder processorDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(ScheduledAnnotationBeanPostProcessor.class);
        processorDefinitionBuilder.addConstructorArgValue(new ScheduledMethodRegistrar() {
            @Override
            public void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo) {

            }
        });

        BeanDefinition processorDefinition = processorDefinitionBuilder.getBeanDefinition();
        BeanDefinition targetDefinition = new RootBeanDefinition(FixedRatesBaseBean.class);
        context.registerBeanDefinition("postProcessor", processorDefinition);
        context.registerBeanDefinition("target", targetDefinition);
        context.refresh();

        ScheduledAnnotationBeanPostProcessor postProcessor = context.getBean("postProcessor", ScheduledAnnotationBeanPostProcessor.class);
        assertThat(postProcessor.getScheduledMethodInfoSet().size()).isEqualTo(3);

        List<ScheduledMethodInfo> scheduledMethodInfoSet=postProcessor.getScheduledMethodInfoSet();

        ScheduledMethodInfo info0= scheduledMethodInfoSet.get(0);
        assertThat(info0.getScheduleType()).isEqualTo(ScheduleType.FIXED_RATE);
        assertThat(info0.getFixedRate()).isEqualTo(4000L);
        assertThat(info0.getInitialDelay()).isEqualTo(0L);
        assertThat(info0.getBeanName()).isEqualTo("target");
        assertThat(info0.getTargetClass()).isEqualTo(FixedRatesBaseBean.class.getName());
        assertThat(info0.getMethod()).isEqualTo("fixedRate");

        ScheduledMethodInfo info1= scheduledMethodInfoSet.get(1);
        assertThat(info1.getScheduleType()).isEqualTo(ScheduleType.FIXED_RATE);
        assertThat(info1.getFixedRate()).isEqualTo(5000L);
        assertThat(info1.getInitialDelay()).isEqualTo(2000L);
        assertThat(info1.getBeanName()).isEqualTo("target");
        assertThat(info1.getTargetClass()).isEqualTo(FixedRatesBaseBean.class.getName());
        assertThat(info1.getMethod()).isEqualTo("fixedRate");

        ScheduledMethodInfo info3= scheduledMethodInfoSet.get(2);
        assertThat(info3.getScheduleType()).isEqualTo(ScheduleType.FIXED_RATE);
        assertThat(info3.getFixedRate()).isEqualTo(6000L);
        assertThat(info3.getInitialDelay()).isEqualTo(7000L);
        assertThat(info3.getBeanName()).isEqualTo("target");
        assertThat(info3.getTargetClass()).isEqualTo(FixedRatesBaseBean.class.getName());
        assertThat(info3.getMethod()).isEqualTo("fixedRate");
    }

    @Test
    public void testProcessScheduledWithFixedDelay(){

        BeanDefinitionBuilder processorDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(ScheduledAnnotationBeanPostProcessor.class);
        processorDefinitionBuilder.addConstructorArgValue(new ScheduledMethodRegistrar() {
            @Override
            public void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo) {

            }
        });

        BeanDefinition processorDefinition = processorDefinitionBuilder.getBeanDefinition();
        BeanDefinition targetDefinition = new RootBeanDefinition(FixedDelayBaseBean.class);
        context.registerBeanDefinition("postProcessor", processorDefinition);
        context.registerBeanDefinition("target", targetDefinition);
        context.refresh();

        ScheduledAnnotationBeanPostProcessor postProcessor = context.getBean("postProcessor", ScheduledAnnotationBeanPostProcessor.class);
        assertThat(postProcessor.getScheduledMethodInfoSet().size()).isEqualTo(3);

        List<ScheduledMethodInfo> scheduledMethodInfoSet=postProcessor.getScheduledMethodInfoSet();

        ScheduledMethodInfo info0= scheduledMethodInfoSet.get(0);
        assertThat(info0.getScheduleType()).isEqualTo(ScheduleType.FIXED_DELAY);
        assertThat(info0.getFixedDelay()).isEqualTo(1000L);
        assertThat(info0.getInitialDelay()).isEqualTo(0L);
        assertThat(info0.getBeanName()).isEqualTo("target");
        assertThat(info0.getTargetClass()).isEqualTo(FixedDelayBaseBean.class.getName());
        assertThat(info0.getMethod()).isEqualTo("fixedDelay");

        ScheduledMethodInfo info1= scheduledMethodInfoSet.get(1);
        assertThat(info1.getScheduleType()).isEqualTo(ScheduleType.FIXED_DELAY);
        assertThat(info1.getFixedDelay()).isEqualTo(2000L);
        assertThat(info1.getInitialDelay()).isEqualTo(3000L);
        assertThat(info1.getBeanName()).isEqualTo("target");
        assertThat(info1.getTargetClass()).isEqualTo(FixedDelayBaseBean.class.getName());
        assertThat(info1.getMethod()).isEqualTo("fixedDelay");

        ScheduledMethodInfo info2= scheduledMethodInfoSet.get(2);
        assertThat(info2.getScheduleType()).isEqualTo(ScheduleType.FIXED_DELAY);
        assertThat(info2.getFixedDelay()).isEqualTo(4000L);
        assertThat(info2.getInitialDelay()).isEqualTo(500L);
        assertThat(info2.getBeanName()).isEqualTo("target");
        assertThat(info2.getTargetClass()).isEqualTo(FixedDelayBaseBean.class.getName());
        assertThat(info2.getMethod()).isEqualTo("fixedDelay");
    }


    @Test
    public void testProcessScheduledWithCron(){

        BeanDefinitionBuilder processorDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(ScheduledAnnotationBeanPostProcessor.class);
        processorDefinitionBuilder.addConstructorArgValue(new ScheduledMethodRegistrar() {
            @Override
            public void schedule(Collection<ScheduledMethodInfo> scheduledMethodInfo) {

            }
        });

        BeanDefinition processorDefinition = processorDefinitionBuilder.getBeanDefinition();
        BeanDefinition targetDefinition = new RootBeanDefinition(CronBaseBean.class);
        context.registerBeanDefinition("postProcessor", processorDefinition);
        context.registerBeanDefinition("target", targetDefinition);
        context.refresh();

        ScheduledAnnotationBeanPostProcessor postProcessor = context.getBean("postProcessor", ScheduledAnnotationBeanPostProcessor.class);
        assertThat(postProcessor.getScheduledMethodInfoSet().size()).isEqualTo(2);

        List<ScheduledMethodInfo> scheduledMethodInfoSet=postProcessor.getScheduledMethodInfoSet();

        ScheduledMethodInfo info0= scheduledMethodInfoSet.get(0);
        assertThat(info0.getScheduleType()).isEqualTo(ScheduleType.CRON);
        assertThat(info0.getCron()).isEqualTo("0 15 10 15 * ?");
        assertThat(info0.getInitialDelay()).isEqualTo(0L);
        assertThat(info0.getBeanName()).isEqualTo("target");
        assertThat(info0.getTargetClass()).isEqualTo(CronBaseBean.class.getName());
        assertThat(info0.getMethod()).isEqualTo("fixedCron");


        ScheduledMethodInfo info2= scheduledMethodInfoSet.get(1);
        assertThat(info2.getScheduleType()).isEqualTo(ScheduleType.CRON);
        assertThat(info2.getCron()).isEqualTo("0 15 10 15 * ?");
        assertThat(info2.getInitialDelay()).isEqualTo(0L);
        assertThat(info2.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Africa/Casablanca"));
        assertThat(info2.getBeanName()).isEqualTo("target");
        assertThat(info2.getTargetClass()).isEqualTo(CronBaseBean.class.getName());
        assertThat(info2.getMethod()).isEqualTo("fixedCron");
    }

    static class FixedRatesBaseBean {

        @Scheduled(fixedRate = 4_000)
        @Scheduled(fixedRate = 5000, initialDelay = 2000)
        @Scheduled(fixedRateString = "6000", initialDelayString = "7000")
        void fixedRate() {
        }
    }

    static class FixedDelayBaseBean {
        @Scheduled(fixedDelay = 1_000)
        @Scheduled(fixedDelay = 2000, initialDelay = 3000)
        @Scheduled(fixedDelayString = "4000", initialDelayString = "500")
        void fixedDelay() {
        }
    }

    static class CronBaseBean {
        @Scheduled(cron = "0 15 10 15 * ?")
        @Scheduled(cron = "0 15 10 15 * ?", zone = "Africa/Casablanca")
        void fixedCron() {
        }
    }
}
