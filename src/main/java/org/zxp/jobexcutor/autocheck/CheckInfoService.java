package org.zxp.jobexcutor.autocheck;

import org.zxp.jobexcutor.aop.DealerCallBackErrorInfo;

import java.util.List;

public interface CheckInfoService {
    /**
     * 保存全部异常信息
     * @param list
     */
    public void saveAll(String uuid,long serialno,List<DealerCallBackErrorInfo> list);

    /**
     * 获取主任务uuid相关的异常
     * @param uuid
     * @return
     */
    public List<DealerCallBackErrorInfo> getDealerCallBackErrorInfoListByUUID(String uuid);

    /**
     * 获取异常主键对应的异常
     * @param uuid
     * @param serialno
     * @param checkcode
     * @return
     */
    public DealerCallBackErrorInfo getDealerCallBackErrorInfo(String uuid,String serialno,String checkcode);

    /**
     * 根据sub表主键获取最新的异常信息
     * @param uuid
     * @param serialno
     * @return
     */
    public String getNewestDealerCallBackErrorInfo(String uuid,String serialno);

    /**
     * 根据匹配码获取异常列表
     * @param checkcode
     * @return
     */
    public List<DealerCallBackErrorInfo> getDealerCallBackErrorInfoListByCheckcode(String checkcode);

}
