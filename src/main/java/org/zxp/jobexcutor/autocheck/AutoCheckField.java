package org.zxp.jobexcutor.autocheck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/***
 * 自动校验注解
 */
public @interface AutoCheckField {
    //如果字段上被此字段修饰，则被校验内容前缀会显示被注解的字段 显示（pojo字段名或CsvHead注解）
    boolean disUniq()default false;
    //字段校验配置注解
    boolean notNull()default false;//是否为空的注解配置 空不校验 true不允许为空
    String select()default "";//选择范围注解配置 空不校验 由于本框架限定，请配置时配置为 ,1,2,3,
    boolean isNotZero()default false;//是否不能为0 空不校验 true不允许为0
    AutoCheckFormat fieldformat()default AutoCheckFormat.none;//格式校验配置 空不校验 date"YYYY-MM-DD" time"YYYY-MM-DD 00:00:00" num"数字类型"
    int length()default -1;//长度校验 为0时该项不可录入值 空不校验
    //中文翻译
    String cname()default "";//字段默认汉字 校验返回使用
}
