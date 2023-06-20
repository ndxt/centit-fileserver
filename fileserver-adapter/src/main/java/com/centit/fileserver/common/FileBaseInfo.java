package com.centit.fileserver.common;

/**
 * @author codefan@sina.com
 */
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
    long getFileSize();

    String getFileShowPath();
    String getFileState();
    String getFileDesc();
    String getIndexState();
    String getOptMethod();
    String getOptTag();
    String getAttachedFileMd5();
    String getAttachedType();
    String getAuthCode();
    String getParentFolder();
    String getFileCatalog();

}
