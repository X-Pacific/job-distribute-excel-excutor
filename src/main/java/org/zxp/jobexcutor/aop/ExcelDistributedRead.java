package org.zxp.jobexcutor.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式处理excel 注解设置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExcelDistributedRead {
    String type();//定时任务类型
    boolean isCheckSumAmount() default true;//是否检查主子一致性 主、子表total字段自动赋值
    Class clazz();//返回的csv对应的类类型
    String callBackBeanName() default "";//回调类的bean名字，注意，必须实现ExcelDistributedCallBackIntf接口
}
