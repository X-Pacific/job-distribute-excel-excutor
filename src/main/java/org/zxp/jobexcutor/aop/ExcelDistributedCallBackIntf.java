package org.zxp.jobexcutor.aop;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;

/**
 * 分布式处理excel回调方法
 */
public interface ExcelDistributedCallBackIntf {

    public void callBack(Elastic_job_excel_main main);
}
