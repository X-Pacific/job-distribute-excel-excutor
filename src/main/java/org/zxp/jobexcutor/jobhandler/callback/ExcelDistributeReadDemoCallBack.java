package org.zxp.jobexcutor.jobhandler.callback;

import org.zxp.jobexcutor.aop.ExcelDistributedCallBackIntf;
import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.springframework.stereotype.Component;

/**
 * 回调类必须指定名字
 */
@Component("excelDistributeReadDemoCallBack")
public class ExcelDistributeReadDemoCallBack implements ExcelDistributedCallBackIntf {
    @Override
    public void callBack(Elastic_job_excel_main main) {
        System.out.println("我是回调哦["+main.getUuid()+"]");
    }
}
