package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class FileOpt {

    private static final Logger logger = LoggerFactory.getLogger(FileOpt.class);

    @Autowired
    protected FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    private String fetchOrSaveFile(String tempFilePath, String fileMd5, long fileSize)
    throws IOException {
        if(fileStore.checkFile(fileMd5, fileSize)){
            return fileStore.getFileStoreUrl(fileMd5, fileSize);
        }
        return fileStore.saveFile(tempFilePath, fileMd5, fileSize);
    }
    /**
     * 存储文件
     * @param tempFilePath 临时文件路劲
     * @param fileMd5 md5编码
     * @param fileSize 文件大小
     */
    protected void save(String tempFilePath, String fileMd5, long fileSize) {
        try {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileMd5);
            if (fileStoreInfo == null) { // 按道理永远运行不到这儿
                String fileStorePath = fetchOrSaveFile(tempFilePath, fileMd5, fileSize);
                fileStoreInfo =
                    new FileStoreInfo(fileMd5, fileSize, fileStorePath, 1L,false);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                if(fileStoreInfo.isTemp()){
                    fileStoreInfo.setFileStorePath(
                        fetchOrSaveFile(tempFilePath, fileMd5, fileSize));
                    fileStoreInfo.setIsTemp(false);
                }
                fileStoreInfoManager.increaseFileReference(fileStoreInfo);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }
}
