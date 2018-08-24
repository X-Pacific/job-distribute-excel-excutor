package org.zxp.jobexcutor.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExcelRead {
    String type();//定时任务类型
    Class clazz();//返回的csv对应的类类型
}
