package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 添加pdf副本
 */
@Service
public class CreatePdfOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(CreatePdfOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(null==fileInfo) {
            return;
        }
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String pdfTempFile = FilePretreatUtils.createPdf(fileInfo, originalTempFilePath);
            if (null != pdfTempFile) {
                save(pdfTempFile, fileInfo.getFileMd5(), new File(pdfTempFile).length());
                fileInfoManager.updateObject(fileInfo);
                logger.info("生成PDF完成");
            }
        } catch (IOException e) {
            logger.error("生成PDF文件出错！" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
