package org.digibooster.scheduling.commons.annotation;

import lombok.Getter;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;


import static org.assertj.core.api.Assertions.assertThat;

public class DefaultScheduledMethodInvokerTest {

    private final StaticApplicationContext context = new StaticApplicationContext();

    @Test
    public void testInvoke(){

        BeanDefinitionBuilder scheduledMethodInvokerDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(DefaultScheduledMethodInvoker.class);
        scheduledMethodInvokerDefinitionBuilder.addConstructorArgValue(context);

        BeanDefinition scheduledMethodInvokerDefinition = scheduledMethodInvokerDefinitionBuilder.getBeanDefinition();
        BeanDefinition targetDefinition = new RootBeanDefinition(TargetBean.class);
        context.registerBeanDefinition("scheduledMethodInvoker", scheduledMethodInvokerDefinition);
        context.registerBeanDefinition("target", targetDefinition);
        context.refresh();

        ScheduledMethodInfo info= ScheduledMethodInfo
                .builder()
                .method("targetMethod")
                .beanName("target")
                .targetClass(TargetBean.class.getName())
                .build();

        DefaultScheduledMethodInvoker scheduledMethodInvoker = context.getBean("scheduledMethodInvoker", DefaultScheduledMethodInvoker.class);
        TargetBean target = context.getBean("target", TargetBean.class);

        assertThat(target.isInvoked()).isEqualTo(false);
        scheduledMethodInvoker.invoke(info);
        assertThat(target.isInvoked()).isEqualTo(true);
    }

    static class TargetBean{

        @Getter
        private boolean invoked=false;

        public void targetMethod(){
            this.invoked=true;
        }
    }
}
