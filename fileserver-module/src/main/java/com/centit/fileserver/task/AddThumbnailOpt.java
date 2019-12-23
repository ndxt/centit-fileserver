package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.file.FileMD5Maker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 添加缩略图
 */
@Service
public class AddThumbnailOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(AddThumbnailOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        int width = (int) fileOptTaskInfo.getTaskOptParams().get("width");
        int height = (int) fileOptTaskInfo.getTaskOptParams().get("height");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String thumbnailFile = FilePretreatUtils.addThumbnail(fileInfo, originalTempFilePath, width, height);
            if (null != thumbnailFile) {
                File thumbnail = new File(thumbnailFile);
                save(thumbnailFile, FileMD5Maker.makeFileMD5(thumbnail), thumbnail.length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("生成缩略图完成");
            }
        } catch (IOException e) {
            logger.error("生成缩略图出错！", e);
        }
    }
}
