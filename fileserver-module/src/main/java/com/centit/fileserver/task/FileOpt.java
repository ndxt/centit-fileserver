package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

public abstract class FileOpt {

    private static final Logger logger = LoggerFactory.getLogger(FileOpt.class);

    @Resource
    protected FileStoreInfoManager fileStoreInfoManager;

    @Resource
    protected FileStore fileStore;

    /**
     * 存储文件
     * @param tempFilePath 临时文件路劲
     * @param fileMd5 md5编码
     * @param fileSize 文件大小
     */
    protected void save(String tempFilePath, String fileMd5, long fileSize) {
        try {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileMd5);
            if (fileStoreInfo == null) {
                String fileStorePath = fileStore.saveFile(tempFilePath, fileMd5, fileSize);
                fileStoreInfo =
                    new FileStoreInfo(fileMd5, fileSize, fileStorePath, 1L,false);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                if(fileStoreInfo.isTemp()){
                    fileStoreInfo.setFileStorePath(
                        fileStore.saveFile(tempFilePath, fileMd5, fileSize));
                }
                fileStoreInfoManager.increaseFileReferenceCount(fileMd5,
                    fileStoreInfo.getFileStorePath(),
                    fileSize,false);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }
}
