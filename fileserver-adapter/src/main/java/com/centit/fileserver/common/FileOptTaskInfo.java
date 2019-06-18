package com.centit.fileserver.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileOptTaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int OPT_SAVE_FILE = 1;
    public static final int OPT_CREATE_PDF = 2;
    public static final int OPT_PDF_WATERMARK = 3;
    public static final int OPT_ADD_THUMBNAIL = 4;
    public static final int OPT_ZIP = 5;
    public static final int OPT_ENCRYPT_ZIP = 6;
    public static final int OPT_AES_ENCRYPT = 7;
    public static final int OPT_DOCUMENT_INDEX = 8;

    private int taskType;
    private String fileId;
    private String fileMd5;
    private Long fileSize;
    // add other fixed fields


    private Map<String, Object> taskOptParams;

    public FileOptTaskInfo() {}

    public FileOptTaskInfo(int taskType) {
        this.taskType = taskType;
        taskOptParams = new HashMap<>();
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Map<String, Object> getTaskOptParams() {
        return taskOptParams;
    }

    public void setTaskOptParams(Map<String, Object> taskOptParams) {
        this.taskOptParams = taskOptParams;
    }

    public void setTaskOptParam(String key, Object value) {
        taskOptParams.put(key, value);
    }
}
