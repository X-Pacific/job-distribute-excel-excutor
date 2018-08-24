package org.zxp.jobexcutor.aop;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.ShardingUtil;
import org.springframework.beans.factory.BeanNameAware;

public interface ExcelDistributedReadIntf<T> extends BeanNameAware {
    /**
     * 协助分布式csv数据处理的接口，请必须实现此接口
     * @param shardingVO  当前分片信息，该对象由ExtJobHandler生成后传入
     * @param dealerCallBackInfo 回调信息，详见该类注释
     * @return
     */
    public ReturnT<String> deal(ShardingUtil.ShardingVO shardingVO, DealerCallBackInfo<T> dealerCallBackInfo);


}
