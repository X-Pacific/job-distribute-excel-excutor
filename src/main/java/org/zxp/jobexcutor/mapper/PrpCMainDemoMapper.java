package org.zxp.jobexcutor.mapper;

import org.zxp.jobexcutor.entity.PrpCMainDemo;

import java.util.List;

public interface PrpCMainDemoMapper {
    int deleteByPrimaryKey(String policyno);

    int insert(PrpCMainDemo record);

    int insertSelective(PrpCMainDemo record);

    PrpCMainDemo selectByPrimaryKey(String policyno);

    int updateByPrimaryKeySelective(PrpCMainDemo record);

    int updateByPrimaryKey(PrpCMainDemo record);

    /**
     * 批量插入数据
     * @param list
     */
    void insertAll(List<PrpCMainDemo> list);
}