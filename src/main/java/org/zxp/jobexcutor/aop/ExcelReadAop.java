package org.zxp.jobexcutor.aop;

import org.zxp.extfile.csv.CsvHandler;
import org.zxp.extfile.csv.FastCsvHandler;
import org.zxp.extfile.csv.autocheck.CheckService;
import org.zxp.jobexcutor.autocheck.CheckInfoService;
import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.zxp.jobexcutor.util.ApplicationContextHelper;
import org.zxp.jobexcutor.util.JobConstant;
import com.xxl.job.core.handler.IJobHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Aspect
@Component
public class ExcelReadAop {
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

    private final static Logger logger = LoggerFactory.getLogger(ExcelReadAop.class);

    /**
     * 帮助定时任务做任务获取、任务调度记录的动作
     * 注意匹配切面的条件（@Around(value）
     * 该方法内的事务均为独立事务
     * @param thisJoinPoint
     * @param dealerCallBackInfo
     * @param an
     * @return
     */
    @Around(value = "@annotation(an)&& args(dealerCallBackInfo)")
    public Object automaticaround(ProceedingJoinPoint thisJoinPoint, DealerCallBackInfo dealerCallBackInfo,
                                  ExcelRead an
    ) {
        //获取被代理对象的beanID
        Map<String , Object> map = (Map) ApplicationContextHelper.getBean(thisJoinPoint.getTarget().getClass());
        String targetBeanID = "";
        for (String key : map.keySet()) {
            targetBeanID = key;
        }
        boolean isOp = false;
        if(dealerCallBackInfo.getRedomain() != null){
            isOp = true;
        }

        Elastic_job_excel_main main = new Elastic_job_excel_main();
        main.setProdtype(an.type());//通过注解获取类型
        //查非分布式任务
        if(isOp){
            main = dealerCallBackInfo.getRedomain();
        }else{
            main = excelMainService.fetchOneReadExcelJobByCondtion_notdis(main);
        }
        if (main == null) {
            return IJobHandler.SUCCESS;
        }else{
            main.setExtfield3(targetBeanID);
            excelMainService.update(main);
        }
        dealerCallBackInfo.setUuid(main.getUuid());
        String uuid = main.getUuid();
        dealerCallBackInfo.setErrorInfo("");
        String desPath = main.getExceloripath();//作业目标目录
        dealerCallBackInfo.setHitFileName(desPath+ File.separator+main.getExceloriname().replaceAll(".xlsx","").replaceAll(".xls","")+".csv");
        logger.info("非分布式{}|UUID=[{}]|成功命中文件：[{}]", JobConstant.CSV_AOP_A1, uuid, dealerCallBackInfo.getHitFileName());

        /**解析csv数据begin**/
        FastCsvHandler handler = FastCsvHandler.getInstance();
        List csvList = new ArrayList();
        try {
            //先生成csv文件
            CsvHandler.excelCover2csv(desPath,main.getExceloriname());
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
                checkInfoService.saveAll(uuid,0l,checkService.getCheckInfoList());
                main.setExtfield4(checkCode);
                excelMainService.update(main);
                throw new Exception("自动校验失败，详见异常信息表");
            }
            dealerCallBackInfo.setCsvList(csvList);
        } catch (Exception e) {
            String errorInfo = "非分布式" + JobConstant.CSV_AOP_A1 + "|UUID=" + uuid +"|文件内容无法正常读取,解析文件“"+dealerCallBackInfo.getHitFileName()+"”异常:"+e.getMessage();
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
                    checkInfoService.saveAll(uuid,0l,dealerCallBackInfo.getCheckInfoList());
                    main.setExtfield4(checkCode);
                    excelMainService.update(main);
                    throw new Exception("业务校验失败，详见异常信息表");
                }
            }
        } catch (Throwable throwable) {
            String errorInfo = "非分布式" + JobConstant.CSV_AOP_A2 + "|UUID=" + uuid +"|用户方法执行异常:"+throwable.getMessage();
            logger.error(errorInfo,throwable);
            if(dealerCallBackInfo.getErrorInfo() == null || "".equals(dealerCallBackInfo.getErrorInfo() )){
                //如果进入此块，说明可能业务方法中未处理异常，未处理时自动处理
                dealerCallBackInfo.setErrorInfo(errorInfo);
            }
        }
        /**业务处理end**/
        //处理异常了
        if(dealerCallBackInfo.getErrorInfo() != null && !"".equals(dealerCallBackInfo.getErrorInfo())){
            main.setErrorinfo(dealerCallBackInfo.getErrorInfo());
            main.setStatus("-2");
            main.setUpdatedate(new Date());
            main.setRop(0L);
            excelMainService.update(main);
        }else{
            main.setErrorinfo("正常");
            main.setStatus("100");
            main.setExtfield4("");
            main.setRop(100L);
            main.setUpdatedate(new Date());
            excelMainService.update(main);
            //优先通过注解配置执行回调
//            if(an.callBackBeanName() != null && !"".equals(an.callBackBeanName())){
//                Object o = ApplicationContextHelper.getBean(an.callBackBeanName());
//                if(o != null && o instanceof ExcelDistributedCallBackIntf){
//                    ExcelDistributedCallBackIntf eo = (ExcelDistributedCallBackIntf)o;
//                    main = excelMainService.getElastic_job_excel_main(uuid);//获取最新的main对象，并调用回调方法
//                    eo.callBack(main);
//                }
//                //如果注解没配，那么通过dealerCallBackInfo对象调用回调方法
//            }else{
//                ExcelDistributedCallBackIntf eo = dealerCallBackInfo.getExcelDistributedCallBackIntf();
//                if(eo != null) {
//                    main = excelMainService.getElastic_job_excel_main(uuid);//获取最新的main对象，并调用回调方法
//                    eo.callBack(main);
//                }
//            }
        }

        csvList = null;
        dealerCallBackInfo.setCsvList(null);
        return IJobHandler.SUCCESS;
    }
}
