package org.zxp.jobexcutor.mapper;

import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Elastic_job_excel_subMapper {
    int deleteByPrimaryKey(Elastic_job_excel_subKey key);

    int insert(Elastic_job_excel_sub record);

    int insertSelective(Elastic_job_excel_sub record);

    Elastic_job_excel_sub selectByPrimaryKey(Elastic_job_excel_subKey key);

    int updateByPrimaryKeySelective(Elastic_job_excel_sub record);

    int updateByPrimaryKey(Elastic_job_excel_sub record);

    @Delete("DELETE FROM ELASTIC_JOB_EXCEL_SUB WHERE uuid = #{uuid}")
    int deleteByUUID(@Param("uuid") String uuid);

    List<Elastic_job_excel_sub> selectByUuid(@Param("uuid") String uuid);

    @Select("SELECT SUM(total)  from ELASTIC_JOB_EXCEL_SUB WHERE uuid = #{uuid} and status in ('2','3')")
    long selectSumTotal(@Param("uuid") String uuid);


    @Select("SELECT count(1)  from ELASTIC_JOB_EXCEL_SUB WHERE uuid = #{uuid} and status = #{status}")
    int selectCountByStatus(@Param("uuid") String uuid,@Param("status") String status);
}