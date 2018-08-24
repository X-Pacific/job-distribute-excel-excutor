package org.zxp.jobexcutor.service;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;

import java.util.List;

public interface ExcelMainService {

    /**
     * 设置错误原因并保存
     * @param uuid
     * @param errorInfo
     */
    public void setErrorInfoAndSave(String uuid,String errorInfo);
    /**
     * 获取一个可被拆分excel的任务
     * @return
     */
    public Elastic_job_excel_main getElastic_job_excel_main(String uuid);


    /**
     * 获取一个可被拆分excel的任务
     * @return
     */
    public Elastic_job_excel_main findOneSplitExcelJob();

    /**
     * 获取一个可被拆分excel的任务（包含查询条件）
     * @param record
     * @return
     */
    public Elastic_job_excel_main findOneSplitExcelJobByType(Elastic_job_excel_main record);

    /**
     * 更新任务
     * @param record
     */
    public int update(Elastic_job_excel_main record);

    /**
     * 更新任务
     * @param record
     */
    public int updateByLock(Elastic_job_excel_main record);

    /**
     * 获取一个可被拆分excel的任务(并置状态)
     * @return
     */
    public Elastic_job_excel_main fetchOneSplitExcelJob();

    /**
     * 获取一个可被拆分excel的任务（包含查询条件）(并置状态)
     * @param record
     * @return
     */
    public Elastic_job_excel_main fetchOneSplitExcelJobByType(Elastic_job_excel_main record);


    /**
     * 更新主表进度
     * @param uuid
     * @param subList
     */
    public void updateRopMain(String uuid,List<Elastic_job_excel_sub> subList);

    /**
     * 获取一个可处理的excel的任务（包含查询条件）(并置状态)
     * 优先获取处理中的任务
     * @param record
     * @return
     */
    public Elastic_job_excel_main fetchOneReadExcelJobByCondtion(Elastic_job_excel_main record) ;


    /**
     * (非分布式)获取一个可处理的excel的任务（包含查询条件）(并置状态)
     * 优先获取处理中的任务
     * @param record
     * @return
     */
    public Elastic_job_excel_main findOneReadExcelJobByTypeOp(Elastic_job_excel_main record) ;

    /**
     * (非分布式)获取一个可处理的excel的任务（包含查询条件）(并置状态)
     * 优先获取处理中的任务
     * @param record
     * @return
     */
    public Elastic_job_excel_main fetchOneReadExcelJobByCondtion_notdis(Elastic_job_excel_main record) ;



    /**
     * 检查当前主任务是否已经完成，并回写状态
     * @param uuid
     * @param isCheckSumAmount 是否检查主子表总数一致性
     */
    public boolean checkIsFinishAndWriteStatus(String uuid,boolean isCheckSumAmount);


    /**
     * 根据条件获取列表
     * @param record
     * @return
     */
    public List<Elastic_job_excel_main> getMainList(Elastic_job_excel_main record);
}
