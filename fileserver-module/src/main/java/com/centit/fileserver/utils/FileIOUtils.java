package com.centit.fileserver.utils;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;

public abstract class FileIOUtils {
    private static final int URI_START_PARAM = 5;
    public static final String LOG_OPERATION_NAME = "FileServerLog";
    public static InputStream getFileStream(FileStore fileStore, FileStoreInfo fileStoreInfo) throws IOException {
        if("E".equals(fileStoreInfo.getIsTemp())) return null;
        return fileStoreInfo.isTemp() ? new FileInputStream(new File(fileStoreInfo.getFileStorePath())) :
            fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
    }

    public static boolean hasSensitiveExtName(String fileName){
        return StringUtils.endsWithAny(fileName.trim().toLowerCase(),
            ".js", ".jsp",".jspx", ".asp", ".php", ".exe", ".html", ".htm",
            ".py", ".py3", ".sh", ".vbs", ".wsh");
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
                    try(InputStream is = FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo)) {
                        UploadDownloadUtils.downFileRange(request, response, is,
                            attachedFileStoreInfo.getFileSize(),
                            FileType.truncateFileExtName(newFileInfo.getFileName())
                                + ".pdf",// + newFileInfo.getAttachedType(),
                            "inline", null);
                    }
                }
            }
        }
        return canView;
    }

    public static InputStream createPdfStream(String fileId, FileInfo fileInfo,
                                                 FileStoreInfo fileStoreInfo ,
                                   FileStore fileStore, CreatePdfOpt createPdfOpt,
                                   FileStoreInfoManager fileStoreInfoManager) throws IOException {

        FileTaskInfo addPdfTaskInfo = new FileTaskInfo(createPdfOpt.getOpeatorName());
        addPdfTaskInfo.setFileId(fileId);
        addPdfTaskInfo.setFileSize(fileStoreInfo.getFileSize());
        createPdfOpt.doPdfOpt(fileInfo, fileStoreInfo.getFileSize());
        if (StringUtils.isNotBlank(fileInfo.getAttachedFileMd5())) {
            FileStoreInfo attachedFileStoreInfo =
                fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
            return  FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo);
        }
        return null;
    }
    public static ImmutableTriple<String, String, String> fetchUnitFilePath(String uri)
        throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 2) {
            return null;
        }
        if (n == URI_START_PARAM + 2) {
            return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
                "", URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        }
        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        for (int i = URI_START_PARAM + 2; i < n - 1; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
            sb.toString(), URLDecoder.decode(urips[n - 1], "UTF-8"));
    }
}
