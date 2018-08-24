package org.zxp.jobexcutor.util;

import org.zxp.extfile.util.Constant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHitter {
    /**
     * 命中一个待处理文件并改名
     *  EXCEL序号/总分片=当前分片-1时 命中excel
     *
     * @param path
     * @param totalSharding
     * @param currentSharding
     * @return
     * @throws IOException
     */
    public static HitFile hitFile(String path, int totalSharding, int currentSharding) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            return null;
        }
        if (!dirFile.isDirectory()) {
            return null;
        }
        HitFile res = new HitFile();
        String[] fileList = dirFile.list();
        List fileName = new ArrayList<String>();
        File hitFile = null;
        for (int i = 0; i < fileList.length; i++) {
            String string = fileList[i];
            File file = new File(dirFile.getPath(), string);
            String name = file.getName();
            if(name.indexOf(JobConstant.EXCUTING_FLAG) == -1){
                String serialNo = name.substring(0,name.length() - 4).split(Constant.SUB_FILE_SPLITER)[1];
                /**当 “EXCEL序号/总分片=当前分片”时 命中excel  当前分片就是正常序号（index-1）*/
                if((currentSharding) == Math.floorMod(Integer.parseInt(serialNo),totalSharding)){
                    hitFile = file;
                    res.setFilePath(path);
                    res.setOriFileName(hitFile.getName());
                    /**获取新名字并重命名，标识改文件当前正在执行,防止重读文件的发生*/
                    String newHitFileName = hitFile.getName().substring(0,hitFile.getName().lastIndexOf("."))+Constant.SUB_FILE_SPLITER+JobConstant.EXCUTING_FLAG+hitFile.getName().substring(hitFile.getName().lastIndexOf("."),hitFile.getName().length());
                    Tools.rename(hitFile,new File(path,newHitFileName));
                    res.setNewFileName(newHitFileName);
                    return res;
                }
            }
        }
        return null;
    }


    /**
     * 命中目标文件并改名
     * @param fpath
     * @param fname
     * @return
     * @throws IOException
     */
    public static HitFile hitGoalFile(String fpath,String fname) throws IOException {
        File hitFile = new File(fpath+File.separator + fname);
        if (!hitFile.exists()) {
            return null;
        }
        HitFile res = new HitFile();
        String name = hitFile.getName();
        if(name.indexOf(JobConstant.EXCUTING_FLAG) == -1){
            String serialNo = name.substring(0,name.length() - 4).split(Constant.SUB_FILE_SPLITER)[1];
            /**当 “EXCEL序号/总分片=当前分片”时 命中excel  当前分片就是正常序号（index-1）*/
            res.setFilePath(fpath);
            res.setOriFileName(fname);
            /**获取新名字并重命名，标识改文件当前正在执行,防止重读文件的发生*/
            String newHitFileName = hitFile.getName().substring(0,hitFile.getName().lastIndexOf("."))+Constant.SUB_FILE_SPLITER+JobConstant.EXCUTING_FLAG+hitFile.getName().substring(hitFile.getName().lastIndexOf("."),hitFile.getName().length());
            //变更为这种方式
            Tools.rename(hitFile,new File(fpath,newHitFileName));
            res.setNewFileName(newHitFileName);
            return res;
        }
        return null;
    }

    public static void main(String[] args){
//        File hitFile = new File("D:\\eeee\\gencsv\\e2.csv");
//        System.out.println(hitFile.getName());
//        String newHitFileName = hitFile.getName().substring(0,hitFile.getName().lastIndexOf("."))+JobConstant.EXCUTING_FLAG+hitFile.getName().substring(hitFile.getName().lastIndexOf("."),hitFile.getName().length());
//        System.out.println(newHitFileName);

        System.out.println(Math.floorMod(10,4));
    }
    // 4个分片
//        1
//        2
//        3
//        4
    // 核心算法
// EXCEL序号/总分片=当前分片-1
//            1/4  =1
//            2/4  =2
//            3/4  =3
//            4/4  =0
//            5/4  =1
    //        11/4
}
