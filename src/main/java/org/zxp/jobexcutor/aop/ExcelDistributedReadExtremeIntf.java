package org.zxp.jobexcutor.aop;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.ShardingUtil;

import java.util.List;

public interface ExcelDistributedReadExtremeIntf<T> {
    String errorInfo="";//错误原因

    /**
     *
     * 协助分布式csv数据处理的接口，请必须实现此接口
     * 基于ExcelDistributedReadIntf优化，省去两个参数
     * @param shardingVO
     * @param csvDataList
     * @param errorInfo
     * @return
     */
    public ReturnT<String> deal(ShardingUtil.ShardingVO shardingVO, List<T> csvDataList, String errorInfo);
}
