package org.zxp.jobexcutor.aop;

import com.xxl.job.core.biz.model.ReturnT;

public interface ExcelReadIntf<T> {
    /**
     * 非分布式excel数据处理的接口
     * @param dealerCallBackInfo 回调信息，详见该类注释
     * @return
     */
    public ReturnT<String> deal(DealerCallBackInfo<T> dealerCallBackInfo);

}
