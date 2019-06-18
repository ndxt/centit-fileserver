package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.utils.SystemTempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 存储文件
 */
@Service
public class SaveFileOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(SaveFileOpt.class);

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileMd5 = fileOptTaskInfo.getFileMd5();
        long fileSize = fileOptTaskInfo.getFileSize();
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
        save(tempFilePath, fileMd5, fileSize);
        logger.info("存储文件完成");
    }
}
