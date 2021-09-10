package com.centit.fileserver.utils;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileIOUtils {
    public static InputStream getFileStream(FileStore fileStore, FileStoreInfo fileStoreInfo) throws IOException {
        return fileStoreInfo.getIsTemp() ?
            new FileInputStream(new File(fileStoreInfo.getFileStorePath())) :
            fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
    }

    public static boolean reGetPdf(String fileId, HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo,
        FileStore fileStore, CreatePdfOpt createPdfOpt, FileInfoManager fileInfoManager,
                            FileStoreInfoManager fileStoreInfoManager) throws IOException {
        boolean canView = false;
        if (AbstractOfficeToPdf.canTransToPdf(fileInfo.getFileType())) {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            FileTaskInfo addPdfTaskInfo = new FileTaskInfo(createPdfOpt.getOpeatorName());
            addPdfTaskInfo.setFileId(fileId);
            addPdfTaskInfo.setFileSize(fileStoreInfo.getFileSize());
            createPdfOpt.doFileTask(addPdfTaskInfo);
            FileInfo newFileInfo = fileInfoManager.getObjectById(fileId);
            if (StringUtils.isNotBlank(newFileInfo.getAttachedFileMd5())) {
                FileStoreInfo attachedFileStoreInfo =
                    fileStoreInfoManager.getObjectById(newFileInfo.getAttachedFileMd5());
                if (attachedFileStoreInfo != null) {
                    canView = true;
                    UploadDownloadUtils.downFileRange(request, response,
                        FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo),
                        attachedFileStoreInfo.getFileSize(),
                        FileType.truncateFileExtName(newFileInfo.getFileName())
                            + ".pdf",// + newFileInfo.getAttachedType(),
                        "inline", null);
                }
            }
        }
        return canView;
    }
}
