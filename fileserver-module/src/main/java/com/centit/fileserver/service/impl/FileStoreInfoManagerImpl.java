package com.centit.fileserver.service.impl;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.dao.FileStoreInfoDao;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;

@Service("fileStoreInfoManager")
@Transactional
public class FileStoreInfoManagerImpl
        extends BaseEntityManagerImpl<FileStoreInfo, String, FileStoreInfoDao>
     implements FileStoreInfoManager {

    @Autowired
    private FileStore fileStore;

    @Autowired//(name ="fileStoreInfoDao")
    @NotNull
    @Override
    protected void setBaseDao(FileStoreInfoDao baseDao) {
        super.baseDao = baseDao;
    }

    @Override
    @Transactional
    public boolean saveTempFileInfo(FileInfo fileInfo, String tempFilePath, long size) {
        FileStoreInfo fileStoreInfo = baseDao.getObjectById(fileInfo.getFileMd5());
        if(fileStoreInfo != null){
            return false;
        }
        String fileStoreUrl = fileStore.matchFileStoreUrl(fileInfo, size);
        boolean isExist = fileStore.checkFile(fileStoreUrl);
        if(isExist){
            tempFilePath = fileStoreUrl;
        }
        //存放在临时区  !isExist
        fileStoreInfo = new FileStoreInfo(fileInfo.getFileMd5(), size,
            tempFilePath, 0L, !isExist);
        baseDao.saveNewObject(fileStoreInfo);
        return !isExist;
    }

    @Override
    @Transactional
    public void increaseFileReference(FileStoreInfo fileStoreInfo){
        fileStoreInfo.setFileReferenceCount(fileStoreInfo.getFileReferenceCount() + 1);
        baseDao.updateObject(fileStoreInfo);
    }

    @Override
    @Transactional
    public void decreaseFileReference(String fileMd5) {
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
