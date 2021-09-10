package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.po.*;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
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
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.ZipCompressor;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileEncryptWithAes;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.security.Md5Encoder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Set;
import java.util.zip.ZipOutputStream;

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

    @RequestMapping(value = "/testBigFile", method = RequestMethod.GET)
    @ApiOperation(value = "测试大文件下载")
    public void downloadTempFile(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        File bigFile = new File("/D/temp/bigFile.mkv");
        UploadDownloadUtils.downFileRange(request, response,
            new FileInputStream(bigFile),
            bigFile.length(), "bigFile.mkv", request.getParameter("downloadType"), null);
    }

    public static void downloadFile(FileStore fileStore, FileInfo fileInfo, FileStoreInfo fileStoreInfo,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (null != fileInfo) {

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
                    //InputStream inputStream = fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
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

    @RequestMapping(value = "/downloadTemp/{tempfileId}", method = RequestMethod.GET)
    @ApiOperation(value = "下载临时文件")
    public void downloadTempFile(@PathVariable("tempfileId") String tempfileId, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        File zipFile = new File(SystemTempFileUtils.getTempFilePath(tempfileId));
        if (!zipFile.exists()) {
            throw new ObjectException("临时文件不存在：" + tempfileId);
        }
        UploadDownloadUtils.downFileRange(request, response,
            new FileInputStream(zipFile), zipFile.length(),
            WebOptUtils
                .getRequestFirstOneParameter(request, "name", "fileName"),
            request.getParameter("downloadType"), null);
    }


    @RequestMapping(value = "/downloadwithauth/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限下载文件，可以传入authCode分享码")
    public void downloadWithauthByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (noAuth(request, response, fileInfo)) {
            return;
        }
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());

        downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
        fileInfoManager.writeDownloadFileLog(fileInfo, WebOptUtils.getCurrentUserCode(request));
    }

    @ApiOperation(value = "store文件预览")
    @ApiImplicitParam(
        name = "md5SizeExt", value = "文件的Md5码_文件的大小.文件格式 MD5_SIZE.EXT，",
        required = true, paramType = "path", dataType = "String"
    )
    @RequestMapping(value = "/previewstore", method = RequestMethod.GET)
    public void previewStoreFile(String md5SizeExt,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(
            md5SizeExt);
        InputStream inputStream = null;
        long size=fileInfo.getFileSize();
        String fileName=md5SizeExt;
        if (request.getParameter("downloadType").equals("inline") && AbstractOfficeToPdf.canTransToPdf(FileType.getFileExtName(md5SizeExt))) {
            String pdfTmpFile = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.pdf";
            String filePath = fileStore.getFile(fileStore.matchFileStoreUrl(fileInfo, fileInfo.getFileSize())).getPath();
            if(AbstractOfficeToPdf.office2Pdf(FileType.getFileExtName(md5SizeExt),filePath,pdfTmpFile)){
                File pdfFile=new File(pdfTmpFile);
                size=pdfFile.length();
                inputStream=new FileInputStream(pdfFile);
                fileName=fileInfo.getFileMd5()+".pdf";
            }
        }
        if(inputStream==null) {
            inputStream = fileStore.loadFileStream(
                fileStore.matchFileStoreUrl(fileInfo, fileInfo.getFileSize()));
        }
        UploadDownloadUtils.downFileRange(request, response,
            inputStream, size, fileName, request.getParameter("downloadType"), null);
    }

    @RequestMapping(value = "/preview/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限预览文件，可以传入authCode分享码")
    public void previewFile(@PathVariable("fileId") String fileId, HttpServletRequest request,
                            HttpServletResponse response) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (noAuth(request, response, fileInfo)) {
            return;
        }
        boolean canView = false;
        try {
            if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(),
                "txt", "html", "csv", "pdf", "xml")) {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
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

    private boolean noAuth(HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        userCode = StringBaseOpt.isNvl(userCode) ? request.getParameter("userCode") : userCode;
        if (!fileLibraryInfoManager.checkAuth(fileInfo, userCode, request.getParameter("authCode"))) {
            JsonResultUtils.writeErrorMessageJson("用户:" + WebOptUtils.getCurrentUserCode(request)
                + ",所属机构:" + WebOptUtils.getCurrentUnitCode(request) + "没有权限;或者验证码" + request.getParameter("authCode") + "不正确", response);
            return true;
        }
        return false;
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
    @RequestMapping(value = "/pfile/{fileId}", method = RequestMethod.GET)
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
     * 根据文件的 access_token 下载文件
     *
     * @param token    token
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value = "/file/{token}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件token下载文件")
    public void downloadByAccessToken(
        @PathVariable("token") String token, HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        // 根据访问日志的id和授权的token查看是否已经被授权
        FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
        if (fileAccessLog != null) {
            if (fileAccessLog.checkValid(false)) {
                FileInfo fileInfo = fileInfoManager.getObjectById(fileAccessLog.getFileId());
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
                downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
                // 记录访问日志
                fileAccessLog.chargeAccessTimes();
                fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
                fileAccessLog.setLastAccessHost(request.getLocalAddr());
                fileAccessLogManager.updateObject(fileAccessLog);
            } else {
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
                    "没有权限访问该文件或者访问授权已过期！", response);
            }
        } else {
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                "找不到该文件或者您没有权限访问该文件！", response);
        }
    }

    /**
     * 根据access_token下载附属文件
     *
     * @param token    token
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value = "/attach/{token}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的token下载附属文件")
    public void downloadAttachByAccessToken(
        @PathVariable("token") String token, HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        // 根据访问日志的id和授权的token查看是否已经被授权
        FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
        // 判断权限
        if (fileAccessLog != null) {
            if (fileAccessLog.checkValid(true)) {
                downloadAttach(fileAccessLog.getFileId(), request, response);
                // 记录访问日志
                fileAccessLog.chargeAccessTimes();
                fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
                fileAccessLog.setLastAccessHost(request.getLocalAddr());
                fileAccessLogManager.updateObject(fileAccessLog);
            } else {
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
                    "没有权限访问该文件或者访问授权已过期！", response);
            }
        } else {
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                "找不到该文件或者您没有权限访问该文件！", response);
        }
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
    @ApiOperation(value = "批量下载文件")
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
}
