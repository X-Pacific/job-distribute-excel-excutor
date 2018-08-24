package org.zxp.jobexcutor.service.impl;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.util.JobConstant;
import org.zxp.jobexcutor.util.ZKInterProcessMutexMap;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("zkDistributedLockService")
public class ZookeeperDistributedLockServiceImpl implements DistributedLockService {
    private final static Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLockServiceImpl.class);

    @Autowired
    ExcelMainService excelMainService;

    private static CuratorFramework client = null;
    private static String ROOT_LOCK = "/distributeLocks/";

    @Value("${spring.zk.config.uri}")
    private String uri;

    private synchronized CuratorFramework getZkClient(){
        if(client == null) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            client = CuratorFrameworkFactory.newClient(uri, retryPolicy);
            client.start();
        }
        return client;
    }


    public boolean zkTryLock(String uuid) throws Exception {
        client = getZkClient();
        InterProcessMutex mutex = new InterProcessMutex(client, ROOT_LOCK+uuid);
        boolean result = mutex.acquire(1000,TimeUnit.MILLISECONDS);
        if(result){
            logger.info("zk获得锁");
            //如果持锁成功，则map中维护一份
            ZKInterProcessMutexMap.put(uuid,mutex);
        }
        return result;
    }

    @Override
    public boolean tryLock(String uuid) throws Exception {
        if (uuid == null || uuid.equals("")) {
            throw new NullPointerException();
        }
        int tryCount = 0;
        while (true) {
            if (zkTryLock(uuid)) {
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
        ZKInterProcessMutexMap.release(uuid);
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        main.setExtfield1("0");
        excelMainService.update(main);
    }


    public static void main(String[] args) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client.start();
        InterProcessMutex mutex = new InterProcessMutex(client, "/curator/WINDOWS");
        boolean result = mutex.acquire(1000,TimeUnit.MILLISECONDS);
        mutex.release();
        System.out.println(mutex.acquire(1000,TimeUnit.MILLISECONDS));
        client.close();
    }

}