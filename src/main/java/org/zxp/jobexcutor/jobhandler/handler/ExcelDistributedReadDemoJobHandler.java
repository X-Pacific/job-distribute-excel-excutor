package org.zxp.jobexcutor.jobhandler.handler;

import org.zxp.jobexcutor.aop.DealerCallBackInfo;
import org.zxp.jobexcutor.aop.ExcelDistributedCallBackIntf;
import org.zxp.jobexcutor.aop.ExcelDistributedReadIntf;
import org.zxp.jobexcutor.csvdto.DemoJobDto;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.util.ShardingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  这是分布式读取（处理）excel（csv）数据的入口例子
 *  1、必须继承IJobHandler
 *  2、需要调用ExcelDistributedReadIntf的实现类来实现业务处理
 *  3、可以（不是必须）在这里指定所有任务执行完成后的回调任务（实现ExcelDistributedCallBackIntf接口）
 *  4、ExcelDistributedReadIntf.deal方法入参shardingVO 在定时任务运行时必须为：ShardingUtil.ShardingVO shardingVO = new ShardingUtil.ShardingVO(1,4);才可正常获取分片参数
 *  5、ExcelDistributedReadIntf.deal方法入参dealerCallBackInfo 请在本类中就进行实例化，框架会自动对这个类的成员变量进行赋值，dealerCallBackInfo对象中的内容意义请参照这个类的注释
 */
@JobHandler(value="excelDistributedReadDemoJobHandler")
@Component
public class ExcelDistributedReadDemoJobHandler extends IJobHandler {
    private final static Logger logger = LoggerFactory.getLogger(ExcelDistributedReadDemoJobHandler.class);
    @Autowired
    ExcelDistributedReadIntf<DemoJobDto> excelDistributedReadDemoJobDealer;
    @Autowired
    ExcelDistributedCallBackIntf excelDistributeReadDemoCallBack;

    @Override
    public ReturnT<String> execute(String s) {
        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
//        ShardingUtil.ShardingVO shardingVO = new ShardingUtil.ShardingVO(2,4);
        DealerCallBackInfo<DemoJobDto> dealerCallBackInfo = new DealerCallBackInfo<DemoJobDto>();
        //回调也可以用下面的代码设置
        //dealerCallBackInfo.setExcelDistributedCallBackIntf(excelDistributeReadDemoCallBack);
        return excelDistributedReadDemoJobDealer.deal(shardingVO,dealerCallBackInfo);
    }
}
