package org.zxp.jobexcutor.entity;

public class Elastic_job_excel_checkinfoKey {
    private String uuid;

    private Long serialno;

    private String checkcode;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public Long getSerialno() {
        return serialno;
    }

    public void setSerialno(Long serialno) {
        this.serialno = serialno;
    }

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode == null ? null : checkcode.trim();
    }
}