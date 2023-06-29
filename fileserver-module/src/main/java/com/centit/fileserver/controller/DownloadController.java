package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.ZipCompressor;
import com.centit.support.file.FileEncryptWithAes;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.security.Md5Encoder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipOutputStream;

/**
 * @author zhf
 */
@Controller
@RequestMapping("/download")
@Api(value = "文件下载", tags = "文件下载")
public class DownloadController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);
    @Autowired
    private FileInfoManager fileInfoManager;
    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;
    @Autowired
    private FileLibraryInfoManager fileLibraryInfoManager;
    @Autowired
    private FileAccessLogManager fileAccessLogManager;
    @Autowired
    protected FileStore fileStore;
    @Autowired
    protected CreatePdfOpt createPdfOpt;

    @RequestMapping(value = "/downloadwithauth/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限下载文件，可以传入authCode分享码")
    public void downloadWithAuthByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
        fileInfoManager.writeDownloadFileLog(fileInfo, WebOptUtils.getCurrentUserCode(request));
    }

    @RequestMapping(value = "/preview/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限预览文件，可以传入authCode分享码")
    public void previewFile(@PathVariable("fileId") String fileId, HttpServletRequest request,
                            HttpServletResponse response) throws IOException{
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String closeAuth = request.getParameter("closeAuth");
        if (StringUtils.isBlank(closeAuth)&&noAuth(request, response, fileInfo)) {
            return;
        }

        boolean canView = false;
        try {
            if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(),
                "txt", "html", "csv", "pdf", "xml")) {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());

                if(fileStoreInfo.getFileSize() < 1){
                    UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]) , fileInfo.getFileName(), response);
                    return ;
                }

                String charset = null;
                if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(),
                    "txt", "csv")) {
                    charset = new AutoDetectReader(FileIOUtils.getFileStream(fileStore, fileStoreInfo)).getCharset().name();
                }
                UploadDownloadUtils.downFileRange(request, response,
                    FileIOUtils.getFileStream(fileStore, fileStoreInfo),
                    fileStoreInfo.getFileSize(), fileInfo.getFileName(), "inline", charset);
                canView = true;
            } else if (StringUtils.isNotBlank(fileInfo.getAttachedFileMd5())) {
                FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());

                if (attachedFileStoreInfo != null && attachedFileStoreInfo.getFileSize() > 0) {
                    UploadDownloadUtils.downFileRange(request, response,
                        FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo),
                        attachedFileStoreInfo.getFileSize(),
                        FileType.truncateFileExtName(fileInfo.getFileName())
                            + "." + fileInfo.getAttachedType(),
                        "inline", null);
                    canView = true;
                } else {
                    canView = FileIOUtils.reGetPdf(fileId, request, response, fileInfo,
                        fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
                }
            } else {
                canView = FileIOUtils.reGetPdf(fileId, request, response, fileInfo,
                    fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
            }
            if (!canView) {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());

                if(fileStoreInfo.getFileSize() < 1){
                    UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]) , fileInfo.getFileName(), response);
                    return ;
                }
                UploadDownloadUtils.downFileRange(request, response,
                    FileIOUtils.getFileStream(fileStore, fileStoreInfo),
                    fileStoreInfo.getFileSize(),
                    fileInfo.getFileName(),
                    "inline", null);
            }
            fileInfoManager.writeDownloadFileLog(fileInfo, WebOptUtils.getCurrentUserCode(request));
        } catch (Exception e) {
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }
    /**
     * 根据文件的id下载附属文件
     * 这个需要权限 控制 用于内部服务之间文件传输
     *
     * @param fileId   文件ID
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException 异常
     */
    @RequestMapping(value = "/pattach/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的id下载附属文件")
    public void downloadAttach(@PathVariable("fileId") String fileId, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);

        if (null != fileInfo) {
            String at = fileInfo.getAttachedType();
            if (StringUtils.isBlank(at) || "N".equals(at)) {
                JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST, "该文件没有附属文件", response);
                return;
            }
            String fileName = FileType.truncateFileExtName(fileInfo.getFileName()) + "." + at;

            FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
            UploadDownloadUtils.downFileRange(request, response,
                fileStore.loadFileStream(attachedFileStoreInfo.getFileStorePath()),
                fileStore.getFileSize(attachedFileStoreInfo.getFileStorePath()), fileName, request.getParameter("downloadType"), null);
        } else {
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                "找不到该文件", response);
        }
    }
    // 文件目录 = 配置目录 + file.getFileStorePath()

    /**
     * 根据文件的id下载文件
     * 这个需要权限 控制 用于内部服务之间文件传输
     *
     * @param fileId   文件ID
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value = "/pfile/{fileId:.*}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的id下载文件")
    public void downloadByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String fileName = request.getParameter("fileName");
        if(!StringBaseOpt.isNvl(fileName)){
            fileInfo.setFileName(fileName);
        }
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());

        downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
        fileInfoManager.writeDownloadFileLog(fileInfo, WebOptUtils.getCurrentUserCode(request));
    }
    /**
     * 批量下载文件
     *
     * @param fileIds  批量下载文件列表
     * @param fileName 文件名
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException 异常
     */
    @RequestMapping(value = "/batchdownload", method = RequestMethod.GET)
    @ApiOperation(value = "批量下载文件，返回压缩文件")
    public void batchDownloadFile(String[] fileIds,
                                  String fileName,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        if (fileIds == null || fileIds.length == 0) {
            JsonResultUtils.writeMessageJson("请提供文件id列表", response);
            return;
        }

        InputStream inputStream = null;
        long fileSize;
        // 如果只下载一个文件则 不压缩
        if (fileIds.length == 1) {
            FileInfo fileInfo = fileInfoManager.getObjectById(fileIds[0]);
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            fileSize = fileStoreInfo.getFileSize();
            String filePath = fileStore.matchFileStoreUrl(fileInfo, fileSize);
            inputStream = fileStore.loadFileStream(filePath);
        } else {
            StringBuilder fileIdSb = new StringBuilder();
            Arrays.sort(fileIds, String::compareTo);
            for (String fid : fileIds) {
                fileIdSb.append(fid);
            }
            // 用所有的fileid（排序）的md5 作为文件名保存在临时目录中
            // 如果临时目录中已经有对应的文件直接下载，如果没有 打包下载
            String fileId = Md5Encoder.encode(fileIdSb.toString());
            String tempFilePath = SystemTempFileUtils.getTempFilePath(fileId, 1024);
            File file = new File(tempFilePath);
            if (!file.exists()) {

                int len = fileIds.length;
                String[] fileUrls = new String[len];
                String[] fileNames = new String[len];
                int j = 0;
                for (int i = 0; i < len; i++) {
                    FileInfo si = fileInfoManager.getObjectById(fileIds[i]);
                    FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(si.getFileMd5());
                    if (si != null) {
                        fileUrls[j] = fileStore.matchFileStoreUrl(si, fileStoreInfo.getFileSize());
                        fileNames[j] = si.getFileName();
                        j++;
                    }
                }
                if (j == 0) {
                    JsonResultUtils.writeMessageJson("请提供文件id列表", response);
                    return;
                }

                compressFiles(tempFilePath, fileUrls, fileNames, j);
            }
            file = new File(tempFilePath);
            fileSize = file.length();
            inputStream = new FileInputStream(file);
        }

        UploadDownloadUtils.downFileRange(request, response,
            inputStream,
            fileSize, fileName, request.getParameter("downloadType"), null);
    }

    private static void downloadFile(FileStore fileStore, FileInfo fileInfo, FileStoreInfo fileStoreInfo,
                                     HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (null != fileInfo) {

            if(fileStoreInfo.getFileSize() < 1){
                UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]) , fileInfo.getFileName(), response);
                return ;
            }

            //对加密的进行特殊处理，ZIP加密的无需处理
            String password = request.getParameter("password");
            if ("A".equals(fileInfo.getEncryptType()) && StringUtils.isNotBlank(password)) {
                String tmpFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileStoreInfo.getFileSize());
                File tmpFile = new File(tmpFilePath);
                if (!fileStoreInfo.isTemp()) { //fileStore.checkFile(fileStoreInfo.getFileMd5(), fileStoreInfo.getFileSize()) ){// !fileStoreInfo.isTemp()){
                    try (InputStream downFile = FileIOUtils.getFileStream(fileStore, fileStoreInfo);
                         OutputStream diminationFile = new FileOutputStream(tmpFile)) {
                        FileEncryptWithAes.decrypt(downFile, diminationFile, password);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        JsonResultUtils.writeHttpErrorMessage(
                            FileServerConstant.ERROR_FILE_ENCRYPT,
                            "解码文件失败：" + e.getMessage(),
                            response);
                        return;
                    }
                }
                try (InputStream inputStream = new FileInputStream(tmpFile)) {
                    UploadDownloadUtils.downFileRange(request, response,
                        inputStream, tmpFile.length(), fileInfo.getFileName(), request.getParameter("downloadType"), null);
                }

                FileSystemOpt.deleteFile(tmpFile);
            } else {
                try {
                    UploadDownloadUtils.downFileRange(request, response,
                        FileIOUtils.getFileStream(fileStore, fileStoreInfo),
                        fileStoreInfo.getFileSize(), fileInfo.getFileName(), request.getParameter("downloadType"), null);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
                }
            }
        } else {
            JsonResultUtils.writeHttpErrorMessage(
                FileServerConstant.ERROR_FILE_NOT_EXIST, "找不到该文件", response);
        }
    }

    private boolean noAuth(HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        if(StringBaseOpt.isNvl(userCode)){
            userCode = request.getParameter("userCode");
        }

        if (!fileLibraryInfoManager.checkAuth(fileInfo, userCode, request.getParameter("authCode"))) {
            JsonResultUtils.writeErrorMessageJson("用户:" + WebOptUtils.getCurrentUserCode(request)
                + ",所属机构:" + WebOptUtils.getCurrentUnitCode(request) + "没有权限;或者验证码" + request.getParameter("authCode") + "不正确", response);
            return true;
        }
        return false;
    }
    private void compressFiles(String zipFilePathName, String[] fileUrls, String[] fileNames, int len) {
        try {
            File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);

            ZipOutputStream out = ZipCompressor.convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";

            for (int i = 0; i < len; i++) {
                try (InputStream fis = fileStore.loadFileStream(fileUrls[i])) {
                    ZipCompressor.compressFile(fis, fileNames[i], out, basedir);
                } catch (Exception e) {
                    logger.info("获取文件" + fileUrls[i] + "出错！");
                }
            }
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
