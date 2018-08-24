package org.zxp.jobexcutor.controller.vo;

import java.io.Serializable;

public class LookVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String rop = "";
    private String uuid = "";
    private String type = "";
    private String riskcode = "";
    private String excelpath = "";
    private String total = "";
    private String status = "";
    private long allTimeUsed = 0L;
    private String isLock = "0";
    private String errorinfo = "";

    public String getRop() {
        return rop;
    }

    public void setRop(String rop) {
        this.rop = rop;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRiskcode() {
        return riskcode;
    }

    public void setRiskcode(String riskcode) {
        this.riskcode = riskcode;
    }

    public String getExcelpath() {
        return excelpath;
    }

    public void setExcelpath(String excelpath) {
        this.excelpath = excelpath;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAllTimeUsed() {
        return allTimeUsed;
    }

    public void setAllTimeUsed(long allTimeUsed) {
        this.allTimeUsed = allTimeUsed;
    }

    public String getIsLock() {
        return isLock;
    }

    public void setIsLock(String isLock) {
        this.isLock = isLock;
    }

    public String getErrorinfo() {
        return errorinfo;
    }

    public void setErrorinfo(String errorinfo) {
        this.errorinfo = errorinfo;
    }
}
