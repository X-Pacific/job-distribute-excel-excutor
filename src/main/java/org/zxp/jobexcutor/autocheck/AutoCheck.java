package org.zxp.jobexcutor.autocheck;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 如果pojo加上此注解，则自动校验
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCheck {
    boolean value() default true;
}
