package org.zxp.jobexcutor.service;

import org.zxp.jobexcutor.entity.PrpCMainDemo;

import java.util.List;

public interface DemoJobService {
    /**
     * 保存prpcmaindemo表数据
     * @param list
     */
    public void saveAll(List<PrpCMainDemo> list) throws Exception;
}
