package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String encryptPass = StringBaseOpt.castObjectToString(
            fileOptTaskInfo.getOptParam("password"));
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String encryptedZipFile = StringUtils.isBlank(encryptPass)?
                FilePretreatUtils.zipFile(fileInfo, originalTempFilePath):
                FilePretreatUtils.zipFileAndEncrypt(fileInfo, originalTempFilePath, encryptPass);
            if (null != encryptedZipFile) {
                fileInfo.setFileMd5(FileMD5Maker.makeFileMD5(new File(encryptedZipFile)));
                fileInfo.setFileName(FileType.truncateFileExtName(
                    fileInfo.getFileName())+".zip");
                fileInfo.setFileType("zip");
                save(encryptedZipFile, fileInfo, new File(encryptedZipFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("zip加密压缩文件完成");
            }
        } catch (Exception e) {
            logger.error("zip加密压缩文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }

    @Override
    public FileTaskInfo attachTaskInfo(FileBaseInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if("Z".equalsIgnoreCase(StringBaseOpt.castObjectToString(pretreatInfo.containsKey("encryptType")))){
            FileTaskInfo zipTaskInfo = new FileTaskInfo(getOpeatorName());
            zipTaskInfo.copy(fileInfo);
            zipTaskInfo.setFileSize(fileSize);
            zipTaskInfo.putOptParam("password", pretreatInfo.get("password"));
            return zipTaskInfo;
        }
        return null;
    }
}
