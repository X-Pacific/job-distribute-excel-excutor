package org.zxp.jobexcutor.aop;


import org.zxp.extfile.csv.FastCsvHandler;
import org.zxp.extfile.csv.autocheck.CheckService;
import org.zxp.extfile.util.Constant;
import org.zxp.jobexcutor.autocheck.CheckInfoService;
import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.zxp.jobexcutor.util.*;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.util.ShardingUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zxp.jobexcutor.util.*;

import java.io.File;
import java.util.*;

/**
 * 分布式处理excel切面
 */
@Aspect
@Component
public class ExcelDistributedReadAop {
    @Autowired
    ExcelMainService excelMainService;
    @Autowired
    ExcelSubService excelSubService;
    @Autowired
    DistributedLockService databaseDistributedLockService;
    @Autowired
    DistributedLockService redisDistributedLockService;
    @Autowired
    DistributedLockService zkDistributedLockService;
    @Autowired
    CheckService<DealerCallBackErrorInfo> checkService;
    @Autowired
    CheckInfoService checkInfoService;
    @Value("${ExcelDistributedReadAop.distributedlock}")
    private String distributedlock;

    private final static Logger logger = LoggerFactory.getLogger(ExcelDistributedReadAop.class);

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

    /**
     * 是否运维方式
     * @param dealerCallBackInfo
     * @return
     */
    private boolean isOp(ShardingUtil.ShardingVO shardingVO,DealerCallBackInfo dealerCallBackInfo){
        if(shardingVO.getIndex() == -999 && shardingVO.getTotal() == -999 && dealerCallBackInfo.getRedosub() != null
                && !Tools.isEmpty(dealerCallBackInfo.getRedosub().getUuid())){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 帮助定时任务做任务获取、任务调度记录的动作
     * 注意匹配切面的条件（@Around(value）
     * 该方法内的事务均为独立事务
     * @param thisJoinPoint
     * @param shardingVO
     * @param dealerCallBackInfo
     * @param an
     * @return
     */
    @Around(value = "@annotation(an)&& args(shardingVO,dealerCallBackInfo)")
    public Object automaticaround(ProceedingJoinPoint thisJoinPoint, ShardingUtil.ShardingVO shardingVO, DealerCallBackInfo dealerCallBackInfo,
                                 ExcelDistributedRead an
    ) {
        //获取被代理对象的beanID
        Map<String , Object> map = (Map) ApplicationContextHelper.getBean(thisJoinPoint.getTarget().getClass());
        String targetBeanID = "";
        for (String key : map.keySet()) {
            targetBeanID = key;
        }
        if(shardingVO == null){
            throw new NullPointerException();
        }
        //获取是否运维方式
        boolean isOp = isOp(shardingVO,dealerCallBackInfo);
        String shardingText = "["+shardingVO.getIndex()+","+shardingVO.getTotal()+"]";
        DistributedLockService distributedLockService = getDistributedLockService();
        /**分片参数*/
        logger.info("{}|分片参数：当前命中分片序号 = [{}], 总分片数 = [{}], 准备开始选取任务，并锁定子任务", JobConstant.CSV_AOP_A1, shardingVO.getIndex(), shardingVO.getTotal());
        /**抓取一个新的可以被分布式处理的任务 并置状态 begin*/
        Elastic_job_excel_main main = new Elastic_job_excel_main();
        main.setProdtype(an.type());//通过注解获取类型
        if(isOp){
            //运维方式先锁定主任务
            main.setUuid(dealerCallBackInfo.getRedosub().getUuid());
            main = excelMainService.findOneReadExcelJobByTypeOp(main);
        }else {
            main = excelMainService.fetchOneReadExcelJobByCondtion(main);
        }
        if (main == null) {
            return IJobHandler.SUCCESS;
        }else{
            //先把代理beanID更新进去
            main.setExtfield3(targetBeanID);
            excelMainService.update(main);
        }
        dealerCallBackInfo.setUuid(main.getUuid());
        String uuid = main.getUuid();
        dealerCallBackInfo.setErrorInfo("");
        dealerCallBackInfo.setHitFileName(null);
        String desPath = main.getExcelsplitpath();//作业目标目录
        /**抓取一个新的可以被分布式处理的任务 并置状态 end*/
        //🔒分🔒部🔒式🔒锁🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒
        /** 下面锁定这个主任务，在这个期间，获得锁的线程将可以优先选择子任务 */
        try {
            /**尝试取得当前任务锁。如果得不到则阻塞，最大100次尝试，达到最大后抛出异常*/
            distributedLockService.tryLock(uuid);
        } catch (Exception e) {
            String errorInfo = shardingText + JobConstant.CSV_AOP_A1 + "|UUID=" + uuid + "|获取锁异常:" + e.getMessage();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            logger.error(errorInfo, e);
            //锁都没获取成功 就退下吧，这里返回，被代理方法进不去
            excelMainService.setErrorInfoAndSave(uuid,errorInfo);
            return IJobHandler.SUCCESS;
        }
        /**抓取一个未被处理的子excel进行处理**/
        int totalSharding = shardingVO.getTotal();//总分片
        int currentSharding = shardingVO.getIndex();//当前分片
        HitFile hitfile = null;
        try {
            if(isOp){// 运维方式直接锁文件
                hitfile = FileHitter.hitGoalFile(dealerCallBackInfo.getRedosub().getExcelsplitpath(),dealerCallBackInfo.getRedosub().getExcelsplitname());
            }else {
                hitfile = FileHitter.hitFile(desPath, totalSharding, currentSharding);
            }
            if(hitfile != null) {
                dealerCallBackInfo.setHitFileName(hitfile.getFilePath() + File.separator + hitfile.getNewFileName());
                logger.info(shardingText + "{}|UUID=[{}]|成功命中文件：[{}]", JobConstant.CSV_AOP_A1, uuid, dealerCallBackInfo.getHitFileName());
            }
        } catch (Exception e) {
            //未命中文件可能当前分片所属已经处理完成
            String errorInfo = shardingText + JobConstant.CSV_AOP_A1 + "|UUID=" + uuid + "|终止当前定时任务,命中子文件异常:" + e.getMessage();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            logger.error(errorInfo, e);
        }
        /**完成选择 释放锁*/
        try {
            distributedLockService.releaseLock(uuid);
            logger.info(shardingText + "{}|UUID=[{}]|已经完成解锁", JobConstant.CSV_AOP_A1, uuid, dealerCallBackInfo.getHitFileName());
        } catch (Exception e) {
            String errorInfo = shardingText + JobConstant.CSV_AOP_A1 + "|UUID=" + uuid + "|终止当前定时任务,释放锁异常:" + e.getMessage();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            logger.error(errorInfo, e);
            excelMainService.setErrorInfoAndSave(uuid,errorInfo);
            return IJobHandler.SUCCESS;
        }
        if (!"".equals(dealerCallBackInfo.getErrorInfo())) {
            //获取子任务失败
            excelMainService.setErrorInfoAndSave(uuid, dealerCallBackInfo.getErrorInfo());
            return IJobHandler.SUCCESS;
        }
        if(hitfile == null){
            logger.info(shardingText + "{}|UUID=[{}]|未命中文件，当前分片所属文件已经处理完成", JobConstant.CSV_AOP_A1, uuid);
            return IJobHandler.SUCCESS;
        }
        //🔒分🔒部🔒式🔒锁🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒🔒
        // excelSubService找到这个csv记录，改子任务状态为进行中
        Elastic_job_excel_subKey subKey = new Elastic_job_excel_subKey();
        subKey.setUuid(uuid);
        subKey.setSerialno(Long.parseLong(hitfile.getOriFileName().substring(0,hitfile.getOriFileName().length()-4).split(Constant.SUB_FILE_SPLITER)[1]));
        Elastic_job_excel_sub sub = excelSubService.getElastic_job_excel_sub(subKey);

        if (sub.getStatus() != null && !sub.getStatus().equals("1") && !isOp) {
            String errorInfo = JobConstant.CSV_AOP_A1 + "|UUID=" + uuid + "|终止当前定时任务,状态校验异常,当前文件" + dealerCallBackInfo.getHitFileName() + "不是待处理状态," + sub.getStatus();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            sub.setErrorinfo(errorInfo);
            excelSubService.update(sub);
            return IJobHandler.SUCCESS;
        }
        sub.setDealbegintime(new Date().getTime()+"");
        sub.setStatus("4");
        sub.setFlag("["+shardingVO.getIndex()+","+shardingVO.getTotal()+"]");
        sub.setUpdatedate(new Date());
        excelSubService.update(sub);
        /**解析csv数据begin**/
        FastCsvHandler handler = FastCsvHandler.getInstance();
        List csvList = new ArrayList();
        try {
            //这个size没有包含title
            checkService.setCheckInfoList(new ArrayList());
            csvList = handler.dealCsv(dealerCallBackInfo.getHitFileName(), an.clazz(), "", checkService);
            if(checkService.getCheckInfoList().size() != 0){
                String checkCode = UUID.randomUUID() + "";
                for (int i = 0; i < checkService.getCheckInfoList().size(); i++) {
                    checkService.getCheckInfoList().get(i).setSubFileName(dealerCallBackInfo.getHitFileName());
                    checkService.getCheckInfoList().get(i).setCheckcode(checkCode);
                }
                //保存异常表信息
                checkInfoService.saveAll(uuid,sub.getSerialno(),checkService.getCheckInfoList());
                sub.setCheckcode(checkCode);
                throw new Exception("自动校验失败，详见异常信息表");
            }
            dealerCallBackInfo.setCsvList(csvList);
        } catch (Exception e) {
            String errorInfo = shardingText + JobConstant.CSV_AOP_A1 + "|UUID=" + uuid +"|文件内容无法正常读取,解析文件“"+dealerCallBackInfo.getHitFileName()+"”异常:"+e.getMessage();
            dealerCallBackInfo.setErrorInfo(errorInfo);
            logger.error(errorInfo,e);
        }
        /**解析csv数据end**/
        /**业务处理begin**/
        try {
            if(dealerCallBackInfo.getErrorInfo() == null || "".equals(dealerCallBackInfo.getErrorInfo())) {
                thisJoinPoint.proceed();//环绕通知必须执行，否则不进入注解的方法
                if(dealerCallBackInfo.getCheckInfoList() != null && dealerCallBackInfo.getCheckInfoList().size() != 0) {
                    String checkCode = UUID.randomUUID() + "";
                    for (int i = 0; i < dealerCallBackInfo.getCheckInfoList().size(); i++) {
                        ((DealerCallBackErrorInfo) dealerCallBackInfo.getCheckInfoList().get(i)).setSubFileName(dealerCallBackInfo.getHitFileName());
                        ((DealerCallBackErrorInfo) dealerCallBackInfo.getCheckInfoList().get(i)).setCheckcode(checkCode);
                    }
                    //保存异常表信息
                    checkInfoService.saveAll(uuid,sub.getSerialno(),dealerCallBackInfo.getCheckInfoList());
                    sub.setCheckcode(checkCode);
                    throw new Exception("业务校验失败，详见异常信息表");
                }
            }
        } catch (Throwable throwable) {
            String errorInfo = shardingText + JobConstant.CSV_AOP_A2 + "|UUID=" + uuid +"|用户方法执行异常:"+throwable.getMessage();
            logger.error(errorInfo,throwable);
            if(dealerCallBackInfo.getErrorInfo() == null || "".equals(dealerCallBackInfo.getErrorInfo() )){
                //如果进入此块，说明可能业务方法中未处理异常，未处理时自动处理
                dealerCallBackInfo.setErrorInfo(errorInfo);
            }
        }
        /**业务处理end**/
        /**更新子任务状态*/
        if (dealerCallBackInfo.getErrorInfo() != null && !"".equals(dealerCallBackInfo.getErrorInfo())) {
            sub.setStatus("3");//设置为处理失败并赋值原因
            if(dealerCallBackInfo.getErrorInfo().length() > 900) {
                sub.setErrorinfo(dealerCallBackInfo.getErrorInfo().substring(0, 900));
            }else{
                sub.setErrorinfo(dealerCallBackInfo.getErrorInfo());
            }
        }else{
            sub.setErrorinfo("");
            sub.setCheckcode("");
            sub.setStatus("2");//设置为处理完成
        }
        if(csvList != null) {
            sub.setTotal(Long.parseLong(csvList.size()+""));
        }else{
            sub.setTotal(0L);
        }
        sub.setUpdatedate(new Date());
        sub.setDealendtime(new Date().getTime()+"");
        excelSubService.update(sub);
        //检查任务是否已经完成
        boolean isFinish = excelMainService.checkIsFinishAndWriteStatus(uuid, an.isCheckSumAmount());
        /**更新进度信息**/
        excelMainService.updateRopMain(uuid,excelSubService.getElastic_job_excel_subListByUuid(uuid));
        //如果已经完成那么执行回调
        if(isFinish){
            //如果走到这里，那说明主任务已经执行完成，回写errorinfo
            excelMainService.setErrorInfoAndSave(uuid, "正常");
            //优先通过注解配置执行回调
            if(an.callBackBeanName() != null && !"".equals(an.callBackBeanName())){
                Object o = ApplicationContextHelper.getBean(an.callBackBeanName());
                if(o != null && o instanceof ExcelDistributedCallBackIntf){
                    ExcelDistributedCallBackIntf eo = (ExcelDistributedCallBackIntf)o;
                    main = excelMainService.getElastic_job_excel_main(uuid);//获取最新的main对象，并调用回调方法
                    eo.callBack(main);
                }
                //如果注解没配，那么通过dealerCallBackInfo对象调用回调方法
            }else{
                ExcelDistributedCallBackIntf eo = dealerCallBackInfo.getExcelDistributedCallBackIntf();
                if(eo != null) {
                    main = excelMainService.getElastic_job_excel_main(uuid);//获取最新的main对象，并调用回调方法
                    eo.callBack(main);
                }
            }
            //全部完成转储sub表数据
            excelSubService.changeSub(uuid);
        }
        //手动释放内存占用
        csvList = null;
        dealerCallBackInfo.setCsvList(null);
        return IJobHandler.SUCCESS;
    }


}
