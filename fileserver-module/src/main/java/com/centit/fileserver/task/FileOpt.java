package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

public class FileOpt {

    private static final Logger logger = LoggerFactory.getLogger(FileOpt.class);

    @Resource
    private FileStoreInfoManager fileStoreInfoManager;

    @Resource
    private FileStore fileStore;

    /**
     * 存储文件
     * @param tempFilePath
     * @param fileMd5
     * @param fileSize
     */
    protected void save(String tempFilePath, String fileMd5, long fileSize) {
        try {
            if (fileStoreInfoManager.getObjectById(fileMd5) == null) {
                String fileStorePath = fileStore.saveFile(tempFilePath, fileMd5, fileSize);
                FileStoreInfo fileStoreInfo = new FileStoreInfo(fileMd5, fileSize, fileStorePath, 1L);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                fileStoreInfoManager.increaseFileReferenceCount(fileMd5);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }
}
