package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.controller.FileLogController;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 添加pdf副本
 */
@Service
public class CreatePdfOpt extends FileStoreOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(CreatePdfOpt.class);

    @Override
    public String getOpeatorName() {
        return "pdf";
    }

    @Autowired
    private FileInfoManager fileInfoManager;
    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @SneakyThrows
    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(null==fileInfo) {
            return;
        }

        FileInfo pdfFileInfo = new FileInfo();
        pdfFileInfo.copy(fileInfo);

        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        if(!new File(originalTempFilePath).exists()){
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            if(fileStoreInfo!=null) {
                InputStream inputStream = fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
                FileSystemOpt.createFile(inputStream, originalTempFilePath);
            }
        }
        try {
            String pdfTempFile = FilePretreatUtils.createPdf(pdfFileInfo, originalTempFilePath);
            if (null != pdfTempFile) {
                pdfFileInfo.setFileMd5(FileMD5Maker.makeFileMD5(new File(pdfTempFile)));
                super.save(pdfTempFile, pdfFileInfo, new File(pdfTempFile).length());
                fileInfo.setAttachedFileMd5(pdfFileInfo.getFileMd5());
                fileInfo.setAttachedType(pdfFileInfo.getFileType());
                fileInfoManager.updateObject(fileInfo);
                logger.info("生成PDF完成");
                OperationLogCenter.log(OperationLog.create().operation(FileLogController.LOG_OPERATION_NAME)
                    .user("admin").unit(fileInfo.getLibraryId())
                    .method("生成PDF完成").tag(fileId).time(DatetimeOpt.currentUtilDate()).content(fileInfo.getFileName()).newObject(fileInfo));
            }
        } catch (IOException e) {
            logger.error("生成PDF文件出错！" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("pdf"),false)
            && AbstractOfficeToPdf.canTransToPdf(fileInfo.getFileType())){
            FileTaskInfo taskInfo = new FileTaskInfo(getOpeatorName());
            taskInfo.copy(fileInfo);
            taskInfo.setFileSize(fileSize);
            return taskInfo;
        }
        return null;
    }

}
