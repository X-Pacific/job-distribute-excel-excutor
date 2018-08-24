package org.zxp.jobexcutor.jobhandler.dealer;

import org.zxp.jobexcutor.aop.DealerCallBackInfo;
import org.zxp.jobexcutor.aop.ExcelRead;
import org.zxp.jobexcutor.aop.ExcelReadIntf;
import org.zxp.jobexcutor.csvdto.DemoJobDto;
import org.zxp.jobexcutor.entity.PrpCMainDemo;
import org.zxp.jobexcutor.service.DemoJobService;
import org.zxp.jobexcutor.util.ExcelJobType;
import org.zxp.jobexcutor.util.JobConstant;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("excelReadDemoJobDealer")
public class ExcelReadDemoJobDealer  implements ExcelReadIntf {
    private final static Logger logger = LoggerFactory.getLogger(ExcelReadDemoJobDealer.class);
    @Autowired
    private DemoJobService demoJobService;

    @Override
    @ExcelRead(type = ExcelJobType.NDEMO, clazz = DemoJobDto.class)
    public ReturnT<String> deal(DealerCallBackInfo dealerCallBackInfo) {
        List<DemoJobDto> csvList = dealerCallBackInfo.getCsvList();
        List<PrpCMainDemo> prpCMainDemos = new ArrayList<PrpCMainDemo>();
        for (int i = 0; csvList != null && i < csvList.size(); i++) {
            DemoJobDto dto = csvList.get(i);
            PrpCMainDemo prpCMainDemo = new PrpCMainDemo();
            BeanUtils.copyProperties(dto,prpCMainDemo);
            prpCMainDemos.add(prpCMainDemo);
        }
        try {
            demoJobService.saveAll(prpCMainDemos);
        } catch (Exception e) {
            //如果此处捕获异常，可以通过如下方式处理，否则请将异常抛出，框架自动处理
            //打印异常方式必须为：JobConstant.CSV_AOP_A1（阶段名称） + |UUID=?（有就显示）+ |需要打印的内容
            String errorInfo = JobConstant.CSV_CUSTOM + "|UUID=" + dealerCallBackInfo.getUuid() + "|保存文件“"+dealerCallBackInfo.getHitFileName()+"”异常："+e.getMessage();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            logger.error(errorInfo,e);
        }
        return ReturnT.SUCCESS;
    }
}
