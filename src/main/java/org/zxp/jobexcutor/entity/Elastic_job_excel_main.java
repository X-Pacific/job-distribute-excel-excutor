package org.zxp.jobexcutor.entity;

import java.util.Date;

public class Elastic_job_excel_main {
    private String uuid;

    private String riskcode;

    private String prodtype;

    private String exceloriname;

    private Long excelsplitsize;

    private String exceloripath;

    private String excelsplitpath;

    private String status;

    private String flag;

    private String errorinfo;

    private Long converttimeuse;

    private Long splittimeuse;

    private String dealbegintime;

    private String dealendtime;

    private Long rop;

    private Long total;

    private String extfield1;

    private String extfield2;

    private String extfield3;

    private String extfield4;

    private Date inputdate;

    private Date updatedate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getRiskcode() {
        return riskcode;
    }

    public void setRiskcode(String riskcode) {
        this.riskcode = riskcode == null ? null : riskcode.trim();
    }

    public String getProdtype() {
        return prodtype;
    }

    public void setProdtype(String prodtype) {
        this.prodtype = prodtype == null ? null : prodtype.trim();
    }

    public String getExceloriname() {
        return exceloriname;
    }

    public void setExceloriname(String exceloriname) {
        this.exceloriname = exceloriname == null ? null : exceloriname.trim();
    }

    public Long getExcelsplitsize() {
        return excelsplitsize;
    }

    public void setExcelsplitsize(Long excelsplitsize) {
        this.excelsplitsize = excelsplitsize;
    }

    public String getExceloripath() {
        return exceloripath;
    }

    public void setExceloripath(String exceloripath) {
        this.exceloripath = exceloripath == null ? null : exceloripath.trim();
    }

    public String getExcelsplitpath() {
        return excelsplitpath;
    }

    public void setExcelsplitpath(String excelsplitpath) {
        this.excelsplitpath = excelsplitpath == null ? null : excelsplitpath.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag == null ? null : flag.trim();
    }

    public String getErrorinfo() {
        return errorinfo;
    }

    public void setErrorinfo(String errorinfo) {
        this.errorinfo = errorinfo == null ? null : errorinfo.trim();
    }

    public Long getConverttimeuse() {
        return converttimeuse;
    }

    public void setConverttimeuse(Long converttimeuse) {
        this.converttimeuse = converttimeuse;
    }

    public Long getSplittimeuse() {
        return splittimeuse;
    }

    public void setSplittimeuse(Long splittimeuse) {
        this.splittimeuse = splittimeuse;
    }

    public String getDealbegintime() {
        return dealbegintime;
    }

    public void setDealbegintime(String dealbegintime) {
        this.dealbegintime = dealbegintime == null ? null : dealbegintime.trim();
    }

    public String getDealendtime() {
        return dealendtime;
    }

    public void setDealendtime(String dealendtime) {
        this.dealendtime = dealendtime == null ? null : dealendtime.trim();
    }

    public Long getRop() {
        return rop;
    }

    public void setRop(Long rop) {
        this.rop = rop;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getExtfield1() {
        return extfield1;
    }

    public void setExtfield1(String extfield1) {
        this.extfield1 = extfield1 == null ? null : extfield1.trim();
    }

    public String getExtfield2() {
        return extfield2;
    }

    public void setExtfield2(String extfield2) {
        this.extfield2 = extfield2 == null ? null : extfield2.trim();
    }

    public String getExtfield3() {
        return extfield3;
    }

    public void setExtfield3(String extfield3) {
        this.extfield3 = extfield3 == null ? null : extfield3.trim();
    }

    public String getExtfield4() {
        return extfield4;
    }

    public void setExtfield4(String extfield4) {
        this.extfield4 = extfield4 == null ? null : extfield4.trim();
    }

    public Date getInputdate() {
        return inputdate;
    }

    public void setInputdate(Date inputdate) {
        this.inputdate = inputdate;
    }

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }
}