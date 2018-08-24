package org.zxp.jobexcutor.mapper;

import org.zxp.jobexcutor.entity.Elastic_job_excel_checkinfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Elastic_job_excel_checkinfoMapper {
    int deleteByPrimaryKey(String checkinfouuid);

    int insert(Elastic_job_excel_checkinfo record);

    int insertSelective(Elastic_job_excel_checkinfo record);

    Elastic_job_excel_checkinfo selectByPrimaryKey(String checkinfouuid);

    int updateByPrimaryKeySelective(Elastic_job_excel_checkinfo record);

    int updateByPrimaryKey(Elastic_job_excel_checkinfo record);

    @Select("SELECT * from elastic_job_excel_checkinfo WHERE mainuuid = #{uuid}")
    List<Elastic_job_excel_checkinfo> selectDealerCallBackErrorInfoListByUUID(@Param("uuid") String uuid);

    @Select("SELECT * from elastic_job_excel_checkinfo WHERE mainuuid = #{uuid} and serialno = #{serialno} and checkcode = #{checkcode}")
    Elastic_job_excel_checkinfo selectDealerCallBackErrorInfo(@Param("uuid") String uuid,
                                                              @Param("serialno") String serialno,
                                                              @Param("checkcode") String checkcode);

    @Select("select * from (select checkcode,listagg(errorinfo,'|') within group (order by  inputdate desc) as errorinfo from ELASTIC_JOB_EXCEL_CHECKINFO where mainuuid = #{uuid} and serialno = #{serialno} group by checkcode ) where rownum = 1")
    Elastic_job_excel_checkinfo selectNewestDealerCallBackErrorInfo(@Param("uuid") String uuid,
                                                                    @Param("serialno") String serialno);

    @Select("SELECT * from elastic_job_excel_checkinfo WHERE checkcode = #{checkcode}")
    List<Elastic_job_excel_checkinfo> selectDealerCallBackErrorInfoListByCheckcode(@Param("checkcode") String checkcode);

}