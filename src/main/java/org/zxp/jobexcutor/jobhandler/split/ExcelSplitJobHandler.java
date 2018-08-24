package org.zxp.jobexcutor.jobhandler.split;


import org.zxp.extfile.csv.CsvHandler;
import org.zxp.extfile.split.FileSplitter;
import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.zxp.jobexcutor.util.JobConstant;
import org.zxp.jobexcutor.util.Tools;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 拆分文件job，该job由xxl-admin 一致性哈希算法配置调用
 * 开发人员无需关注这个实现，仅需配置定时任务即可（一致性哈希算法）
 */
//@JobHandler(value="excelSplitJobHandler")
//调整为简写的方式，方便配置
@JobHandler(value="ESJH")
@Component
public class ExcelSplitJobHandler extends IJobHandler {
    private final static Logger logger = LoggerFactory.getLogger(ExcelSplitJobHandler.class);

    @Autowired
    ExcelMainService excelMainService;
    @Autowired
    ExcelSubService excelSubService;
    /**注入配置信息类*/
    @Autowired
    ExcelSplitConfig excelSplitConfig;

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public ReturnT<String> execute(String param)   {
        /***fetch一个任务，并置为中间状态*/
        Elastic_job_excel_main main = excelMainService.fetchOneSplitExcelJobByType(excelSplitConfig.genFetchCondition());
        if(main == null){
            return SUCCESS;
        }
        String uuid = main.getUuid();
        logger.info("{}|UUID=[{}]|获取UUID:[{}]的任务", JobConstant.CSV_COVERT, uuid, uuid);
        String filePath = main.getExceloripath();
        String fileName = main.getExceloriname();
        String fileUri = filePath + File.separator + fileName;
        String csvUri = filePath + File.separator + fileName.substring(0,fileName.lastIndexOf(".")) + ".csv";
        String desFileUri = filePath + File.separator + fileName + "_" + uuid;
        FileSplitter splitter = FileSplitter.getInstance();
        int splitFileSize = JobConstant.DEFAULT_SPLIT_SIZE;//默认拆分文件大小为1M
        try{
            splitFileSize = Integer.parseInt(main.getFlag());//按照配置设置拆分文件大小
        }catch (Exception e){
            //正常情况，不做处理
        }
        /** 保存变量*/
        String status = "1";
        String errorInfo = "正常";
        long converttime = 0L;/**转化耗时*/
        long splittime = 0L;/**拆分耗时*/
        List<Elastic_job_excel_sub> subList = null;
        int mainTotal = 0;
        try {
            long timebegin = Tools.getNowLongDate();
            mainTotal = CsvHandler.excelCover2csv(filePath,fileName);
            mainTotal = mainTotal - 1;//需要去掉一个title才是真正的处理行数，用于主子核对
            long timeend = Tools.getNowLongDate();
            converttime = timeend - timebegin;
        } catch (Exception e) {
            status = "-1";
            errorInfo = JobConstant.CSV_COVERT+"|UUID=" + uuid + "|ExcelSplitJobHandler转化csv异常:"+e.getMessage();
            logger.error(errorInfo,e);
        }
        if(!"1".equals(main.getExtfield2())) {//分布式处理流程
            /**开始拆分excel文件为多个csv文件*/
            try {
                if ("1".equals(status)) {
                    long timebegin = Tools.getNowLongDate();
                    File desFile = new File(desFileUri);
                    if (desFile.exists() && desFile.isDirectory()) {
                        Tools.delFolder(desFileUri);
                    }
                    /**
                     * 这个拆分我们在extfile中做了特殊处理，将每一个csv文件都加了一行title
                     */
                    splitter.split(csvUri, splitFileSize, desFileUri);
                    long timeend = Tools.getNowLongDate();
                    splittime = timeend - timebegin;
                }
            } catch (Exception e) {
                status = "-1";
                errorInfo = JobConstant.CSV_COVERT + "|UUID=" + uuid + "|ExcelSplitJobHandler拆分异常:" + e.getMessage();
                logger.error(errorInfo, e);
            }
            List<String> splitFileNames = null;
            try {
                splitFileNames = Tools.lisFile(desFileUri);
            } catch (IOException e) {
                status = "-1";
                errorInfo = JobConstant.CSV_COVERT + "|UUID=" + uuid + "|ExcelSplitJobHandler获取拆分csv文件异常:" + e.getMessage();
                logger.error(errorInfo, e);
            }
            main.setExcelsplitpath(desFileUri);
            main.setExcelsplitsize(Long.parseLong(splitFileNames.size() + ""));
            main.setStatus(status);
            main.setUpdatedate(new Date());
            main.setErrorinfo(errorInfo);
            if (main.getTotal() == null || main.getTotal() == 0L) {
                main.setTotal(Long.parseLong(mainTotal + ""));//如果之前没有设置总数，则设置总条数
            }
            main.setConverttimeuse(converttime);
            main.setSplittimeuse(splittime);
            /**存储子表拆分结果***/
            if ("1".equals(status) && splitFileNames != null) {
                subList = excelSubService.genExcelSubList(uuid, desFileUri, splitFileNames);
                excelSubService.saveAll(uuid, subList);
            }
            main.setRop(Tools.getRop(main, subList));
            excelMainService.update(main);//更新任务状态
            logger.info("{}|UUID=[{}]|拆分结果:[{}]，拆分个数:[{}]", JobConstant.CSV_SPLIT, uuid, status, splitFileNames.size());
            //这种打印方式可以将日志显示到admin上，但只限于单机，所以暂时不用
            //XxlJobLogger.log("UUID"+uuid);
        }else{//非分布式处理流程
            main.setExcelsplitpath("");
            main.setExcelsplitsize(1L);
            main.setStatus(status);
            main.setUpdatedate(new Date());
            main.setErrorinfo(errorInfo);
            if (main.getTotal() == null || main.getTotal() == 0L) {
                main.setTotal(Long.parseLong(mainTotal + ""));//如果之前没有设置总数，则设置总条数
            }
            main.setConverttimeuse(converttime);
            main.setSplittimeuse(0L);
            main.setRop(0L);
            excelMainService.update(main);//更新任务状态
        }
        return SUCCESS;
    }


    public static void main(String[] args){
        String a = "D:\\eeee\\gencsv\\e2.csv";
        File aa = new File(a);
        aa.delete();
    }

}
