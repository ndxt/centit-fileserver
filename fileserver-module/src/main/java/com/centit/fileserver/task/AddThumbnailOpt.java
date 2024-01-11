package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 添加缩略图
 */
@Service
public class AddThumbnailOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(AddThumbnailOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Override
    public String getOpeatorName() {
        return "thumbnail";
    }

    private void doThumbnail(FileInfo fileInfo, int width, int height) {
        String originalTempFilePath =
            SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileInfo.getFileSize());
        try {
            String thumbnailFile =
                FilePretreatUtils.addThumbnail(fileInfo, originalTempFilePath, width, height);
            if (null != thumbnailFile) {
                FileInfo thumbnailFileInfo = new FileInfo();
                thumbnailFileInfo.copy(fileInfo);
                File thumbnail = new File(thumbnailFile);
                thumbnailFileInfo.setFileMd5(FileMD5Maker.makeFileMD5(thumbnail));
                super.save(thumbnailFile, thumbnailFileInfo, thumbnail.length());
                fileInfo.setAttachedFileMd5(thumbnailFileInfo.getFileMd5());
                fileInfo.setAttachedType(FileType.getFileExtName(thumbnailFile));
                fileInfoManager.updateObject(fileInfo);
                logger.info("生成缩略图完成");
            }
        } catch (IOException e) {
            logger.error("生成缩略图出错！", e);
        }
    }
    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        int width = NumberBaseOpt.castObjectToInteger(fileOptTaskInfo.getOptParam("width"),320);
        int height = NumberBaseOpt.castObjectToInteger(fileOptTaskInfo.getOptParam("height"), 240);
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        fileInfo.setFileSize(fileSize);
        doThumbnail(fileInfo, width, height);
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
        if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("thumbnail"),false)) {
            FileTaskInfo taskInfo = new FileTaskInfo(getOpeatorName());
            taskInfo.copy(fileInfo);
            taskInfo.setFileSize(fileSize);
            taskInfo.putOptParam("width", pretreatInfo.get("width"));
            taskInfo.putOptParam("height", pretreatInfo.get("height"));
            return taskInfo;
        }
        return null;
    }

    @Override
    public int runTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("thumbnail"),false)) {
            int width = NumberBaseOpt.castObjectToInteger(pretreatInfo.get("width"),320);
            int height = NumberBaseOpt.castObjectToInteger(pretreatInfo.get("height"), 240);
            fileInfo.setFileSize(fileSize);
            doThumbnail(fileInfo, width, height);
            return 1;
        }
        return 0;
    }

}
