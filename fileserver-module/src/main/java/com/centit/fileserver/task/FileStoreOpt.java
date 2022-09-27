package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class FileStoreOpt {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreOpt.class);

    @Autowired
    protected FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    private String fetchOrSaveFile(String tempFilePath, FileInfo file, long fileSize)
    throws IOException {

        String fileStoreUrl = fileStore.matchFileStoreUrl(file, fileSize);
        if(fileStore.checkFile(fileStoreUrl)){
            return fileStoreUrl;
        }
        return fileStore.saveFile(tempFilePath, file, fileSize);
    }
    /**
     * 存储文件
     * @param tempFilePath 临时文件路劲
     * @param file FileInfo
     * @param fileSize 文件大小
     */
    protected void save(String tempFilePath, FileInfo file, long fileSize) {
        try {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(file.getFileMd5());
            if (fileStoreInfo == null) { // 按道理永远运行不到这儿
                String fileStorePath = fetchOrSaveFile(tempFilePath, file, fileSize);
                fileStoreInfo =
                    new FileStoreInfo(file.getFileMd5(), fileSize,
                        fileStorePath, 1L,false);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                if(fileStoreInfo.isTemp()){
                    fileStoreInfo.setFileStorePath(
                        fetchOrSaveFile(tempFilePath, file, fileSize));
                    fileStoreInfo.setIsTemp(false);
                }
                //fileStoreInfoManager.increaseFileReference(fileStoreInfo);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }
}
