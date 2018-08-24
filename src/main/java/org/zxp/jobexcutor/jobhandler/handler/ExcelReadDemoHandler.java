package org.zxp.jobexcutor.jobhandler.handler;

import org.zxp.jobexcutor.aop.DealerCallBackInfo;
import org.zxp.jobexcutor.aop.ExcelReadIntf;
import org.zxp.jobexcutor.csvdto.DemoJobDto;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JobHandler(value="excelReadDemoHandler")
@Component
public class ExcelReadDemoHandler extends IJobHandler {
    @Autowired
    ExcelReadIntf<DemoJobDto> excelReadDemoJobDealer;

    @Override
    public ReturnT<String> execute(String s) {
        return excelReadDemoJobDealer.deal(new DealerCallBackInfo<DemoJobDto>());
    }
}
