package org.zxp.jobexcutor.jobhandler.split;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExcelSplitConfig {

    @Value("${ExcelSplitJobHandler.PRODTYPE}")
    private String PRODTYPE;
    @Value("${ExcelSplitJobHandler.RISKCODE}")
    private String RISKCODE;

    /**
     * 根据配置组织可以fetch到的拆分excel的任务条件
     * @return
     */
    public Elastic_job_excel_main genFetchCondition() {
        Elastic_job_excel_main mainCondtion = new Elastic_job_excel_main();
        if (PRODTYPE != null && !"".equals(PRODTYPE)) {
            mainCondtion.setProdtype(PRODTYPE);
        }
        if (RISKCODE != null && !"".equals(RISKCODE)) {
            mainCondtion.setRiskcode(RISKCODE);
        }
        return mainCondtion;
    }


}
