package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * pdf添加水印
 */
@Service
public class PdfWatermarkOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(PdfWatermarkOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    /**
     * @return 任务转换器名称
     */
    @Override
    public String getOpeatorName() {
        return "watermark";
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
        if (StringUtils.isNotBlank(
            StringBaseOpt.castObjectToString(pretreatInfo.get("watermark")))){
            FileTaskInfo taskInfo = new FileTaskInfo(getOpeatorName());
            taskInfo.copy(fileInfo);
            taskInfo.setFileSize(fileSize);
            taskInfo.putOptParam("watermark",
                StringBaseOpt.castObjectToString(pretreatInfo.get("watermark")));
            return taskInfo;
        }
        return null;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String waterMarkStr = (String) fileOptTaskInfo.getOptParam("watermark");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String waterMarkPdfTempFile = FilePretreatUtils.addWatermarkForPdf(fileInfo, originalTempFilePath, waterMarkStr);
            if (null != waterMarkPdfTempFile) {
                save(waterMarkPdfTempFile, fileInfo, new File(waterMarkPdfTempFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("添加水印完成");
            }
        } catch (IOException e) {
            logger.error("添加水印出错！", e);
        }
    }
}
