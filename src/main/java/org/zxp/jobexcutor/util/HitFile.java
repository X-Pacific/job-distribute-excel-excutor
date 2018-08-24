package org.zxp.jobexcutor.util;

public class HitFile {
    private String oriFileName ;//原文件名称

    public String getOriFileName() {
        return oriFileName;
    }

    @Override
    public String toString() {
        return "HitFile{" +
                "oriFileName='" + oriFileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", newFileName='" + newFileName + '\'' +
                '}';
    }

    public void setOriFileName(String oriFileName) {
        this.oriFileName = oriFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    private String filePath;//文件路径
    private String newFileName ;//新文件名称
}
