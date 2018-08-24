package org.zxp.jobexcutor.service.impl;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;
import org.zxp.jobexcutor.mapper.Elastic_job_excel_mainMapper;
import org.zxp.jobexcutor.service.ExcelMainService;
import org.zxp.jobexcutor.service.ExcelSubService;
import org.zxp.jobexcutor.util.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("excelMainService")
public class ExcelMainServiceImpl implements ExcelMainService{
    @Autowired
    Elastic_job_excel_mainMapper mapper;
    @Autowired
    ExcelSubService excelSubService;

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void setErrorInfoAndSave(String uuid, String errorInfo) {
        Elastic_job_excel_main main = mapper.selectByPrimaryKey(uuid);
        main.setErrorinfo(errorInfo);//暂时不管状态，等待下次重新执行
        main.setUpdatedate(new Date());
        mapper.updateByPrimaryKey(main);//更新失败原因
    }

    @Override
    public Elastic_job_excel_main getElastic_job_excel_main(String uuid) {
        if(uuid == null || "".equals(uuid)){
            throw  new NullPointerException();
        }
        return mapper.selectByPrimaryKey(uuid);
    }

    @Override
    public Elastic_job_excel_main findOneSplitExcelJob() {
        return mapper.findOneSplitExcelJob();
    }

    @Override
    public Elastic_job_excel_main findOneSplitExcelJobByType(Elastic_job_excel_main record) {
        return mapper.findOneSplitExcelJobByType(record);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public int update(Elastic_job_excel_main record) {
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public int updateByLock(Elastic_job_excel_main record) {
        return mapper.updateByLock(record);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public Elastic_job_excel_main fetchOneSplitExcelJob() {
        Elastic_job_excel_main main = findOneSplitExcelJob();
        if(main == null){
            return null;
        }
        /**excel处理状态 0未处理 |-1excel拆分处理失败 1excel拆分完成  10excel拆分中|-2excel分布处理失败 2excel分布处理完成 20excel分布处理中|100全部处理完成*/
        main.setStatus("10");//设置中间状态
        update(main);
        return main;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Elastic_job_excel_main fetchOneSplitExcelJobByType(Elastic_job_excel_main record) {
        Elastic_job_excel_main main = findOneSplitExcelJobByType(record);
        if(main == null){
            return null;
        }
        /**excel处理状态 0未处理 |-1excel拆分处理失败 1excel拆分完成  10excel拆分中|-2excel分布处理失败 2excel分布处理完成 20excel分布处理中|100全部处理完成*/
        main.setStatus("10");//设置中间状态
        update(main);
        return main;
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void updateRopMain(String uuid,List<Elastic_job_excel_sub> subList) {
        long ropNew = 0l;
        Elastic_job_excel_main mainOld = mapper.selectByPrimaryKey(uuid);//根据当前主键获取数据库最新情况
        mainOld.setUpdatedate(new Date());
        ropNew = Tools.getRop(mainOld,subList);
        mainOld.setRop(ropNew);
        update(mainOld);
    }

    @Override
    public Elastic_job_excel_main fetchOneReadExcelJobByCondtion(Elastic_job_excel_main record) {
        String uuid = record.getUuid();
        boolean isFirstDeal = false;//是否首次处理，如果是，则记录begintime
        /**先找进行中的任务*/
        Elastic_job_excel_main main = mapper.findOneReadExcelJobByType20(record);
        if(main == null){
            main = mapper.findOneReadExcelJobByType1(record);
            if(main == null){
                return null;
            }else{
                isFirstDeal = true;
            }
        }
        /**excel处理状态 0未处理 |-1 excel拆分处理失败 1excel拆分完成  10excel拆分中|-2excel分布处理失败 2excel分布处理完成(有异常情况，部分失败、主子不一致等) 20excel分布处理中|100excel分布处理完成(全部成功)*/
        if(!"20".equals(main.getStatus())) {
            main.setStatus("20");//设置中间状态
            if(isFirstDeal){//如果是首次，设置开始处理时间
                main.setDealbegintime(new Date().getTime()+"");
            }
            main.setUpdatedate(new Date());
            update(main);
        }
        return main;
    }

    @Override
    public Elastic_job_excel_main findOneReadExcelJobByTypeOp(Elastic_job_excel_main record) {
        return mapper.findOneReadExcelJobByTypeOp(record);
    }

    @Override
    public Elastic_job_excel_main fetchOneReadExcelJobByCondtion_notdis(Elastic_job_excel_main record) {
        String uuid = record.getUuid();
        boolean isFirstDeal = false;//是否首次处理，如果是，则记录begintime
        /**先找进行中的任务*/
        Elastic_job_excel_main main = mapper.findOneReadExcelJobByType0_notdis(record);
        /**excel处理状态 0未处理 |-1 excel拆分处理失败 1excel拆分完成  10excel拆分中|-2excel分布处理失败 2excel分布处理完成(有异常情况，部分失败、主子不一致等) 20excel分布处理中|100excel分布处理完成(全部成功)*/
        if(!"20".equals(main.getStatus())) {
            main.setStatus("20");//设置中间状态
            if(isFirstDeal){//如果是首次，设置开始处理时间
                main.setDealbegintime(new Date().getTime()+"");
            }
            main.setUpdatedate(new Date());
            update(main);
        }
        return main;
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public boolean checkIsFinishAndWriteStatus(String uuid, boolean isCheckSumAmount) {
        Elastic_job_excel_main main = mapper.selectByPrimaryKey(uuid);
        if("100".equals(main.getStatus())){
            main.setRop(100L);
            mapper.updateByPrimaryKey(main);
            return true;
        }
        /**检查主任务状态  可产出状态 20   2 100 begin**/
        String mainStatus = "20";//主任务状态，默认为处理中
        //excel处理状态 1生成|2读取处理完成|3失败|4读取中
//        int countOfStatus1 = 0;//生成状态的个数
        int countOfStatus2 = 0;//读取处理完成状态的个数
        int countOfStatus3 = 0;//失败状态的个数
//        int countOfStatus4 = 0;//生读取中的个数
        countOfStatus2 = excelSubService.getCountByStatus(uuid,"2");
        countOfStatus3 = excelSubService.getCountByStatus(uuid,"3");
        int countOfAll = Integer.parseInt(main.getExcelsplitsize()+"");//总个数
        //如果成功或失败的个数合等于总数，那么说明任务已经处理完毕
        if((countOfStatus3+countOfStatus2) == countOfAll){
            main.setDealendtime(new Date().getTime() + "");
            if(countOfStatus3 >  0){
                mainStatus = "2";//成功但有失败
            }else{
                //检查主子一致性
                if(isCheckSumAmount) {
                    long mainTotal = main.getTotal();
                    long subSumTotal = excelSubService.getSubSumTotal(uuid);
                    if(mainTotal != subSumTotal){
                        mainStatus = "2";//成功但主子不一致
                    }else{
                        mainStatus = "100";//全部成功
                    }
                }else{
                    mainStatus = "100";//全部成功
                }
            }
            main.setUpdatedate(new Date());
            main.setStatus(mainStatus);
            mapper.updateByPrimaryKey(main);
            if("100".equals(mainStatus)) {
                return true;
            }else{
                return false;
            }
        }
        main.setUpdatedate(new Date());
        main.setStatus(mainStatus);
        mapper.updateByPrimaryKey(main);
        return false;
        /**检查主任务状态  可产出状态 20   2 100 begin**/
    }

    @Override
    public List<Elastic_job_excel_main> getMainList(Elastic_job_excel_main record) {
        return mapper.selectMainList(record);
    }

}
