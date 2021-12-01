package com.centit.fileserver.task;

import com.alibaba.fastjson.JSON;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 存储文件
 */
@Service
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
                .user("admin")
                .method("SaveFileOpt").tag(fileMd5).time(DatetimeOpt.currentUtilDate())
                .content("文件存储失败" + fileOptTaskInfo.getFileId() + "没有对应fileInfo")
                .oldObject(fileOptTaskInfo));
            logger.error("文件存储失败，找不到对应的文件信息：" + JSON.toJSONString(fileOptTaskInfo));
            return;
        }
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
        save(tempFilePath, fileInfo, fileSize);
        logger.info("存储文件完成");
        OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
            .user("admin")//.unit(fileOptTaskInfo.)
            .method("SaveFileOpt").tag(fileMd5).time(DatetimeOpt.currentUtilDate())
            .content("存储文件完成:" + tempFilePath));
    }


    @Override
    public FileTaskInfo attachTaskInfo(FileBaseInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if (StringUtils.equalsAnyIgnoreCase(
            StringBaseOpt.castObjectToString(pretreatInfo.containsKey("encryptType")),
            "A", "Z")) {
            return null;
        }
        FileTaskInfo saveFileTaskInfo = new FileTaskInfo(getOpeatorName());
        saveFileTaskInfo.copy(fileInfo);
        saveFileTaskInfo.setFileSize(fileSize);
        return saveFileTaskInfo;
    }
}
