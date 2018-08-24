package org.zxp.jobexcutor.util;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZKInterProcessMutexMap {
    private final static Logger logger = LoggerFactory.getLogger(ZKInterProcessMutexMap.class);

    public static Map<String ,InterProcessMutex> map = new ConcurrentHashMap<>();

    public static synchronized void put(String uuid,InterProcessMutex i){
        map.put(uuid,i);
    }

    public static void release(String uuid){
        if(map.containsKey(uuid)){
            InterProcessMutex i = map.get(uuid);
            try {
                i.release();
                take(uuid);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }

    public static synchronized void take(String uuid){
        map.remove(uuid);
    }
}
