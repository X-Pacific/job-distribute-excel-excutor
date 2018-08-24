package org.zxp.jobexcutor.aop;

/**
 * csv处理错误对象
 * @param <T>
 */
public class DealerCallBackErrorInfo<T> {
    //对应excel中的序号
    int index = 0;
    //excel序号对应的逻辑主键
    String indexField = "";
    //匹配码
    String checkcode = "";
    //错误原因
    String errorInfo = "";

    public String getSubFileName() {
        return subFileName;
    }

    public void setSubFileName(String subFileName) {
        this.subFileName = subFileName;
    }

    //csv文件路径
    String subFileName = "";
    //对应csv数据载体
    T t;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getIndexField() {
        return indexField;
    }

    public void setIndexField(String indexField) {
        this.indexField = indexField;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode;
    }
}
