package org.zxp.jobexcutor.entity;

import java.util.Date;

public class Elastic_job_excel_sub extends Elastic_job_excel_subKey {
    private String excelsplitname;

    private String excelsplitpath;

    private String status;

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode;
    }

    private String checkcode;

    private String errorinfo;

    private String dealbegintime;

    private String dealendtime;

    private String flag;

    private Long total;

    private Date inputdate;

    private Date updatedate;

    public String getExcelsplitname() {
        return excelsplitname;
    }

    public void setExcelsplitname(String excelsplitname) {
        this.excelsplitname = excelsplitname == null ? null : excelsplitname.trim();
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

    public String getErrorinfo() {
        return errorinfo;
    }

    public void setErrorinfo(String errorinfo) {
        this.errorinfo = errorinfo == null ? null : errorinfo.trim();
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

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag == null ? null : flag.trim();
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
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