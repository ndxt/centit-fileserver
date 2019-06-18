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
    //TODO add fix fields
    private String fileId;
    private String fileMd5;
    private String fileSize;


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
