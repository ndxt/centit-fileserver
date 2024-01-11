package com.centit.fileserver.task;

import com.alibaba.fastjson2.JSON;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 存储文件
 */
@Service
@Transactional
public class SaveFileOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(SaveFileOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    /**
     * @return 任务转换器名称
     */
    @Override
    public String getOpeatorName() {
        return "save";
    }


    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileMd5 = fileOptTaskInfo.getFileMd5();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileOptTaskInfo.getFileId());
        if (null == fileInfo) {
            OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
                .user("admin").level(OperationLog.LEVEL_ERROR)
                .topUnit("system")
                .method("SaveFileOpt").tag(fileMd5)
                .content("文件存储失败" + fileOptTaskInfo.getFileId() + "没有对应fileInfo")
                .oldObject(fileOptTaskInfo));
            logger.error("文件存储失败，找不到对应的文件信息：" + JSON.toJSONString(fileOptTaskInfo));
            return;
        }
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
        save(tempFilePath, fileInfo, fileSize);
        logger.info("存储文件完成");
    }


    @Override
    public FileTaskInfo attachTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if (StringUtils.equalsAnyIgnoreCase(
            StringBaseOpt.castObjectToString(pretreatInfo.get("encryptType")),
            "A","S","M","G","Z")) {
            return null;
        }
        FileTaskInfo saveFileTaskInfo = new FileTaskInfo(getOpeatorName());
        saveFileTaskInfo.copy(fileInfo);
        saveFileTaskInfo.setFileSize(fileSize);
        return saveFileTaskInfo;
    }

    @Override
    public int runTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if (StringUtils.equalsAnyIgnoreCase(
            StringBaseOpt.castObjectToString(pretreatInfo.get("encryptType")),
            "A","S","M","G","Z")) {
            return 0;
        }
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        save(tempFilePath, fileInfo, fileSize);
        return 1;
    }
}
