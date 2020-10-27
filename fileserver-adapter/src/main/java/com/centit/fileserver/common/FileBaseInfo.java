package com.centit.fileserver.common;

public interface FileBaseInfo {
    String getFileId();
    String getFileMd5();
    String getFileName();
    String getFileType();
    String getOsId();
    String getOptId();
    String getFileOwner();
    String getFileUnit();
    String getLibraryId();
    //long getFileSize();
}
