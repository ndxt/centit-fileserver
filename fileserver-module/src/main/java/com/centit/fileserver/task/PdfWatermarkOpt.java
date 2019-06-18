package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * pdf添加水印
 */
@Service
public class PdfWatermarkOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(PdfWatermarkOpt.class);

    @Resource
    private FileInfoManager fileInfoManager;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        String waterMarkStr = (String) fileOptTaskInfo.getTaskOptParams().get("watermark");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String waterMarkPdfTempFile = FilePretreatUtils.addWatermarkForPdf(fileInfo, originalTempFilePath, waterMarkStr);
            if (null != waterMarkPdfTempFile) {
                save(waterMarkPdfTempFile, fileInfo.getFileMd5(), new File(waterMarkPdfTempFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("添加水印完成");
            }
        } catch (IOException e) {
            logger.error("添加水印出错！", e);
        }
    }
}
