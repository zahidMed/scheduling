package org.digibooster.scheduling.commons.util;

import org.digibooster.scheduling.commons.util.ScheduledMethodInfo;
import org.springframework.util.StringUtils;

public class JobUtils {

    public static String getJobName(ScheduledMethodInfo methodInfo){
        return new StringBuffer(methodInfo.getScheduleType().name())
                .append("_")
                .append(methodInfo.getBeanName().hashCode())
                .append("_")
                .append(methodInfo.getTargetClass().hashCode())
                .append("_")
                .append(methodInfo.getMethod().hashCode())
                .append("_")
                .append(StringUtils.hasText(methodInfo.getCron())?methodInfo.getCron().hashCode():"")
                .append("_")
                .append(methodInfo.getTimeZone()!=null?methodInfo.getTimeZone().hashCode():"")
                .append("_")
                .append(methodInfo.getInitialDelay())
                .append("_")
                .append(methodInfo.getFixedDelay())
                .append("_")
                .append(methodInfo.getFixedRate())
                .toString();
    }
}
