package com.centit.fileserver.task;

import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Map;

/**
 * AES加密文件
 */
@Service
@Transactional
public class EncryptFileOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(EncryptFileOpt.class);

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
    public FileTaskInfo attachTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        String password = SecurityOptUtils.decodeSecurityString(StringBaseOpt.castObjectToString(pretreatInfo.get("password")));
        String encryptType = StringBaseOpt.castObjectToString(pretreatInfo.get("encryptType"));
        //AES 加密 //SM4 国密加密
        if( StringUtils.equalsAnyIgnoreCase(encryptType, "A","S","M","G")){
            FileTaskInfo taskInfo = new FileTaskInfo(getOpeatorName());
            taskInfo.copy(fileInfo);
            taskInfo.setFileSize(fileSize);
            taskInfo.putOptParam("password", password);
            taskInfo.putOptParam("encryptType", encryptType);
            return taskInfo;
        }
        return null;
    }
    private void doEncryptFile(FileInfo fileInfo, long fileSize, String encryptType, String encryptPass){
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        String aesEncryptedFile = FilePretreatUtils.encryptFile(fileInfo, originalTempFilePath, encryptType, encryptPass);
        save(aesEncryptedFile, fileInfo, new File(aesEncryptedFile).length());
        fileInfoManager.updateObject(fileInfo);
        FileSystemOpt.deleteFile(originalTempFilePath);
    }
    @Override
    public int runTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        String password = SecurityOptUtils.decodeSecurityString(StringBaseOpt.castObjectToString(pretreatInfo.get("password")));
        String encryptType = StringBaseOpt.castObjectToString(pretreatInfo.get("encryptType"));
        //AES 加密 //SM4 国密加密
        if( StringUtils.equalsAnyIgnoreCase(encryptType, "A","S","M","G")){
            doEncryptFile(fileInfo, fileSize, encryptType, password);
            return 1;
        }
        return 0;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String encryptPass = (String) fileOptTaskInfo.getOptParam("password");
        String encryptType = (String) fileOptTaskInfo.getOptParam("encryptType");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(fileInfo==null) return;
        doEncryptFile(fileInfo, fileSize, encryptType, encryptPass);
    }
}
