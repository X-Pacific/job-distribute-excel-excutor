package org.zxp.jobexcutor.service.impl;

import org.zxp.extfile.util.Constant;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub_his;
import org.zxp.jobexcutor.mapper.Elastic_job_excel_subMapper;
import org.zxp.jobexcutor.mapper.Elastic_job_excel_sub_hisMapper;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("excelSubService")
public class ExcelSubServiceImpl implements ExcelSubService {
    @Autowired
    Elastic_job_excel_subMapper mapper ;
    @Autowired
    Elastic_job_excel_sub_hisMapper hismapper ;
    @Override
    public Elastic_job_excel_sub getElastic_job_excel_sub(Elastic_job_excel_subKey key) {
        return mapper.selectByPrimaryKey(key);
    }

    @Override
    public List<Elastic_job_excel_sub> getElastic_job_excel_subListByUuid(String uuid) {
        return mapper.selectByUuid(uuid);
    }

    @Override
    public List<Elastic_job_excel_sub> genExcelSubList(String mainUUID, String subPath, List<String> subList) {
        List<Elastic_job_excel_sub> results = new ArrayList<Elastic_job_excel_sub>();

        for (int i = 0; i < subList.size(); i++) {
            Elastic_job_excel_sub sub = new Elastic_job_excel_sub();
            sub.setUuid(mainUUID);
            sub.setSerialno(Long.parseLong(subList.get(i).substring(0,subList.get(i).length() - 4).split(Constant.SUB_FILE_SPLITER)[1]));
            sub.setExcelsplitname(subList.get(i));
            sub.setExcelsplitpath(subPath);
            sub.setStatus("1");
            sub.setInputdate(new Date());
            sub.setUpdatedate(new Date());
            results.add(sub);
        }
        return results;
    }

    @Override
    @Transactional
    public void saveAll(String uuid,List<Elastic_job_excel_sub> subList) {
        if(uuid != null && !"".equals(uuid)){
            mapper.deleteByUUID(uuid);
        }
        for (int i = 0; i < subList.size(); i++) {
            mapper.insert(subList.get(i));
        }
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void update(Elastic_job_excel_sub sub) {
        mapper.updateByPrimaryKey(sub);
    }

    @Override
    public void delete(String uuid) {
        mapper.deleteByUUID(uuid);
    }

    @Override
    public long getSubSumTotal(String uuid) {
        return mapper.selectSumTotal(uuid);
    }

    @Override
    public int getCountByStatus(String uuid, String status) {
        return mapper.selectCountByStatus(uuid,status);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void changeSub(String uuid) {
        List<Elastic_job_excel_sub> list =  mapper.selectByUuid(uuid);
        for (int i = 0; i < list.size(); i++) {
            Elastic_job_excel_sub_his his = new Elastic_job_excel_sub_his();
            BeanUtils.copyProperties(list.get(i),his);
            hismapper.deleteByPrimaryKey(his);
            hismapper.insert(his);
        }
        mapper.deleteByUUID(uuid);
    }
}
