package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * AES加密文件
 */
@Service
public class EncryptFileWithAesOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(EncryptFileWithAesOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    /**
     * @return 任务转换器名称
     */
    @Override
    public String getOpeatorName() {
        return "encrypt";
    }

    /**
     * 获取文件预处理信息
     *
     * @param fileInfo     文件信息
     * @param fileSize     文件大小
     * @param pretreatInfo 预处理信息
     * @return 文件任务信息 null 表示不匹配不需要处理
     */
    @Override
    public FileTaskInfo attachTaskInfo(FileBaseInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        String password = StringBaseOpt.castObjectToString(pretreatInfo.get("password"));
        if("A".equalsIgnoreCase(StringBaseOpt.castObjectToString(pretreatInfo.containsKey("encryptType")))
            || StringUtils.isNotBlank(password)){
            FileTaskInfo taskInfo = new FileTaskInfo(getOpeatorName());
            taskInfo.copy(fileInfo);
            taskInfo.setFileSize(fileSize);
            taskInfo.putOptParam("password", password);
            return taskInfo;
        }
        return null;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String encryptPass = (String) fileOptTaskInfo.getOptParam("password");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String aesEncryptedFile = FilePretreatUtils.encryptFileWithAes(fileInfo, originalTempFilePath, encryptPass);
            if (null != aesEncryptedFile) {
                save(aesEncryptedFile, fileInfo, new File(aesEncryptedFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("AES加密文件完成");
            }
        } catch (Exception e) {
            logger.error("AES加密文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }
}
