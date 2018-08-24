package org.zxp.jobexcutor.mapper;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Elastic_job_excel_mainMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(Elastic_job_excel_main record);

    int insertSelective(Elastic_job_excel_main record);

    Elastic_job_excel_main selectByPrimaryKey(String uuid);

    @Select("SELECT * from ELASTIC_JOB_EXCEL_MAIN WHERE uuid = #{uuid} FOR UPDATE")
    Elastic_job_excel_main selectByPrimaryKeyForUpdate(@Param("uuid") String uuid);

    int updateByPrimaryKeySelective(Elastic_job_excel_main record);

    int updateByPrimaryKey(Elastic_job_excel_main record);

    /**
     * 更新未被锁本次锁成功 返回更新数量
     * @param record
     * @return
     */
    int updateByLock(Elastic_job_excel_main record);

    /**
     * 获取一个可被拆分excel的任务
     * @return
     */
    Elastic_job_excel_main findOneSplitExcelJob();

    /**
     * 获取一个可被拆分excel的任务（包含查询条件）
     * @param record
     * @return
     */
    Elastic_job_excel_main findOneSplitExcelJobByType(Elastic_job_excel_main record);

    /**
     * 获取一个可处理(已经处理中)excel的任务（包含查询条件），按照inputdate升序排序
     * @param record
     * @return
     */
    Elastic_job_excel_main findOneReadExcelJobByType20(Elastic_job_excel_main record);

    /**
     * 获取一个可处理(未开始处理)excel的任务（包含查询条件），按照inputdate升序排序
     * @param record
     * @return
     */
    Elastic_job_excel_main findOneReadExcelJobByType1(Elastic_job_excel_main record);


    /**
     * 获取一个可处理(未开始处理)excel的任务（包含查询条件），按照inputdate升序排序，非分布式
     * @param record
     * @return
     */
    Elastic_job_excel_main findOneReadExcelJobByType0_notdis(Elastic_job_excel_main record);

    /**
     * 获取一个指定uuid的主任务，并且状态是失败或进行中
     * @param record
     * @return
     */
    Elastic_job_excel_main findOneReadExcelJobByTypeOp(Elastic_job_excel_main record);


    /**
     * 获取指定条件的主任务列表，限定为分布式
     * @param record
     * @return
     */
    List<Elastic_job_excel_main> selectMainList(Elastic_job_excel_main record);

}