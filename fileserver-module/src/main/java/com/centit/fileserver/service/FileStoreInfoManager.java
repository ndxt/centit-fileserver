package com.centit.fileserver.service;

import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;

import java.util.List;

public interface FileStoreInfoManager extends BaseEntityManager<FileStoreInfo, String> {
    boolean saveTempFileInfo(FileInfo fileInfo, String tempFilePath, long size);
    void increaseFileReference(FileStoreInfo fileStoreInfo);
    void decreaseFileReference(String fileMd5);

    List<FileStoreInfo> listTempFile(int limit);

    void markStoreErrorTag(FileStoreInfo fileStoreInfo);
}
