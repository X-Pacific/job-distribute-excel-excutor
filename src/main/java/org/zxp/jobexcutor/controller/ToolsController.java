package org.zxp.jobexcutor.controller;

import org.zxp.extfile.util.Constant;
import org.zxp.jobexcutor.aop.DealerCallBackInfo;
import org.zxp.jobexcutor.aop.ExcelDistributedReadIntf;
import org.zxp.jobexcutor.aop.ExcelReadIntf;
import org.zxp.jobexcutor.autocheck.CheckInfoService;
import org.zxp.jobexcutor.controller.vo.LookVo;
import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.entity.Elastic_job_excel_subKey;
import org.zxp.jobexcutor.jobhandler.handler.ExcelDistributedReadDemoJobHandler;
import org.zxp.jobexcutor.service.DistributedLockService;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.zxp.jobexcutor.util.ApplicationContextHelper;
import org.zxp.jobexcutor.util.JobConstant;
import org.zxp.jobexcutor.util.Tools;
import com.xxl.job.core.util.ShardingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/tools")
@EnableAutoConfiguration
public class ToolsController {
    private final static Logger logger = LoggerFactory.getLogger(ToolsController.class);

    @Autowired
    ExcelMainService excelMainService;
    @Autowired
    ExcelSubService excelSubService;
    @Autowired
    ExcelDistributedReadDemoJobHandler excelDistributedReadDemoJobHandler;
    @Autowired
    DistributedLockService databaseDistributedLockService;
    @Autowired
    DistributedLockService redisDistributedLockService;
    @Autowired
    DistributedLockService zkDistributedLockService;
    @Autowired
    CheckInfoService checkInfoService;
    @Value("${ExcelDistributedReadAop.distributedlock}")
    private String distributedlock;

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
            return null;
        }
        else{
            return databaseDistributedLockService;
        }
    }


    /**
     * 处理分布式异常情况的文件
     * @param uuid
     * @param fileserialno
     * @return
     * @throws Exception
     */
    @RequestMapping("/dealfail/{uuid}/{fileserialno}")
    @ResponseBody
    String dealfail(@PathVariable("uuid")String uuid,@PathVariable("fileserialno")String fileserialno) throws Exception {
        String ret = "请传入正确的参数！";
        if(Tools.isEmpty(uuid) || Tools.isEmpty(fileserialno)){
            return "请传入正确的参数！";
        }
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        Elastic_job_excel_subKey subkey = new Elastic_job_excel_subKey();
        subkey.setUuid(uuid);
        subkey.setSerialno(Long.parseLong(fileserialno));
        Elastic_job_excel_sub sub = excelSubService.getElastic_job_excel_sub(subkey);
        if(main == null){
            return "请传入正确的参数！";
        }
        if("0".equals(main.getExtfield2())){
            return "非分布式任务不能通过此接口重调！";
        }
        ExcelDistributedReadIntf intf = null;
        if(!Tools.isEmpty(main.getExtfield3())){
            intf = (ExcelDistributedReadIntf)ApplicationContextHelper.getBean(main.getExtfield3());
        }
        if(intf == null || Tools.isEmpty(main.getExtfield3()) || sub == null){
            return "内部错误，未知的空异常！";
        }
        if(!sub.getStatus().equals("3")){
            return "状态不正确，当前任务不能补执行！";
        }
        //还原文件名称begin
        String filename = sub.getExcelsplitpath() + File.separator + sub.getExcelsplitname();
        String filename_ing = "";
        String filename_ori = "";
        if(sub.getExcelsplitname().indexOf(JobConstant.EXCUTING_FLAG) > -1){
            filename_ing = filename;
            filename_ori = filename.replaceAll(Constant.SUB_FILE_SPLITER+JobConstant.EXCUTING_FLAG,"");
        }else{
            filename_ori = filename;
            filename_ing = filename.substring(0,filename.lastIndexOf("."))+Constant.SUB_FILE_SPLITER+JobConstant.EXCUTING_FLAG+filename.substring(filename.lastIndexOf("."),filename.length());
        }
        File fileori = new File(filename_ori);
        if(!fileori.exists() || !fileori.isFile()){
            return "请重新上传子文件["+fileori+"]！";
        }
        File file = new File(filename_ing);
        if(file.exists() && file.isFile()){
            file.delete();
        }
        //还原文件名称end
        ShardingUtil.ShardingVO shardingVO = new ShardingUtil.ShardingVO(-999,-999);
        DealerCallBackInfo dealerCallBackInfo = new DealerCallBackInfo();
        dealerCallBackInfo.setRedosub(sub);
        intf.deal(shardingVO,dealerCallBackInfo);
        sub = excelSubService.getElastic_job_excel_sub(subkey);
        if(sub.getStatus().equals("2")){
            return "执行成功！";
        }else{
            return "执行失败，失败原因："+sub.getErrorinfo();
        }
    }



    @RequestMapping("/releaseLock/{uuid}")
    @ResponseBody
    LookVo releaseLock(@PathVariable("uuid")String uuid) throws Exception {
        DistributedLockService distributedLockService = getDistributedLockService();
        if(distributedLockService == null){
            throw new Exception("当前模式不支持手动解锁!");
        }
        try {
            distributedLockService.releaseLock(uuid);
        } catch (Exception e) {
            logger.error("",e);
        }
        return look(uuid);
    }

    @RequestMapping("/look/{uuid}")
    @ResponseBody
    LookVo look(@PathVariable("uuid")String uuid) {
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        LookVo lookDto = new LookVo();
        lookDto.setUuid(main.getUuid());
        lookDto.setRiskcode(main.getRiskcode());
        lookDto.setErrorinfo(main.getErrorinfo());
        lookDto.setRop(main.getRop()+"%");
        lookDto.setIsLock(main.getExtfield1());
        lookDto.setStatus(main.getStatus());
        lookDto.setExcelpath(main.getExceloripath()+File.separator+main.getExceloriname());
        lookDto.setTotal(main.getTotal()+"");
        lookDto.setType(main.getProdtype());
        long a = 0l;
        long b = 0l;
        long c = 0l;
        if(main.getSplittimeuse() != null){
            a = main.getSplittimeuse();
        }
        if(main.getConverttimeuse() != null){
            b = main.getConverttimeuse();
        }
        if(main.getDealbegintime() != null && main.getDealendtime() != null && !main.getDealendtime().equals("0")){
            c = Long.parseLong(main.getDealendtime()) - Long.parseLong(main.getDealbegintime());
        }
        lookDto.setAllTimeUsed(a+b+c);
        return lookDto;
    }

    @RequestMapping("/repairtwo/{uuid}")
    @ResponseBody
    String restartJob2(@PathVariable("uuid")String uuid){
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        if(main == null){
            return "未找到任务！";
        }
        List<Elastic_job_excel_sub> subList = excelSubService.getElastic_job_excel_subListByUuid(uuid);
        for (int i = 0; i < subList.size(); i++) {
            subList.get(i).setStatus("1");
            subList.get(i).setDealbegintime("0");
            subList.get(i).setErrorinfo("");
            subList.get(i).setDealendtime("0");
            subList.get(i).setTotal(0L);
        }
        excelSubService.saveAll(uuid,subList);
        main.setStatus("1");
        main.setDealbegintime("0");
        main.setDealendtime("0");
        main.setExtfield1("0");//解锁
        main.setRop(Tools.getRop(main,subList));
        excelMainService.update(main);
        String fpath = main.getExcelsplitpath();
        try {
            if (fpath != null && !"".equals(fpath)) {
                List<String> flist = Tools.lisFile(fpath);
                for (int i = 0; i < flist.size(); i++) {
                    String u = flist.get(i);
                    File f = new File(fpath,u);
                    f.renameTo(new File(fpath,u.replaceAll((Constant.SUB_FILE_SPLITER+JobConstant.EXCUTING_FLAG),"")));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "恢复成功";
    }

    @RequestMapping("/repairone/{uuid}")
    @ResponseBody
    String restartJob1(@PathVariable("uuid")String uuid){
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        if(main == null){
            return "未找到任务！";
        }
        excelSubService.delete(uuid);
        main.setStatus("0");
        main.setDealbegintime("0");
        main.setDealendtime("0");
        main.setConverttimeuse(0L);
        main.setSplittimeuse(0L);
        main.setErrorinfo("");
        main.setTotal(0L);
        main.setExcelsplitsize(0L);
        main.setExtfield1("0");//解锁
        main.setUpdatedate(new Date());
        main.setRop(0L);
        excelMainService.update(main);
        String fpath = main.getExcelsplitpath();
        try {
            if (fpath != null && !"".equals(fpath)) {
                List<String> flist = Tools.lisFile(fpath);
                for (int i = 0; i < flist.size(); i++) {
                    String u = flist.get(i);
                    File f = new File(fpath,u);
                    f.delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "恢复成功";
    }

    /**
     * 处理分非布式异常子任务
     * @return
     */
    @RequestMapping("/dealfailone/{uuid}")
    @ResponseBody
    String dealfailone(@PathVariable("uuid")String uuid){
        if(Tools.isEmpty(uuid)){
            return "请传入正确的参数！";
        }
        Elastic_job_excel_main main = excelMainService.getElastic_job_excel_main(uuid);
        if(main == null){
            return "请传入正确的参数！";
        }
        if(!"0".equals(main.getExtfield2())){
            return "非分布式任务不能通过此接口重调！";
        }
        if(!"-2".equals(main.getStatus())){
            return "当前任务状态无法重新执行！";
        }
        ExcelReadIntf intf = null;
        if(!Tools.isEmpty(main.getExtfield3())){
            intf = (ExcelReadIntf)ApplicationContextHelper.getBean(main.getExtfield3());
        }
        if(intf == null || Tools.isEmpty(main.getExtfield3())){
            return "内部错误，未知的空异常！";
        }
        DealerCallBackInfo dealerCallBackInfo = new DealerCallBackInfo();
        dealerCallBackInfo.setRedomain(main);
        intf.deal(dealerCallBackInfo);
        main = excelMainService.getElastic_job_excel_main(uuid);
        if(main.getStatus().equals("100")){
            return "执行成功！";
        }else{
            return "执行失败，失败原因："+main.getErrorinfo();
        }
    }


    /**
     * 获取最新校验（异常）信息
     * @return
     */
    @RequestMapping("/newestcheckinfo/{uuid}/{serialno}")
    @ResponseBody
    String getNewestCheckInfo(@PathVariable("uuid")String uuid,@PathVariable("serialno")String serialno){
        if(Tools.isEmpty(uuid) || Tools.isEmpty(serialno)){
            return "请传入正确的参数！";
        }
        String info = checkInfoService.getNewestDealerCallBackErrorInfo(uuid,serialno);
        if(Tools.isEmpty(info)){
            info = "无异常信息";
        }
        return info;
    }

}