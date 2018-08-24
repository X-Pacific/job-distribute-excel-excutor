package org.zxp.jobexcutor.service;

public interface DistributedLockService {
    /**
     * 尝试锁UUID的任务
     * @param uuid
     * @return
     */
    public boolean tryLock(String uuid) throws Exception;

    /**
     * 判断当前记录是否被锁
     * @param uuid
     * @return
     */
    public boolean isLock(String uuid) ;

    /**
     * 释放锁
     * @param uuid
     * @return
     */
    public void releaseLock(String uuid) throws Exception;


}
