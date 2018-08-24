package org.zxp.jobexcutor.controller;

import org.zxp.jobexcutor.jobhandler.handler.ExcelDistributedReadDemoJobHandler;
import org.zxp.jobexcutor.jobhandler.handler.ExcelReadDemoHandler;
import org.zxp.jobexcutor.jobhandler.split.ExcelSplitJobHandler;
import org.zxp.jobexcutor.service.DemoJobService;
import org.zxp.jobexcutor.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;

@Controller
@RequestMapping("/test")
@EnableAutoConfiguration
public class TestController {
    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    DemoJobService demoJobService;
    @Autowired
    ExcelDistributedReadDemoJobHandler excelDistributedReadDemoJobHandler;
    @Autowired
    ExcelSplitJobHandler excelSplitJobHandler;
    @Autowired
    ExcelReadDemoHandler excelReadDemoHandler;

    @RequestMapping("/testOneInsert")
    @ResponseBody
    String insert() {
        excelReadDemoHandler.execute(null);
        return "";
//        Date begin = new Date();
////        String fileurl = "/home/xxl/excel/e2.xlsx";//这是excel的路径
//        String fileurl = "D:\\eeee\\gencsv\\e2.xlsx";//这是excel的路径
//        InputStream inputStream = null;
//        try {
//            inputStream = new FileInputStream(new File(fileurl));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            ExcelReader reader = new ExcelReader(inputStream, ExcelTypeEnum.XLSX, demoJobService,
//                    new AnalysisEventListener<List<String>>() {
//                        StringBuffer sb = new StringBuffer();
//                        private int index = 0;
//                        ArrayList<PrpCMainDemo> list = new ArrayList<PrpCMainDemo>();
//
//                        @Override
//                        public void invoke(List<String> object, AnalysisContext context) {
//                            ++index;
//                            if(index != 1){
//                                PrpCMainDemo d = new PrpCMainDemo();
//                                d.setPolicyno(object.get(0));
//                                d.setRiskcode(object.get(1));
//                                list.add(d);
//                            }
//                        }
//
//                        @Override
//                        public void doAfterAllAnalysed(AnalysisContext context) {
//                            try {
//                                demoJobService.saveAll(list);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//            reader.read();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                inputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Date end = new Date();
//            System.out.println(end.getTime()-begin.getTime());
//        }
//        return null;
    }


    @RequestMapping("/testSplit")
    @ResponseBody
    String split() {
        excelSplitJobHandler.execute(null);
        return "123";
    }

    @RequestMapping("/testDeal")
    @ResponseBody
    String deal() {
        excelDistributedReadDemoJobHandler.execute(null);
        return "123";
    }


    @RequestMapping("/rename")
    @ResponseBody
    String rename() throws Exception {
        Tools.rename(new File("D:\\eeee\\gencsv\\e2.xlsx_WINDOWS\\a.xlsx"),new File("D:\\eeee\\gencsv\\e2.xlsx_WINDOWS\\a1.xlsx"));
        return "111";
    }
}
