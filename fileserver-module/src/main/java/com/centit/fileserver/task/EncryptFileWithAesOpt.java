package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.file.FileSystemOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;

/**
 * AES加密文件
 */
@Service
public class EncryptFileWithAesOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(EncryptFileWithAesOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String encryptPass = (String) fileOptTaskInfo.getTaskOptParams().get("password");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String aesEncryptedFile = FilePretreatUtils.encryptFileWithAes(fileInfo, originalTempFilePath, encryptPass);
            if (null != aesEncryptedFile) {
                save(aesEncryptedFile, fileInfo.getFileMd5(), new File(aesEncryptedFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("AES加密文件完成");
            }
        } catch (Exception e) {
            logger.error("AES加密文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }
}
