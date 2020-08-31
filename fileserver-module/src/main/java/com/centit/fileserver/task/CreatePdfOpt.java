package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileSystemOpt;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @SneakyThrows
    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileInfo oldInfo= new FileInfo();
        oldInfo.copyNotNullProperty(fileInfo);
        oldInfo.setFileId(fileId);
        if(null==fileInfo) {
            return;
        }
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        if(!new File(originalTempFilePath).exists()){
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            if(fileStoreInfo!=null) {
                FileSystemOpt.fileCopy(fileStore.getFileStoreUrl(fileStoreInfo.getFileMd5(),fileStoreInfo.getFileSize()), originalTempFilePath);
            }
        }
        try {
            String pdfTempFile = FilePretreatUtils.createPdf(fileInfo, originalTempFilePath);
            if (null != pdfTempFile) {
                save(pdfTempFile, fileInfo.getFileMd5(), new File(pdfTempFile).length());
                oldInfo.setAttachedFileMd5(fileInfo.getFileMd5());
                oldInfo.setAttachedType(fileInfo.getFileType());
                fileInfoManager.updateObject(oldInfo);
                logger.info("生成PDF完成");
            }
        } catch (IOException e) {
            logger.error("生成PDF文件出错！" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
