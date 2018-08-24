package org.zxp.jobexcutor.autocheck.impl;

import org.zxp.jobexcutor.aop.DealerCallBackErrorInfo;
import org.zxp.jobexcutor.autocheck.CheckInfoService;
import org.zxp.jobexcutor.entity.Elastic_job_excel_checkinfo;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;
import org.zxp.jobexcutor.mapper.Elastic_job_excel_checkinfoMapper;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component("checkInfoService")
public class CheckInfoServiceImpl implements CheckInfoService {
    @Autowired
    Elastic_job_excel_checkinfoMapper mapper;
    @Autowired
    ExcelSubService excelSubService;

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void saveAll(String uuid,long serialno,List<DealerCallBackErrorInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            DealerCallBackErrorInfo errorinfo = list.get(i);
            Elastic_job_excel_checkinfo checkinfo = coverObject(uuid,serialno,errorinfo);
            mapper.insert(checkinfo);
        }
    }

    @Override
    public List<DealerCallBackErrorInfo> getDealerCallBackErrorInfoListByUUID(String uuid) {
        List<DealerCallBackErrorInfo> retList = new ArrayList();
        List<Elastic_job_excel_checkinfo> list = mapper.selectDealerCallBackErrorInfoListByUUID(uuid);
        for (int i = 0; i < list.size(); i++) {
            Elastic_job_excel_checkinfo checkinfo = list.get(i);
            DealerCallBackErrorInfo errorinfo = coverObject(uuid,checkinfo);
            if(errorinfo != null){
                retList.add(errorinfo);
            }
        }
        return retList;
    }

    @Override
    public DealerCallBackErrorInfo getDealerCallBackErrorInfo(String uuid, String serialno, String checkcode) {
        Elastic_job_excel_checkinfo checkinfo = mapper.selectDealerCallBackErrorInfo(uuid, serialno, checkcode);
        return coverObject(uuid,checkinfo);
    }

    @Override
    public String getNewestDealerCallBackErrorInfo(String uuid, String serialno) {
        Elastic_job_excel_checkinfo checkinfo = mapper.selectNewestDealerCallBackErrorInfo(uuid, serialno);
        return checkinfo.getErrorinfo();
    }

    @Override
    public List<DealerCallBackErrorInfo> getDealerCallBackErrorInfoListByCheckcode(String checkcode) {
        List<DealerCallBackErrorInfo> retList = new ArrayList();
        List<Elastic_job_excel_checkinfo> list = mapper.selectDealerCallBackErrorInfoListByCheckcode(checkcode);
        for (int i = 0; i < list.size(); i++) {
            Elastic_job_excel_checkinfo checkinfo = list.get(i);
            DealerCallBackErrorInfo errorinfo = coverObject(checkinfo.getMainuuid(),checkinfo);
            if(errorinfo != null){
                retList.add(errorinfo);
            }
        }
        return retList;
    }

    private DealerCallBackErrorInfo coverObject(String uuid,Elastic_job_excel_checkinfo checkinfo){
        if(checkinfo == null){
            return null;
        }
        DealerCallBackErrorInfo errorinfo = new DealerCallBackErrorInfo();
        long serilano = checkinfo.getSerialno();
        Elastic_job_excel_subKey subkey = new Elastic_job_excel_subKey();
        subkey.setSerialno(serilano);
        subkey.setUuid(uuid);
        Elastic_job_excel_sub sub = excelSubService.getElastic_job_excel_sub(subkey);
        errorinfo.setT(sub);
        errorinfo.setCheckcode(checkinfo.getCheckcode());
        errorinfo.setSubFileName(checkinfo.getFilename());
        errorinfo.setErrorInfo(checkinfo.getErrorinfo());
        errorinfo.setIndexField(checkinfo.getIndexfield());
        return errorinfo;
    }

    private Elastic_job_excel_checkinfo coverObject(String uuid, long serialno, DealerCallBackErrorInfo errorinfo){
        Elastic_job_excel_checkinfo checkinfo = new Elastic_job_excel_checkinfo();
        checkinfo.setCheckinfouuid(UUID.randomUUID()+"");
        checkinfo.setMainuuid(uuid);
        checkinfo.setSerialno(serialno);
        checkinfo.setErrorinfo(errorinfo.getErrorInfo());
        checkinfo.setFilename(errorinfo.getSubFileName());
        checkinfo.setIndexfield(errorinfo.getIndexField());
        checkinfo.setIndexnum(errorinfo.getIndex()+"");
        checkinfo.setCheckcode(errorinfo.getCheckcode());
        checkinfo.setInputdate(new Date());
        return checkinfo;
    }
}
