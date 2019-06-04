package com.centit.fileserver.utils;

import java.io.Serializable;

public class FileStoreTaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TaskType {SAVE_FILE, PRETREAT_FILE}

    private TaskType taskType;
//    private String fileId;
    private Serializable taskInfo;

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

//    public String getFileId() {
//        return fileId;
//    }
//
//    public void setFileId(String fileId) {
//        this.fileId = fileId;
//    }

    public void setTaskInfo(Serializable taskInfo) {
        this.taskInfo = taskInfo;
    }

    public Serializable getTaskInfo() {
        return taskInfo;
    }
}
