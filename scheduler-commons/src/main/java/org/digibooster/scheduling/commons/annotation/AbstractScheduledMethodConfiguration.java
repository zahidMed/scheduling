package org.digibooster.scheduling.commons.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringValueResolver;

public class AbstractScheduledMethodConfiguration {


    @Bean
    public ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor(@Autowired StringValueResolver embeddedValueResolver,
                                                                                     @Autowired ScheduledMethodRegistrar registrar){
        return new ScheduledAnnotationBeanPostProcessor(registrar);
    }

    @Bean
    public ScheduledMethodInvoker scheduledMethodInvoker(@Autowired ApplicationContext applicationContext){
        return new DefaultScheduledMethodInvoker(applicationContext);
    }
}
