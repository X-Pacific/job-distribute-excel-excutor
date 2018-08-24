package org.zxp.jobexcutor.util;

public class JobConstant {
    public static final int DEFAULT_SPLIT_SIZE = 1;
    /**
     * 更新进度的最大循环数，防止死循环
     */
//    public static final int MAX_LOOP = 1000;

    public static final String EXCUTING_FLAG = "doingdoingdoing";
    /**
     * 给主任务加锁，最大尝试失败次数
     */
    public static final int MAX_TRY_COUNT = 100;

    /**
     * 默认释放锁的时间
     * DB方式目前使用
     */
    public static final long RECOVER_TIME = 10*1000;

    public static final String PREFIX = "`";

    /**
     * excel分布式处理阶段，日志输出专用
     */
    public static final String CSV_COVERT = "[csv转化]";
    public static final String CSV_SPLIT = "[csv拆分]";
    public static final String CSV_AOP_A1 = "[csv处理环绕切面-前]";
    public static final String CSV_CUSTOM = "[csv处理-用户处理]";
    public static final String CSV_AOP_A2 = "[csv处理环绕切面-后]";

}
