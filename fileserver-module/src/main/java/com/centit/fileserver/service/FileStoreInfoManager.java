package com.centit.fileserver.service;

import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;

public interface FileStoreInfoManager extends BaseEntityManager<FileStoreInfo, String> {
    void increaseFileReferenceCount(String fileMd5,String path,long size,Boolean isTemp);
    void decreaseFileReferenceCount(String fileMd5);
}
