package com.centit.fileserver.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class FileTaskInfo implements /*FileBaseInfo,*/ Serializable {
    private static final long serialVersionUID = 1L;

    private String taskType;
    private String fileId;
    private String fileMd5;
    private String fileType;
    private String fileName;
    private String osId;
    private String optId;
    private String fileOwner;
    private String fileUnit;
    private String libraryId;

    private Long fileSize;

    private Map<String, Object> taskOptParams;

    public FileTaskInfo(String taskType) {
        this.taskType = taskType;
        taskOptParams = new HashMap<>();
    }

    public void copy(FileBaseInfo otherFile){
        fileId = otherFile.getFileId();
        fileMd5 = otherFile.getFileMd5();
        fileType = otherFile.getFileType();
        fileName = otherFile.getFileName();
        osId = otherFile.getOsId();
        optId = otherFile.getOptId();
        fileOwner = otherFile.getFileOwner();
        fileUnit = otherFile.getFileUnit();
        libraryId = otherFile.getLibraryId();
    }

}

