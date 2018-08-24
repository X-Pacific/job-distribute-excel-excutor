package org.zxp.jobexcutor.util;

import org.zxp.jobexcutor.entity.Elastic_job_excel_main;
import org.zxp.jobexcutor.entity.Elastic_job_excel_sub;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tools {

    /**
     * 获取目录下所有文件的名称列表
     *
     * @param path
     * @return
     */
    public static List<String> lisFile(String path) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            return null;
        }
        if (!dirFile.isDirectory()) {
            return null;
        }
        String[] fileList = dirFile.list();
        List fileName = new ArrayList<String>();

        for (int i = 0; i < fileList.length; i++) {
            //遍历文件目录
            String string = fileList[i];
            //File("documentName","fileName")是File的另一个构造器
            File file = new File(dirFile.getPath(), string);
            String name = file.getName();
            //如果是一个目录，搜索深度depth++，输出目录名后，进行递归
            fileName.add(name);
        }
        return fileName;
    }


    public static long getNowLongDate() {
        return new Date().getTime();
    }


    /**
     * 获取进度值
     *
     * @param main
     * @param subList
     * @return
     */
    public static long getRop(Elastic_job_excel_main main, List<Elastic_job_excel_sub> subList) {
        int baseNum = 0;
        if (main.getExcelsplitsize() == null || "".equals(main.getExcelsplitsize())) {
            return 0;
        }
        double allNum = 0;
        try {
            allNum = Double.parseDouble((main.getExcelsplitsize() + 2) + "");
        } catch (Exception e) {
            return 0;
        }
        if (main.getConverttimeuse() != null && !"".equals(main.getConverttimeuse())) {
            baseNum++;
        }
        if (main.getSplittimeuse() != null && !"".equals(main.getSplittimeuse())) {
            baseNum++;
        }
        if (main.getDealbegintime() != null && !"".equals(main.getSplittimeuse())) {
            for (int i = 0; i < subList.size(); i++) {
                /**成功或失败都是已经处理了的*/
                if ("2".equals(subList.get(i).getStatus()) || "3".equals(subList.get(i).getStatus())) {
                    baseNum++;
                }
            }
        }
        double rop = (baseNum / allNum) * 100;
        return Long.parseLong(Integer.parseInt((int) Math.floor(rop) + "") + "");
    }

    /**
     * 删除文件夹
     *
     * @param folderPath
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    static Map<String, File> mmm = new HashMap<String, File>();

    public static void main(String[] args) {
        final File f = new File("D://");
        mmm.put("1", f);
        File d = new File("C://");
        mmm.put("2", d);

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                dooooo("1");
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                dooooo("2");
            }
        };

        ExecutorService e = Executors.newFixedThreadPool(5);
        e.execute(r1);
        e.execute(r2);
    }
    public static void dooooo(String num) {
        synchronized (mmm.get(num)) {
            try {
                if(num.equals("1")){
                    Thread.sleep(3000);
                }else{
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程" + num + "已经运行完毕");
        }
    }

    public static boolean isEmpty(Object obj){
        if(obj instanceof String){
            if(obj == null || "".equals(String.valueOf(obj))){
                return true;
            }
        }else{
            if(obj == null){
                return true;
            }
        }
        return false;
    }


    /**
     * 校验是否为数字
     * @param value
     * @return
     */
    public static boolean isNumber(String value){
        try{
            double number = Double.parseDouble(value);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public static void copy(File f1, File f2) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f1));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f2));
        int len = 0;
        byte[] b = new byte[1024];
        while ((len = bis.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        bis.close();
        bos.close();
    }

    public static void rename(File source, File target) throws IOException{
        copy(source,target);
        source.delete();
    }

}
