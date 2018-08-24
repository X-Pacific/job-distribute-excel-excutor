package org.zxp.jobexcutor.autocheck.impl;

import org.zxp.extfile.csv.CsvHead;
import org.zxp.extfile.csv.autocheck.CheckService;
import org.zxp.extfile.csv.dto.CsvBaseDto;
import org.zxp.jobexcutor.aop.DealerCallBackErrorInfo;
import org.zxp.jobexcutor.autocheck.AutoCheck;
import org.zxp.jobexcutor.autocheck.AutoCheckField;
import org.zxp.jobexcutor.autocheck.AutoCheckFormat;
import org.zxp.jobexcutor.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class CheckServiceImpl implements CheckService<DealerCallBackErrorInfo> {
    private final static Logger logger = LoggerFactory.getLogger(CheckServiceImpl.class);

    private static final String date_format = "yyyy-MM-dd";
    private static final String time_format = "yyyy-MM-dd hh:mm:ss";


    public List<DealerCallBackErrorInfo> getCheckInfoList() {
        return checkInfoList;
    }

    public void setCheckInfoList(List<DealerCallBackErrorInfo> checkInfoList) {
        this.checkInfoList = checkInfoList;
    }

    private List<DealerCallBackErrorInfo> checkInfoList = new ArrayList<>();


    public static AutoCheckField getAnnotationFd(Field fd){
        if (fd.isAnnotationPresent(AutoCheckField.class)) {
            AutoCheckField annotation  = fd.getAnnotation(AutoCheckField.class);//获取到这个注解属性了
            return annotation;
        }else{
            return null;
        }
    }

    public static Object getMethod(Object obj, String filed) {
        try {
            Class clazz = obj.getClass();
            PropertyDescriptor pd = new PropertyDescriptor(filed, clazz);
            Method getMethod = pd.getReadMethod();//获得get方法
            if (pd != null) {
                Object o = getMethod.invoke(obj);//执行get方法返回一个Object
                return o;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String checkField(Object obj) throws Exception {
        if(obj != null){
            AutoCheck ac = obj.getClass().getAnnotation(AutoCheck.class);
            if(ac != null && ac.value()){
                //继续校验
            }else{
                return "";
            }
        }else{
           return "";
        }
        Class<?> objclass = obj.getClass();
        Field[] at = objclass.getDeclaredFields();
        if(at.length == 0 || (at.length == 1 && at[0].getName().equals("serialVersionUID"))){
            at = objclass.getSuperclass().getDeclaredFields();
        }
        StringBuffer sbreturn = new StringBuffer();
        for (Field fd : at) {
            fd.setAccessible(true);//私有属性
            //获取这个字段上是否包含BLAmicSffServiceAnnotation
            AutoCheckField annotation = getAnnotationFd(fd);
            if (annotation != null) {
                String value = getMethod(obj,fd.getName()).toString();//得到当前字段在对象中的值
                String fieldcname = Tools.isEmpty(annotation.cname())?fd.getName():annotation.cname();//字段解释(没配置默认为字段名称)
                boolean ischeckOne = false;
                //空类型校验
                if(!Tools.isEmpty(annotation.notNull()) && annotation.notNull()){
                    if(Tools.isEmpty(value)){
                        sbreturn.append(fieldcname+"不能为空！|");
                        if(ischeckOne){
                            return sbreturn.toString();
                        }
                    }
                }
                //选择范围校验
                if(!Tools.isEmpty(annotation.select())){
                    if(annotation.select().indexOf(","+value+",") < 0){
                        sbreturn.append(fieldcname+"不在范围内！|");
                        if(ischeckOne){
                            return sbreturn.toString();
                        }
                    }
                }
                //是否不能为0
                if(annotation.isNotZero()){
                    if(!Tools.isNumber(value)){
                        sbreturn.append(fieldcname+"不是数字类型！|");
                        if(ischeckOne){
                            return sbreturn.toString();
                        }
                    }else if(Double.parseDouble(value) == 0){
                        sbreturn.append(fieldcname+"不能为0！|");
                        if(ischeckOne){
                            return sbreturn.toString();
                        }
                    }
                }
                //格式校验配置
                if(!Tools.isEmpty(annotation.fieldformat()) && annotation.fieldformat() != AutoCheckFormat.none){
                    if(annotation.fieldformat() == AutoCheckFormat.date){
                        if(validDate(value,date_format)){
                            sbreturn.append(fieldcname+"格式不正确！|");
                            if(ischeckOne){
                                return sbreturn.toString();
                            }
                        }
                    }
                    if(annotation.fieldformat() == AutoCheckFormat.time){
                        if(validDate(value,time_format)){
                            sbreturn.append(fieldcname+"格式不正确！|");
                            if(ischeckOne){
                                return sbreturn.toString();
                            }
                        }
                    }
                    if(annotation.fieldformat() == AutoCheckFormat.num){
                        if(!Tools.isNumber(value)){
                            sbreturn.append(fieldcname+"格式不正确！|");
                            if(ischeckOne){
                                return sbreturn.toString();
                            }
                        }
                    }
                }
                //长度校验
                if(!Tools.isEmpty(annotation.length()) && annotation.length() != -1){
                    if(annotation.length() != value.length()){
                        sbreturn.append(fieldcname+"长度不正确，要求"+annotation.length()+"位！|");
                        if(ischeckOne){
                            return sbreturn.toString();
                        }
                    }
                }
            }
            //如果没配置注解不进行校验
        }
        String ret = sbreturn.toString();
        if(Tools.isEmpty(ret)){

        }else{
            DealerCallBackErrorInfo info = new DealerCallBackErrorInfo();
            info.setErrorInfo(ret);
            int index = 0;
            if(obj.getClass().getSuperclass() == CsvBaseDto.class){
                try{
                    Method m = obj.getClass().getSuperclass().getDeclaredMethod("getIndexnum");
                    index = Integer.parseInt(m.invoke(obj,null)+"");
                }catch (Exception e){
                    logger.error("",e);
                }
            }
            info.setIndex(index);//对excel序号进行赋值   原始excel==》csv==》拆分的csv==》解析到T的父类==》反射取出来
            //下面开始获取注解字段
            StringBuffer indexField = new StringBuffer("{");
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                AutoCheckField an = f.getAnnotation(AutoCheckField.class);
                if(an != null && an.disUniq()){
                    String disName = "";
                    CsvHead csvHead = f.getAnnotation(CsvHead.class);
                    if(csvHead != null){
                        disName = csvHead.value();
                    }else{
                        disName = f.getName();
                    }
                    String value = String.valueOf(getMethod(obj, f.getName()));
                    indexField.append(disName).append(":").append(value).append(",");
                }
            }
            String strIndexField = indexField.toString();
            if("{".equals(strIndexField)){
                strIndexField = "";
            }
            if(strIndexField.endsWith(",")){
                strIndexField = strIndexField.substring(0,strIndexField.length() - 1);
            }
            info.setIndexField(strIndexField+"}");
            this.checkInfoList.add(info);
        }
        return ret;
    }

    @Override
    public void addCheckInfo(DealerCallBackErrorInfo dealerCallBackErrorInfo) {
        checkInfoList.add(dealerCallBackErrorInfo);
    }

    public static boolean validDate(String str,String iformat) {
        boolean convertSuccess=true;
        SimpleDateFormat format = new SimpleDateFormat(iformat);
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (Exception e) {
            convertSuccess=false;
        }
        return convertSuccess;
    }
}
