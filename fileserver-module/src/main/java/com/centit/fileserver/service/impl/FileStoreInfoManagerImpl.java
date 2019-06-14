package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileStoreInfoDao;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileStore;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Service("fileStoreInfoManager")
@Transactional
public class FileStoreInfoManagerImpl
        extends BaseEntityManagerImpl<FileStoreInfo, String, FileStoreInfoDao>
     implements FileStoreInfoManager {

    @Value("${spring.datasource.url}")
    private String connUrl;

    @Resource
    private FileStore fileStore;

    @Resource(name ="fileStoreInfoDao")
    @NotNull
    @Override
    protected void setBaseDao(FileStoreInfoDao baseDao) {
        super.baseDao = baseDao;
    }

    @Override
    @Transactional
    public void increaseFileReferenceCount(String fileMd5) {
        if(StringUtils.isBlank(fileMd5)){
            return;
        }

        FileStoreInfo fileStoreInfo = baseDao.getObjectById(fileMd5);
        if(fileStoreInfo == null){
            return;
        }

        fileStoreInfo.setFileReferenceCount(fileStoreInfo.getFileReferenceCount() + 1);
        baseDao.updateObject(fileStoreInfo);
    }

    @Override
    @Transactional
    public void decreaseFileReferenceCount(String fileMd5) {
        if(StringUtils.isBlank(fileMd5)){
            return;
        }

        FileStoreInfo fileStoreInfo = baseDao.getObjectById(fileMd5);
        if(fileStoreInfo == null){
            return;
        }

        Long currentFileReferenceCount = fileStoreInfo.getFileReferenceCount() - 1;
        if (currentFileReferenceCount > 0) {
            fileStoreInfo.setFileReferenceCount(currentFileReferenceCount);
            baseDao.updateObject(fileStoreInfo);
        } else {
            try {
                fileStore.deleteFile(fileStoreInfo.getFileStorePath());
                baseDao.deleteObject(fileStoreInfo);
            } catch (IOException e) {
                logger.error("删除文件失败: " + fileStoreInfo.getFileMd5(), e);
            }
        }
    }
}
