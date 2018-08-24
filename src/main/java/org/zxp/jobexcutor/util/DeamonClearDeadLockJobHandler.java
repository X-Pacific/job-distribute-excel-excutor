package org.zxp.jobexcutor.util;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 由于基于数据库的分布式锁可能出现死锁的问题，所以可以配置本任务以自动解锁（一致性hash方式）
 * 如果需要开启数据库锁的解锁保护，则请配置该任务
 */
@JobHandler(value="deamonClearDeadLockJobHandler")
@Component
public class DeamonClearDeadLockJobHandler extends IJobHandler {
    private final static Logger logger = LoggerFactory.getLogger(DeamonClearDeadLockJobHandler.class);

    @Value("${ExcelDistributedReadAop.distributedlock}")
    private String distributedlock;
    @Autowired
    private ExcelMainService excelMainService;
    @Autowired
    DistributedLockService databaseDistributedLockService;
    @Autowired
    DistributedLockService redisDistributedLockService;
    @Autowired
    DistributedLockService zkDistributedLockService;

    private static Map<String ,Long> timeMap = new ConcurrentHashMap<>();

    /**
     * 获取分布式锁服务
     * @return
     */
    private DistributedLockService getDistributedLockService(){
        if(distributedlock == null){
            return databaseDistributedLockService;
        }
        if("REDIS".equals(distributedlock.toUpperCase())){
            return redisDistributedLockService;
        }
        else if("ZK".equals(distributedlock.toUpperCase())){
            return zkDistributedLockService;
        }
        else{
            return databaseDistributedLockService;
        }
    }

    public void startDeamon(){
        //只有数据库方式才通过守护线程巡逻保证不死锁
        if("DB".equals(distributedlock.toUpperCase()) || "REDIS".equals(distributedlock.toUpperCase())) {
            Elastic_job_excel_main main = new Elastic_job_excel_main();
            main.setStatus("-2,2,20");
            List<Elastic_job_excel_main> list = excelMainService.getMainList(main);
            if (list != null) {
                DistributedLockService distributedLockService = getDistributedLockService();
                for (int i = 0; i < list.size(); i++) {
                    main = list.get(i);
                    String uuid = main.getUuid();
                    //如果包含则比对
                    if (timeMap.containsKey(uuid)) {
                        long startTime = timeMap.get(uuid);
                        long nowTime = new Date().getTime();
                        //超过了，释放锁
                        if (nowTime - startTime > JobConstant.RECOVER_TIME) {
                            try {
                                logger.info("发现uuid:"+uuid+"锁逾期，自动解锁");
                                distributedLockService.releaseLock(uuid);
                                timeMap.remove(uuid);
                            } catch (Exception e) {
                                logger.error("", e);
                            }
                        }
                    }
                    //如果不包含则初始化第一次
                    else {
                        timeMap.put(uuid, new Date().getTime());
                    }
                }
            }
        }
    }

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        startDeamon();
        return ReturnT.SUCCESS;
    }
}
