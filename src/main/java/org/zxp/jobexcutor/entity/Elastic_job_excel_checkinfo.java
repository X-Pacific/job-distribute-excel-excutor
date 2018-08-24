package org.zxp.jobexcutor.entity;

import java.util.Date;

public class Elastic_job_excel_checkinfo {
    private String checkinfouuid;

    private String mainuuid;

    private Long serialno;

    private String filename;

    private String errorinfo;

    private String checkcode;

    private String indexnum;

    private String indexfield;

    private String flag;

    private Date inputdate;

    public String getCheckinfouuid() {
        return checkinfouuid;
    }

    public void setCheckinfouuid(String checkinfouuid) {
        this.checkinfouuid = checkinfouuid == null ? null : checkinfouuid.trim();
    }

    public String getMainuuid() {
        return mainuuid;
    }

    public void setMainuuid(String mainuuid) {
        this.mainuuid = mainuuid == null ? null : mainuuid.trim();
    }

    public Long getSerialno() {
        return serialno;
    }

    public void setSerialno(Long serialno) {
        this.serialno = serialno;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public String getErrorinfo() {
        return errorinfo;
    }

    public void setErrorinfo(String errorinfo) {
        this.errorinfo = errorinfo == null ? null : errorinfo.trim();
    }

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode == null ? null : checkcode.trim();
    }

    public String getIndexnum() {
        return indexnum;
    }

    public void setIndexnum(String indexnum) {
        this.indexnum = indexnum == null ? null : indexnum.trim();
    }

    public String getIndexfield() {
        return indexfield;
    }

    public void setIndexfield(String indexfield) {
        this.indexfield = indexfield == null ? null : indexfield.trim();
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag == null ? null : flag.trim();
    }

    public Date getInputdate() {
        return inputdate;
    }

    public void setInputdate(Date inputdate) {
        this.inputdate = inputdate;
    }
}