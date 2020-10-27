package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.controller.FileLogController;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

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
        String fileMd5 = fileOptTaskInfo.getFileMd5();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileOptTaskInfo.getFileId());
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
        save(tempFilePath, fileInfo, fileSize);
        logger.info("存储文件完成");
        OperationLogCenter.log(OperationLog.create().operation(FileLogController.LOG_OPERATION_NAME)
            .user("admin")//.unit(fileOptTaskInfo.)
            .method("存储文件完成").tag(fileMd5).time(DatetimeOpt.currentUtilDate()).content(tempFilePath));
    }
}
