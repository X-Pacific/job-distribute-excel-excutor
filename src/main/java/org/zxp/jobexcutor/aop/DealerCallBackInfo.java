package org.zxp.jobexcutor.aop;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;

import java.util.ArrayList;
import java.util.List;

/**
 * 1、本类为回调对象，框架会自动进行赋值
 * 2、T是csv数据列表的对象泛型
 */
public class DealerCallBackInfo<T> {
    /**
     * 请不要随意使用该字段，否则可能出现不可预知的问题，运维工具使用-分布式
     */
    private Elastic_job_excel_sub redosub = null;

    public Elastic_job_excel_main getRedomain() {
        return redomain;
    }

    public void setRedomain(Elastic_job_excel_main redomain) {
        this.redomain = redomain;
    }

    /**
     * 请不要随意使用该字段，否则可能出现不可预知的问题，运维工具使用-非分布式
     */
    private Elastic_job_excel_main redomain = null;

    public Elastic_job_excel_sub getRedosub() {
        return redosub;
    }

    public void setRedosub(Elastic_job_excel_sub redosub) {
        this.redosub = redosub;
    }

    /**
     * 命中子文件路径+名字
     */
    private String hitFileName = "";
    /**
     * 命中主任务的主键uuid
     */
    private String uuid = "" ;
    /**
     * 业务处理发生的异常需要将异常信息set到这个字段，以便框架能够获取更准确的异常信息
     */
    private String errorInfo = "";
    /**
     *  读取csv的数据列表，由框架自动读取供开发人员直接使用
     *  备注：返回csv数据列表 直接通过入参通过aop传"调用"传不进来-_-!
     */
    private List<T> csvList = new ArrayList<T>();
    //回调类 也可以通过sping的方式设定
    ExcelDistributedCallBackIntf excelDistributedCallBackIntf;
    //校验信息
    private List<DealerCallBackErrorInfo<T>> checkInfoList;

    public List<DealerCallBackErrorInfo<T>> getCheckInfoList() {
        return checkInfoList;
    }

    public void setCheckInfoList(List<DealerCallBackErrorInfo<T>> checkInfoList) {
        this.checkInfoList = checkInfoList;
    }

    public ExcelDistributedCallBackIntf getExcelDistributedCallBackIntf() {
        return excelDistributedCallBackIntf;
    }

    public void setExcelDistributedCallBackIntf(ExcelDistributedCallBackIntf excelDistributedCallBackIntf) {
        this.excelDistributedCallBackIntf = excelDistributedCallBackIntf;
    }



    public List<T> getCsvList() {
        return csvList;
    }

    public void setCsvList(List<T> csvList) {
        this.csvList = csvList;
    }





    public String getHitFileName() {
        return hitFileName;
    }

    public void setHitFileName(String hitFileName) {
        this.hitFileName = hitFileName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
