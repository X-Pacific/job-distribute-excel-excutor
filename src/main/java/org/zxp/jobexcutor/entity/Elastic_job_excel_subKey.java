package org.zxp.jobexcutor.entity;

public class Elastic_job_excel_subKey {
    private String uuid;

    private Long serialno;

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
}