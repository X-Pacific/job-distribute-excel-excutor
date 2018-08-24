package org.zxp.jobexcutor.mapper;

import org.zxp.jobexcutor.entity.Elastic_job_excel_sub_his;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub_hisKey;

public interface Elastic_job_excel_sub_hisMapper {
    int deleteByPrimaryKey(Elastic_job_excel_sub_hisKey key);

    int insert(Elastic_job_excel_sub_his record);

    int insertSelective(Elastic_job_excel_sub_his record);

    Elastic_job_excel_sub_his selectByPrimaryKey(Elastic_job_excel_sub_hisKey key);

    int updateByPrimaryKeySelective(Elastic_job_excel_sub_his record);

    int updateByPrimaryKey(Elastic_job_excel_sub_his record);
}