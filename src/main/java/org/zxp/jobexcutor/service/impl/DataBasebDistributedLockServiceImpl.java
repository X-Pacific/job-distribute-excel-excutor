package org.zxp.jobexcutor.service.impl;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.util.JobConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通过数据库获取锁
 */
@Service("databaseDistributedLockService")
public class DataBasebDistributedLockServiceImpl implements DistributedLockService {
    @Autowired
    ExcelMainService excelMainService;

    /**
     * 这个方法不加独立事务了，直接调用excelMainService独立事物的方法，否则（这个方法加了独立事务）里面的搜所有方法均用同一个事务
     */
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public synchronized boolean tryLock(String uuid) throws Exception {
        if (uuid == null || uuid.equals("")) {
            throw new NullPointerException();
        }
        int tryCount = 0;
        while (true) {
            if (!isLock(uuid)) {
                Elastic_job_excel_main mainCondtion = excelMainService.getElastic_job_excel_main(uuid);
                mainCondtion.setExtfield1("1");
                int excuteCount = excelMainService.updateByLock(mainCondtion);
                //说明已经获得锁
                if (excuteCount == 1) {
                    return true;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            if (++tryCount > JobConstant.MAX_TRY_COUNT) {
                throw new Exception("can not exceed MAX_TRY_COUNT");
            }
        }
    }


    @Override
    public boolean isLock(String uuid) {
        if (uuid == null || uuid.equals("")) {
            throw new NullPointerException();
        }
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        if ("1".equals(main.getExtfield1())) {
            return true;
        }
        return false;
    }


    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public synchronized void releaseLock(String uuid) throws Exception {
        if(uuid == null || uuid.equals("")){
            throw new NullPointerException();
        }
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        main.setExtfield1("0");
        excelMainService.update(main);
    }

}
