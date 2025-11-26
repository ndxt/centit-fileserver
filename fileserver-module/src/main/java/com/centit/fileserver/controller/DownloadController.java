package com.centit.fileserver.controller;

import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.AddThumbnailOpt;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseData;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.*;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.image.ImageOpt;
import com.centit.support.office.Watermark4Pdf;
import com.centit.support.security.FileEncryptUtils;
import com.centit.support.security.Md5Encoder;
import com.centit.support.security.SecurityOptUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    protected FileStore fileStore;

    @Autowired
    protected CreatePdfOpt createPdfOpt;
    @Autowired
    protected AddThumbnailOpt addThumbnailOpt;

    @RequestMapping(value = "/downloadwithauth/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限下载文件，可以传入authCode分享码")
    public void downloadWithAuthByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
        fileInfoManager.writeDownloadFileLog(fileInfo, request);
    }

    @RequestMapping(value = "/preview/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据权限预览文件，可以传入authCode分享码")
    public void previewFile(@PathVariable("fileId") String fileId, HttpServletRequest request,
                            HttpServletResponse response) {
        // 设置缓存控制头
        response.setHeader("Cache-Control", "public, max-age=604800");

        // 校验 fileId 合法性
        if (fileId == null || fileId.trim().isEmpty()) {
            JsonResultUtils.writeErrorMessageJson("无效的文件ID", response);
            return;
        }

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo == null) {
            JsonResultUtils.writeErrorMessageJson("文件不存在", response);
            return;
        }

        String closeAuth = request.getParameter("closeAuth");
        if (noAuth(request, response, fileInfo, closeAuth)) {
            return;
        }

        try {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            if (fileStoreInfo == null) {
                JsonResultUtils.writeErrorMessageJson("文件存储信息不存在", response);
                return;
            }

            File decryptFile=handleEncryptedFileIfNeeded(request, response, fileInfo, fileStoreInfo);

            long fileSize = fileStoreInfo.getFileSize();
            if (fileSize == 0) {
                UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]), fileInfo.getFileName(), response);
                return;
            }

            boolean canView = false;

            if (isTextOrPdfWithoutAttachment(fileInfo)) {
                serveTextOrPdfInline(request, response, fileStoreInfo, fileInfo);
                canView = true;
            } else if (hasValidAttachedFile(fileInfo)) {
                canView = serveAttachedFile(request, response, fileInfo);
            } else {
                canView = FileIOUtils.reGetPdf(request, response, fileInfo,
                    fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
            }

            if (!canView) {
                serveFallbackFile(request, response, fileStoreInfo, fileInfo);
            }
            if(decryptFile != null){
                FileSystemOpt.deleteFile(decryptFile);
            }
        } catch (Exception e) {
            logger.error("文件预览失败，fileId: {}", fileId, e);
            JsonResultUtils.writeErrorMessageJson("文件预览失败：" + e.getMessage(), response);
        }
    }

    private File handleEncryptedFileIfNeeded(HttpServletRequest request, HttpServletResponse response,
                                             FileInfo fileInfo, FileStoreInfo fileStoreInfo) throws Exception {
        if (StringUtils.equalsAnyIgnoreCase(fileInfo.getEncryptType(), "A", "S", "M", "G")) {
            String password = SecurityOptUtils.decodeSecurityString(request.getParameter("password"));
            if (password == null || password.isEmpty()) {
                JsonResultUtils.writeErrorMessageJson("缺少解密密码", response);
                return null;
            }

            String tmpFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileStoreInfo.getFileSize());
            File decryptFile = new File(tmpFilePath);

            if (!fileStoreInfo.isTemp()) {
                try (InputStream downFile = FileIOUtils.getFileStream(fileStore, fileStoreInfo);
                     OutputStream outFile = Files.newOutputStream(decryptFile.toPath())) {
                    if (downFile != null) {
                        FileEncryptUtils.decrypt(downFile, outFile, FileInfo.mapEncryptType(fileInfo.getEncryptType()), password);
                    }
                    fileStoreInfo.setIsTemp("T");
                    fileStoreInfo.setFileStorePath(decryptFile.getAbsolutePath());
                    return decryptFile;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    JsonResultUtils.writeErrorMessageJson(
                        FileServerConstant.ERROR_FILE_ENCRYPT,
                        getI18nMessage("error.423.cannt_encrypt_file", request, e.getMessage()),
                        response);
                    throw e;
                }
            }
        }
        return null;
    }

    private boolean isTextOrPdfWithoutAttachment(FileInfo fileInfo) {
        return StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(), "txt", "csv", "pdf", "xml")
            && StringUtils.isBlank(fileInfo.getAttachedFileMd5());
    }

    private void serveTextOrPdfInline(HttpServletRequest request, HttpServletResponse response,
                                      FileStoreInfo fileStoreInfo, FileInfo fileInfo) throws Exception {
        String charset = null;
        if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(), "txt", "csv")) {
            try (InputStream is = FileIOUtils.getFileStream(fileStore, fileStoreInfo)) {
                if (is != null)
                    charset = new AutoDetectReader(is).getCharset().name();
            }
        }
        try (InputStream is = FileIOUtils.getFileStream(fileStore, fileStoreInfo)) {
            UploadDownloadUtils.downFileRange(request, response, is, fileStoreInfo.getFileSize(),
                fileInfo.getFileName(), "inline", charset);
        }
    }

    private boolean hasValidAttachedFile(FileInfo fileInfo) {
        return StringUtils.isNotBlank(fileInfo.getAttachedFileMd5());
    }

    private boolean serveAttachedFile(HttpServletRequest request, HttpServletResponse response,
                                      FileInfo fileInfo) throws Exception {
        FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
        if (attachedFileStoreInfo != null && attachedFileStoreInfo.getFileSize() > 0) {
            try (InputStream is = FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo)) {
                String fileName = FileType.truncateFileExtName(fileInfo.getFileName()) + "." + fileInfo.getAttachedType();
                UploadDownloadUtils.downFileRange(request, response, is, attachedFileStoreInfo.getFileSize(),
                    fileName, "inline", null);
            }
            return true;
        } else {
            return FileIOUtils.reGetPdf(request, response, fileInfo,
                fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
        }
    }

    private void serveFallbackFile(HttpServletRequest request, HttpServletResponse response,
                                   FileStoreInfo fileStoreInfo, FileInfo fileInfo) throws Exception {
        if (fileStoreInfo.getFileSize() == 0) {
            UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]), fileInfo.getFileName(), response);
            return;
        }
        try (InputStream is = FileIOUtils.getFileStream(fileStore, fileStoreInfo)) {
            UploadDownloadUtils.downFileRange(request, response, is, fileStoreInfo.getFileSize(),
                fileInfo.getFileName(), "inline", null);
        }
    }


    @RequestMapping(value = "/viewPdf", method = RequestMethod.POST)
    @ApiOperation(value = "添加水印并浏览")
    public void previewPdfWithWaterMark(@RequestBody String jsonStr, HttpServletRequest request,
                                        HttpServletResponse response) {
        // 设置缓存控制头，例如 "Cache-Control: public, max-age=604800" //一周 604800 一个与 3794400
        response.setHeader("Cache-Control", "public, max-age=604800");
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        String fileId = jsonObj.getString("fileId");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String closeAuth = request.getParameter("closeAuth");
        if (noAuth(request, response, fileInfo, closeAuth)) {
            return;
        }
        try {
            if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(), "pdf", "docx", "xlsx")) {
                InputStream pdfFileStream = null;
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());

                if (fileStoreInfo.getFileSize() == 0) {
                    UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]), fileInfo.getFileName(), response);
                    return;
                }
                if ("pdf".equals(fileInfo.getFileType())) {
                    pdfFileStream = FileIOUtils.getFileStream(fileStore, fileStoreInfo);
                } else {
                    if (StringUtils.isNotBlank(fileInfo.getAttachedFileMd5())) {
                        FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
                        pdfFileStream = FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo);
                    } else {
                        pdfFileStream = FileIOUtils.createPdfStream(fileInfo, fileStoreInfo, fileStore, createPdfOpt, fileStoreInfoManager);
                    }
                }
                if (pdfFileStream != null) {
                    String waterMarkStr = StringBaseOpt.castObjectToString(jsonObj.get("watermark"), "");
                    float opacity = NumberBaseOpt.castObjectToFloat(jsonObj.get("opacity"), 0.4f);
                    float rotation = NumberBaseOpt.castObjectToFloat(jsonObj.get("rotation"), 45f);
                    float frontSize = NumberBaseOpt.castObjectToFloat(jsonObj.get("frontSize"), 60f);
                    boolean isRepeat = BooleanBaseOpt.castObjectToBoolean(jsonObj.get("isRepeat"), false);
                    String fileName = FileType.truncateFileExtName(fileInfo.getFileName()) + ".pdf";
                    response.setContentType(FileType.getFileMimeType(fileName));
                    response.setHeader("Content-Disposition", "inline; filename="
                        + URLEncoder.encode(fileName, "UTF-8"));
                    Watermark4Pdf.addWatermark4Pdf(pdfFileStream, response.getOutputStream(), waterMarkStr, opacity, rotation, frontSize, isRepeat);
                    pdfFileStream.close();
                    return;
                }
            }
            JsonResultUtils.writeErrorMessageJson(ResponseData.ERROR_NOT_FOUND,
                getI18nMessage("error.404.file_cannot_be_converted_to_pdf", request),
                response);
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
        // 设置缓存控制头，例如 "Cache-Control: public, max-age=604800" //一周 604800 一个与 3794400
        response.setHeader("Cache-Control", "public, max-age=604800");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (null != fileInfo) {
            String at = fileInfo.getAttachedType();
            if (StringUtils.isBlank(at) || "N".equals(at)) {
                JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST,
                    getI18nMessage("error.404.file_not_found", request, fileId + ": attached"), response);
                return;
            }
            String fileName = FileType.truncateFileExtName(fileInfo.getFileName()) + "." + at;

            FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
            UploadDownloadUtils.downFileRange(request, response,
                fileStore.loadFileStream(attachedFileStoreInfo.getFileStorePath()),
                fileStore.getFileSize(attachedFileStoreInfo.getFileStorePath()), fileName, request.getParameter("downloadType"), null);
        } else {
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                getI18nMessage("error.404.file_not_found", request, fileId), response);
        }
    }

    @RequestMapping(value = "/thumbnail/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取缩略图")
    public void thumbnailFile(@PathVariable("fileId") String fileId, HttpServletRequest request,
                              HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=604800");

        if (fileId == null || fileId.isEmpty()) {
            JsonResultUtils.writeErrorMessageJson("Invalid fileId", response);
            return;
        }

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo == null) {
            JsonResultUtils.writeErrorMessageJson("File not found", response);
            return;
        }

        String fileMd5 = fileInfo.getFileMd5();
        if (fileMd5 == null || fileMd5.isEmpty()) {
            JsonResultUtils.writeErrorMessageJson("Invalid file MD5", response);
            return;
        }

        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileMd5);
        if (fileStoreInfo == null) {
            JsonResultUtils.writeErrorMessageJson("File store info not found", response);
            return;
        }

        try {
            if (fileStoreInfo.getFileSize() == 0) {
                UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]), fileInfo.getFileName(), response);
                return;
            }

            int height = sanitizeParameter(request.getParameter("height"), 240, 1, 2048);
            int width = sanitizeParameter(request.getParameter("width"), 320, 1, 2048);
            int quality = sanitizeParameter(request.getParameter("quality"), 100, 1, 100);

            String tempDir = SystemTempFileUtils.getTempDirectory();
            String inFilePath = tempDir + fileMd5 + "_in.jpg";
            String outFilePath = tempDir + fileMd5 + "_out.jpg";

            try {
                try (InputStream fileStream = FileIOUtils.getFileStream(fileStore, fileStoreInfo)) {
                    if (fileStream != null) {
                        FileIOOpt.appendInputStreamToFile(fileStream, inFilePath);
                    }
                }
                ImageOpt.createThumbnail(inFilePath, width, height, quality, outFilePath);

                try (InputStream is = Files.newInputStream(Paths.get(outFilePath))) {
                    UploadDownloadUtils.downFileRange(request, response, is, fileStoreInfo.getFileSize(),
                        fileInfo.getFileName(), "inline", null);
                }
            } finally {
                FileSystemOpt.deleteFile(inFilePath);
                FileSystemOpt.deleteFile(outFilePath);
            }
        } catch (IOException e) {
            JsonResultUtils.writeErrorMessageJson("IO error: " + e.getMessage(), response);
        } catch (Exception e) {
            JsonResultUtils.writeErrorMessageJson("Unexpected error: " + e.getMessage(), response);
        }
    }

    // 参数校验与范围限制
    private int sanitizeParameter(String value, int defaultValue, int min, int max) {
        int result = NumberBaseOpt.castObjectToInteger(value, defaultValue);
        return Math.max(min, Math.min(result, max));
    }


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
        // 设置缓存控制头，例如 "Cache-Control: public, max-age=604800" //一周 604800 一个与 3794400
        response.setHeader("Cache-Control", "public, max-age=604800");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String fileName = request.getParameter("fileName");
        if (!StringUtils.isBlank(fileName)) {
            fileInfo.setFileName(fileName);
        }
        if (GeneralAlgorithm.isEmpty(fileInfo)) {
            throw new ObjectException(ObjectException.BLANK_EXCEPTION,
                getI18nMessage("error.404.file_not_found", request, fileId));
        }
        if (StringUtils.isBlank(fileInfo.getFileMd5())) {
            throw new ObjectException(ObjectException.BLANK_EXCEPTION,
                getI18nMessage("error.404.file_not_found", request, fileId));
        }
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        downloadFile(fileStore, fileInfo, fileStoreInfo, request, response);
        fileInfoManager.writeDownloadFileLog(fileInfo, request);
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
            JsonResultUtils.writeErrorMessageJson(ResponseData.ERROR_FIELD_INPUT_NOT_VALID,
                getI18nMessage("error.701.field_is_blank", request, "fileIds"),
                response);
            return;
        }

        InputStream inputStream = null;
        long fileSize;
        boolean singleNotZip = BooleanBaseOpt.castObjectToBoolean(request.getParameter("singleNotZip"), false);
        // 如果只下载一个文件则 不压缩
        if (fileIds.length == 1 && singleNotZip) {
            FileInfo fileInfo = fileInfoManager.getObjectById(fileIds[0]);
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            fileSize = fileStoreInfo.getFileSize();
            String filePath = fileStoreInfo.getFileStorePath();
            fileName = fileInfo.getFileName();
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
                        fileUrls[j] = fileStoreInfo.getFileStorePath();
                        fileNames[j] = si.getFileName();
                        j++;
                    }
                }
                if (j == 0) {
                    JsonResultUtils.writeErrorMessageJson(ResponseData.ERROR_FIELD_INPUT_NOT_VALID,
                        getI18nMessage("error.701.field_is_blank", request, "fileIds"),
                        response);
                    return;
                }
                compressFiles(tempFilePath, fileUrls, fileNames, j);
            }
            file = new File(tempFilePath);
            fileSize = file.length();
            inputStream = Files.newInputStream(file.toPath());
        }

        UploadDownloadUtils.downFileRange(request, response,
            inputStream,
            fileSize, fileName, request.getParameter("downloadType"), null);
    }

    private void downloadFile(FileStore fileStore, FileInfo fileInfo, FileStoreInfo fileStoreInfo,
                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (null != fileInfo && fileStoreInfo != null) {
            if (fileStoreInfo.getFileSize() == 0) {
                UploadDownloadUtils.downloadFile(new ByteArrayInputStream(new byte[0]), fileInfo.getFileName(), response);
                return;
            }
            //对加密的进行特殊处理，ZIP加密的无需处理
            if (StringUtils.equalsAnyIgnoreCase(fileInfo.getEncryptType(), "A", "S", "M", "G")) {
                String password = SecurityOptUtils.decodeSecurityString(request.getParameter("password"));
                String tmpFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileStoreInfo.getFileSize());
                File tmpFile = new File(tmpFilePath);
                if (!fileStoreInfo.isTemp()) { //fileStore.checkFile(fileStoreInfo.getFileMd5(), fileStoreInfo.getFileSize()) ){// !fileStoreInfo.isTemp()){
                    try (InputStream downFile = FileIOUtils.getFileStream(fileStore, fileStoreInfo);
                         OutputStream diminationFile = new FileOutputStream(tmpFile)) {
                        FileEncryptUtils.decrypt(downFile, diminationFile, FileInfo.mapEncryptType(fileInfo.getEncryptType()), password);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        JsonResultUtils.writeErrorMessageJson(
                            FileServerConstant.ERROR_FILE_ENCRYPT,
                            getI18nMessage("error.423.cannt_encrypt_file", request, e.getMessage()),
                            response);
                        return;
                    }
                }
                try (InputStream inputStream = Files.newInputStream(tmpFile.toPath())) {
                    UploadDownloadUtils.downFileRange(request, response,
                        inputStream, tmpFile.length(), fileInfo.getFileName(), request.getParameter("downloadType"), null);
                }

                FileSystemOpt.deleteFile(tmpFile);
            } else {
                try (InputStream is = FileIOUtils.getFileStream(fileStore, fileStoreInfo)) {
                    UploadDownloadUtils.downFileRange(request, response, is,
                        fileStoreInfo.getFileSize(), fileInfo.getFileName(), request.getParameter("downloadType"), null);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
                }
            }
        } else {
            JsonResultUtils.writeHttpErrorMessage(
                FileServerConstant.ERROR_FILE_NOT_EXIST,
                getI18nMessage("error.404.file_not_found", request, "{fileInfo is null}"), response);
        }
    }

    private boolean noAuth(HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo, String closeAuth) {
        if (StringUtils.isNotBlank(closeAuth)) { //BooleanBaseOpt.castObjectToBoolean(closeAuth, false)
            return false;
        }
        String userCode = WebOptUtils.getCurrentUserCode(request);
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        if (StringUtils.isBlank(userCode)) {
            userCode = request.getParameter("userCode");
        }

        if (!fileLibraryInfoManager.checkAuth(topUnit, fileInfo, userCode, request.getParameter("authCode"))) {
            JsonResultUtils.writeErrorMessageJson(ResponseData.ERROR_FORBIDDEN,
                getI18nMessage("error.403.download_file_forbidden", request,
                    userCode, WebOptUtils.getCurrentUnitCode(request), request.getParameter("authCode")),
                response);
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
                    String fileName = fileNames[i];
                    int n = 0;
                    for (int j = 0; j < i; j++) {
                        if (StringUtils.equalsAnyIgnoreCase(fileName, fileNames[j]))
                            n++;
                    }
                    if (n > 0) {
                        fileName = fileName + "(" + n + ")";
                    }
                    ZipCompressor.compressFile(fis, fileName, out, basedir);
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
