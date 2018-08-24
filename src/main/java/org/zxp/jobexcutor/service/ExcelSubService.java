package org.zxp.jobexcutor.service;

import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;

import java.util.List;

public interface ExcelSubService {

    /**
     * 获得主键
     * @param key
     * @return
     */
    public Elastic_job_excel_sub getElastic_job_excel_sub(Elastic_job_excel_subKey key);

    /**
     * 根据uuid获得子任务列表
     * @param uuid
     * @return
     */
    public List<Elastic_job_excel_sub> getElastic_job_excel_subListByUuid(String uuid);

    /**
     * 根据文件列表组织子文件对象列表
     * @param mainUUID
     * @param subPath
     * @param subList
     * @return
     */
    public List<Elastic_job_excel_sub> genExcelSubList(String mainUUID,String subPath, List<String> subList);

    /**
     * 保存子文件列表
     * @param subList
     */
    public void saveAll(String uuid,List<Elastic_job_excel_sub> subList);


    /**
     * 更新
     * @param sub
     */
    public void update(Elastic_job_excel_sub sub);

    /**
     * 删除
     * @param uuid
     */
    public void delete(String uuid);


    /**
     * 根据uuid获取当前任务已经处理的记录和
     * @param uuid
     * @return
     */
    public long getSubSumTotal(String uuid);

    /**
     * 根据uuid已经子任务状态获取当前任务指定状态的个数
     * @param uuid
     * @param status
     * @return
     */
    public int getCountByStatus(String uuid,String status);


    /**
     * 转储uuid的子表数据
     * @param uuid
     */
    public void changeSub(String uuid);
}
