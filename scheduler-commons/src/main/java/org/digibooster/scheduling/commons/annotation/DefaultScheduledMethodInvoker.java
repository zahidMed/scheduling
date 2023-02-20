package org.digibooster.scheduling.commons.annotation;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@AllArgsConstructor
public class DefaultScheduledMethodInvoker implements ScheduledMethodInvoker {

    protected final ApplicationContext applicationContext;

    @Override
    public void invoke(ScheduledMethodInfo scheduledMethodInfo){

        Object bean = applicationContext.getBean(scheduledMethodInfo.getBeanName());
        Assert.notNull(bean,"No bean found for class: "+scheduledMethodInfo.getBeanName());

        MethodInvoker methodInvoker = new MethodInvoker();
        methodInvoker.setTargetObject(bean);
        methodInvoker.setTargetMethod(scheduledMethodInfo.getMethod());
        try {
            methodInvoker.prepare();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.error("Error while preparing method invoker for : {}::{}",scheduledMethodInfo.getBeanName(),scheduledMethodInfo.getMethod(),e);
            return;
        }

        try {
            methodInvoker.invoke();
        } catch (InvocationTargetException | IllegalAccessException e){
            log.error("Error while invoking method: {}::{}",scheduledMethodInfo.getBeanName(),scheduledMethodInfo.getMethod(),e);
        }
    }
}
