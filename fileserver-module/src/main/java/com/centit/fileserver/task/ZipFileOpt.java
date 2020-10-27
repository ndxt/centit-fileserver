package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
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
import java.util.Map;

/**
 * zip加密压缩文件
 */
@Service
public class ZipFileOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(ZipFileOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    /**
     * @return 任务转换器名称
     */
    @Override
    public String getOpeatorName() {
        return "zip";
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
        return null;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String encryptPass = (String) fileOptTaskInfo.getTaskOptParams().get("password");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String encryptedZipFile = FilePretreatUtils.zipFileAndEncrypt(fileInfo, originalTempFilePath, encryptPass);
            if (null != encryptedZipFile) {
                save(encryptedZipFile, fileInfo, new File(encryptedZipFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("zip加密压缩文件完成");
            }
        } catch (Exception e) {
            logger.error("zip加密压缩文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }
}

  /*  @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String zipFilePath = FilePretreatUtils.zipFile(fileInfo, originalTempFilePath);
            if (null != zipFilePath) {
                save(zipFilePath, fileInfo.getFileMd5(), new File(zipFilePath).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("zip压缩文件完成");
            }
        } catch (Exception e) {
            logger.error("zip压缩文件时出错！", e);
        }
    }*/
