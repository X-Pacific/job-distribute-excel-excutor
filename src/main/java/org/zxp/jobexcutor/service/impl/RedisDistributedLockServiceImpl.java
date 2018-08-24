package org.zxp.jobexcutor.service.impl;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.util.JobConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service("redisDistributedLockService")
public class RedisDistributedLockServiceImpl implements DistributedLockService {

    private final static Logger logger = LoggerFactory.getLogger(RedisDistributedLockServiceImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    ExcelMainService excelMainService;


    public boolean redisTrylock(String key) {
        boolean result = redisTemplate.opsForValue().setIfAbsent(key, "This is a Lock.");
        return result;
    }


    @Override
    public boolean tryLock(String uuid) throws Exception {
        if (uuid == null || uuid.equals("")) {
            throw new NullPointerException();
        }
        int tryCount = 0;
        while (true) {
            if(redisTrylock(uuid)) {
                Elastic_job_excel_main mainCondtion = excelMainService.getElastic_job_excel_main(uuid);
                mainCondtion.setExtfield1("1");
                excelMainService.update(mainCondtion);
                return true;
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
        return false;
    }

    @Override
    public void releaseLock(String uuid) throws Exception {
        if (uuid == null || uuid.equals("")) {
            throw new NullPointerException();
        }
        redisTemplate.delete(uuid);
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        main.setExtfield1("0");
        excelMainService.update(main);
    }

}
