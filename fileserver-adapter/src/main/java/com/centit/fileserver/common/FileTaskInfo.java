package com.centit.fileserver.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * @author codefan@sina.com
 */
@Data
public class FileTaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskType;
    private String fileId;
    private String fileMd5;
    private String fileName;

    private Long fileSize;

    private Map<String, Object> taskOptParams;

    public FileTaskInfo(String taskType) {
        this.taskType = taskType;
        taskOptParams = new HashMap<>();
    }

    public void putOptParam(String name, Object value) {
        if(value!=null) {
            taskOptParams.put(name, value);
        }
    }

    public Object getOptParam(String name) {
        return taskOptParams.get(name);
    }

    public void copy(FileBaseInfo otherFile){
        fileId = otherFile.getFileId();
        fileMd5 = otherFile.getFileMd5();
        fileName = otherFile.getFileName();
    }

}

