package org.zxp.jobexcutor.csvdto;

import org.zxp.extfile.csv.CsvHead;
import org.zxp.extfile.csv.dto.CsvBaseDto;
import org.zxp.jobexcutor.autocheck.AutoCheck;
import org.zxp.jobexcutor.autocheck.AutoCheckField;

import java.lang.reflect.Method;

/**
 * 只能有String类型字段
 * 这是一个样例，csvhead注解配置内容为csv的title（注意大小写要一致）
 */
@AutoCheck
public class DemoJobDto extends CsvBaseDto {
    @CsvHead("POLICYNO")
    @AutoCheckField(cname = "保单号",disUniq = true,notNull = true)
    private String policyno;

    @CsvHead("CLASSCODE")
    private String classcode;

    @CsvHead("RISKCODE")
    @AutoCheckField(cname = "险种",disUniq = true,notNull = true)
    private String riskcode;

    @CsvHead("PROPOSALNO")
    private String proposalno;

    @CsvHead("CONTRACTNO")
    private String contractno;

    @CsvHead("POLICYSORT")
    private String policysort;

    @CsvHead("PRINTNO")
    private String printno;

    @CsvHead("BUSINESSNATURE")
    private String businessnature;

    @CsvHead("LANGUAGE")
    private String language;

    @CsvHead("POLICYTYPE")
    private String policytype;

    @CsvHead("APPLICODE")
    private String applicode;

    @CsvHead("APPLINAME")
    private String appliname;

    @CsvHead("APPLIADDRESS")
    private String appliaddress;

    @CsvHead("INSUREDCODE")
    private String insuredcode;

    @CsvHead("INSUREDNAME")
    private String insuredname;

    @CsvHead("INSUREDADDRESS")
    private String insuredaddress;


    public String getPolicyno() {
        return policyno;
    }

    public void setPolicyno(String policyno) {
        this.policyno = policyno;
    }

    public String getClasscode() {
        return classcode;
    }

    public void setClasscode(String classcode) {
        this.classcode = classcode;
    }

    public String getRiskcode() {
        return riskcode;
    }

    public void setRiskcode(String riskcode) {
        this.riskcode = riskcode;
    }

    public String getProposalno() {
        return proposalno;
    }

    public void setProposalno(String proposalno) {
        this.proposalno = proposalno;
    }

    public String getContractno() {
        return contractno;
    }

    public void setContractno(String contractno) {
        this.contractno = contractno;
    }

    public String getPolicysort() {
        return policysort;
    }

    public void setPolicysort(String policysort) {
        this.policysort = policysort;
    }

    public String getPrintno() {
        return printno;
    }

    public void setPrintno(String printno) {
        this.printno = printno;
    }

    public String getBusinessnature() {
        return businessnature;
    }

    public void setBusinessnature(String businessnature) {
        this.businessnature = businessnature;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPolicytype() {
        return policytype;
    }

    public void setPolicytype(String policytype) {
        this.policytype = policytype;
    }

    public String getApplicode() {
        return applicode;
    }

    public void setApplicode(String applicode) {
        this.applicode = applicode;
    }

    public String getAppliname() {
        return appliname;
    }

    public void setAppliname(String appliname) {
        this.appliname = appliname;
    }

    public String getAppliaddress() {
        return appliaddress;
    }

    public void setAppliaddress(String appliaddress) {
        this.appliaddress = appliaddress;
    }

    public String getInsuredcode() {
        return insuredcode;
    }

    public void setInsuredcode(String insuredcode) {
        this.insuredcode = insuredcode;
    }

    public String getInsuredname() {
        return insuredname;
    }

    public void setInsuredname(String insuredname) {
        this.insuredname = insuredname;
    }

    public String getInsuredaddress() {
        return insuredaddress;
    }

    public void setInsuredaddress(String insuredaddress) {
        this.insuredaddress = insuredaddress;
    }

    public static void main(String[] args){
        DemoJobDto obj = new DemoJobDto();
        try {
            Method m = obj.getClass().getSuperclass().getDeclaredMethod("setIndexnum",String.class);
            String a = "222";
            m.invoke(obj,a);
            System.out.println(obj.getIndexnum());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
