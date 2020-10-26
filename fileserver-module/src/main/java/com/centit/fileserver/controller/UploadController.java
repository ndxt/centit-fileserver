package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileOptTaskQueue;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.FileUploadAuthorizedManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.support.algorithm.*;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/upload")
@Api(value = "文件断点上传，并且保存文件信息接口", tags = "文件断点上传，并且保存文件信息接口")
public class UploadController extends BaseController {
    public static final String UPLOAD_FILE_TOKEN_NAME = "uploadToken";
    protected Logger logger = LoggerFactory.getLogger(UploadController.class);
    @Value("${file.check.duplicate:true}")
    protected boolean checkDuplicate;

    @Value("${file.index.keepsingle.showpath:true}")
    protected boolean keepSingleIndexByShowpath;

    @Value("${file.check.upload.token:false}")
    protected boolean checkUploadToken;

    @Value("${app.runAsBoot:false}")
    protected static boolean runAsSpringBoot;

    @Autowired
    protected FileStore fileStore;

    @Autowired(required = false)
    protected ESIndexer documentIndexer;

    @Autowired
    private FileOptTaskQueue fileOptTaskQueue;

    @Autowired
    protected FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    private FileUploadAuthorizedManager fileUploadAuthorizedManager;

    public static void setRunAsSpringBoot(boolean asBoot){
        runAsSpringBoot = asBoot;
    }

    private static FileInfo fetchFileInfoFromRequest(HttpServletRequest request){

        FileInfo fileInfo = new FileInfo();

        fileInfo.setFileMd5(request.getParameter("token"));
        String fileName =UploadDownloadUtils.getRequestFirstOneParameter(request,"name","fileName");
        String fileState = request.getParameter("fileState");
        if(StringUtils.isNotBlank(fileState)) {
            fileInfo.setFileState(fileState);
        }

        fileInfo.setFileName(fileName);//*
        fileInfo.setOsId(request.getParameter("osId"));//*
        fileInfo.setOptId(request.getParameter("optId"));
        fileInfo.setOptMethod(request.getParameter("optMethod"));
        fileInfo.setOptTag(request.getParameter("optTag"));
        //这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
        String filePath = request.getParameter("filePath");
        if(StringUtils.isBlank(filePath)) {
            filePath = request.getParameter("fileShowPath");
        }
        fileInfo.setFileShowPath(filePath);
        fileInfo.setFileOwner(WebOptUtils.getCurrentUserCode(request));
        fileInfo.setFileUnit(request.getParameter("fileUnit"));
        fileInfo.setFileDesc(request.getParameter("fileDesc"));
        fileInfo.setLibraryId(request.getParameter("libraryId"));
        fileInfo.setCreateTime(DatetimeOpt.currentUtilDate());

        return fileInfo;
    }

    private static Map<String, Object> fetchPretreatInfoFromRequest(HttpServletRequest request){
        Map<String, Object> pretreatInfo = new HashMap<>();
        pretreatInfo.put("fileId", request.getParameter("fileId") );
        pretreatInfo.put("fileMd5", request.getParameter("token"));
        Long fileSize = NumberBaseOpt.parseLong(
            UploadDownloadUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);

        pretreatInfo.put("fileSize", fileSize);
        pretreatInfo.put("index", StringRegularOpt.isTrue(request.getParameter("index")));
//        pretreatInfo.setIsIsUnzip(StringRegularOpt.isTrue(request.getParameter("unzip")));
        pretreatInfo.put("pdf", StringRegularOpt.isTrue(request.getParameter("pdf")));
        pretreatInfo.put("watermark", request.getParameter("watermark"));
        pretreatInfo.put("thumbnail", StringRegularOpt.isTrue(request.getParameter("thumbnail")));
        pretreatInfo.put("height", NumberBaseOpt.parseLong(
                request.getParameter("height"), 200l).intValue());
        pretreatInfo.put("width", NumberBaseOpt.parseLong(
                request.getParameter("width"), 300l).intValue());
        //encryptType 加密方式 N : 没有加密 Z：zipFile D: DES加密
        String encryptType = request.getParameter("encryptType");
        if("zip".equalsIgnoreCase(encryptType) || "Z".equals(encryptType)) {
            pretreatInfo.put("encryptType", "Z");
        }
        if("des".equalsIgnoreCase(encryptType) || "D".equals(encryptType)) // 待删除
        {
            pretreatInfo.put("encryptType", "A");
        }
        //AES 暂未实现
        if("aes".equalsIgnoreCase(encryptType) || "A".equals(encryptType)) {
            pretreatInfo.put("encryptType", "A");
        }
        pretreatInfo.put("password", request.getParameter("password"));

        return pretreatInfo;
    }

    /**
     * 判断文件是否存在，如果文件已经存在可以实现秒传
     *
     * @param token token
     * @param size 大小
     */
    @ApiOperation(value = "检查文件是否存在")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400,
            allowedHeaders = "*", methods = RequestMethod.GET)
    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    @WrapUpResponseBody
    public boolean checkFileExists(String token, long size){
        return fileStore.checkFile(token, size);
    }

    /**
     * 获取文件 断点位置，前端根据断点位置续传
     *
     * @param token token
     * @param size 大小
     */
    @ApiOperation(value = "检查续传点，如果signal为continue请续传，如果为secondpass表示文件已存在需要调用秒传接口")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.GET)
    @RequestMapping(value = "/range", method = {RequestMethod.GET})
    @WrapUpResponseBody
    public JSONObject checkFileRange(String token, long size) {
        return UploadDownloadUtils.checkFileRange(fileStore, token, size);
    }

    /*
     * 这个是spring boot中无法正确运行，spring boot中不能获取 CommonsMultipartResolver
     */
    protected InputStream fetchISFromCommonsResolver(HttpServletRequest request, FileInfo fileInfo,Map<String, Object> pretreatInfo) throws IOException {
        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;
        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
            CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();
            FileItem fi = cMultipartFile.getFileItem();
            if (fi.isFormField()) {
                if (StringUtils.equals("fileInfo", fi.getFieldName())) {
                    FileInfo info = JSON.parseObject(fi.getString(), FileInfo.class);
                    fileInfo.copyNotNullProperty(info);
                } else if (StringUtils.equals("pretreatInfo", fi.getFieldName())) {
                    JSONObject pi = JSON.parseObject(fi.getString());
                    pretreatInfo = CollectionsOpt.unionTwoMap(pretreatInfo, pi);
                }
            } else {
                String fn = fi.getName();
                if(StringUtils.isBlank(fileInfo.getFileName()) && StringUtils.isNotBlank(fn)){
                    fileInfo.setFileName(fn);
                }
                fis = fi.getInputStream();
            }
        }
        return fis;
    }

    protected InputStream fetchISFromStandardResolver(HttpServletRequest request, FileInfo fileInfo,Map<String, Object> pretreatInfo) throws IOException {
        MultipartResolver resolver = new StandardServletMultipartResolver();
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
            MultipartFile cMultipartFile = entry.getValue();
            org.springframework.core.io.Resource resource = cMultipartFile.getResource();
            if(resource.isFile()) {
                String fileName = resource.getFilename();
                if(StringUtils.isNotBlank(fileName)){
                    fileInfo.setFileName(fileName);
                }
                fis = cMultipartFile.getInputStream();
            } else {
                String resourceName = resource.getFilename();
                if("fileInfo".equals(resourceName)){
                    FileInfo info = JSON.parseObject(cMultipartFile.getInputStream(), FileInfo.class);
                    fileInfo.copyNotNullProperty(info);
                } else if ("pretreatInfo".equals(resourceName)) {
                    JSONObject pi = JSON.parseObject(cMultipartFile.getInputStream(), JSONObject.class);
                    pretreatInfo = CollectionsOpt.unionTwoMap(pretreatInfo, pi);
                }
            }
        }
        return fis;
    }

    protected Triple<FileInfo, Map<String, Object>, InputStream>
        fetchUploadFormFromRequest(HttpServletRequest request) throws IOException {
        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        Map<String, Object> pretreatInfo = fetchPretreatInfoFromRequest(request);
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new ImmutableTriple<>(fileInfo, pretreatInfo, request.getInputStream());
        }
        InputStream fis = runAsSpringBoot?fetchISFromStandardResolver(request, fileInfo, pretreatInfo)
            :fetchISFromCommonsResolver(request, fileInfo, pretreatInfo);
        return new ImmutableTriple<>(fileInfo, pretreatInfo, fis);
    }

    /**
     * 处理文件信息 并按照指令对文件进行加工
     * param fs 文件的物理存储接口
     * @param fileMd5 加密
     * @param size 大小
     * @param fileInfo 文件对象
     * @param pretreatInfo PretreatInfo对象
     * @param response HttpServletResponse
     */
    private void completedFileStoreAndPretreat(String fileMd5, long size,
                                      FileInfo fileInfo, Map<String, Object> pretreatInfo,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            JSONObject json = storeAndPretreatFile(fileMd5, size, fileInfo, pretreatInfo);
            if(checkUploadToken){
                String uploadToken = request.getParameter(UPLOAD_FILE_TOKEN_NAME);
                fileUploadAuthorizedManager.consumeAuthorization(uploadToken);
            }
            JsonResultUtils.writeOriginalJson(json.toString(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_PRETREAT,
                    "文件上传成功，但是在保存前：" +
                        ObjectException.extortExceptionMessage(e), response);
        }
    }

    private JSONObject storeAndPretreatFile(String fileMd5, long size,
                                            FileInfo fileInfo, Map<String, Object> pretreatInfo)  {
        fileInfo.setFileMd5(fileMd5);
//        fileInfo.setFileSize(size);
//        fileInfo.setFileStorePath(fs.getFileStoreUrl(fileMd5, size));
        fileInfoManager.saveNewObject(fileInfo);
        String fileId = fileInfo.getFileId();
        try {
            // 先保存一个 临时文件； 如果文件已经存在是不会保存的
            fileStoreInfoManager.saveTempFileInfo(fileMd5,
                SystemTempFileUtils.getTempFilePath(fileMd5, size), size);

            if (!pretreatInfo.containsKey("encryptType")) { // 不加密的文件保存到服务器
                FileOptTaskInfo saveFileTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_SAVE_FILE);
                saveFileTaskInfo.setFileMd5(fileMd5);
                saveFileTaskInfo.setFileSize(size);
                fileOptTaskQueue.add(saveFileTaskInfo);
            } else  if ("A".equals(pretreatInfo.get("encryptType"))) {
                FileOptTaskInfo aesEncryptTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_AES_ENCRYPT);
                aesEncryptTaskInfo.setFileId(fileId);
                aesEncryptTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                aesEncryptTaskInfo.setTaskOptParam("password", pretreatInfo.get("password"));
                fileOptTaskQueue.add(aesEncryptTaskInfo);
            } else if ("Z".equals(pretreatInfo.get("encryptType"))) {
                if(pretreatInfo.get("password") == null) {
                    FileOptTaskInfo zipTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ZIP);
                    zipTaskInfo.setFileId(fileId);
                    zipTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                    fileOptTaskQueue.add(zipTaskInfo);
                } else {
                    FileOptTaskInfo encryptZipTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ENCRYPT_ZIP);
                    encryptZipTaskInfo.setFileId(fileId);
                    encryptZipTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                    encryptZipTaskInfo.setTaskOptParam("password", pretreatInfo.get("password"));
                    fileOptTaskQueue.add(encryptZipTaskInfo);
                }
            }

            if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("index"),false) && checkIndex(fileInfo.getFileType())) {
                FileOptTaskInfo indexTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_DOCUMENT_INDEX);
                indexTaskInfo.setFileId(fileId);
                indexTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                fileOptTaskQueue.add(indexTaskInfo);
            }

            if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("pdf"),false)||
            checkPdf(fileInfo)) {
                FileOptTaskInfo addPdfTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_CREATE_PDF);
                addPdfTaskInfo.setFileId(fileId);
                addPdfTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                fileOptTaskQueue.add(addPdfTaskInfo);
            }

            if (pretreatInfo.get("watermark") != null) {
                FileOptTaskInfo pdfWatermarkTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_PDF_WATERMARK);
                pdfWatermarkTaskInfo.setFileId(fileId);
                pdfWatermarkTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                pdfWatermarkTaskInfo.setTaskOptParam("watermark", pretreatInfo.get("watermark"));
                fileOptTaskQueue.add(pdfWatermarkTaskInfo);
            }

            if (BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("thumbnail"),false)) {
                FileOptTaskInfo thumbnailTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ADD_THUMBNAIL);
                thumbnailTaskInfo.setFileId(fileId);
                thumbnailTaskInfo.setFileSize((long) pretreatInfo.get("fileSize"));
                thumbnailTaskInfo.setTaskOptParam("width", pretreatInfo.get("width"));
                thumbnailTaskInfo.setTaskOptParam("height", pretreatInfo.get("height"));
                fileOptTaskQueue.add(thumbnailTaskInfo);
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }

        if(checkDuplicate){
            FileInfo duplicateFile = fileInfoManager.getDuplicateFile(fileInfo);
            if(duplicateFile != null){
                if(documentIndexer != null ){
                    documentIndexer.deleteDocument(duplicateFile.getFileId());
                }
                fileInfoManager.deleteObject(duplicateFile);
            }
        }

        return UploadDownloadUtils.makeRangeUploadCompleteJson(
            fileMd5, size, fileInfo.getFileName(), fileId);

    }
    private boolean checkIndex(String fileType) {
        switch (fileType) {
            case AbstractOfficeToPdf.DOC:
            case AbstractOfficeToPdf.DOCX:
            case AbstractOfficeToPdf.XLS:
            case AbstractOfficeToPdf.XLSX:
            case AbstractOfficeToPdf.PPT:
            case AbstractOfficeToPdf.PPTX:
            case AbstractOfficeToPdf.PDF:
            case AbstractOfficeToPdf.TXT:
                return true;
            default:
                return false;
        }
    }
    public static boolean checkPdf(FileInfo fileInfo) {
        if(StringBaseOpt.isNvl(fileInfo.getLibraryId())){
            return false;
        }
        switch (fileInfo.getFileType()) {
            case AbstractOfficeToPdf.DOC:
            case AbstractOfficeToPdf.DOCX:
            case AbstractOfficeToPdf.XLS:
            case AbstractOfficeToPdf.XLSX:
            case AbstractOfficeToPdf.PPT:
            case AbstractOfficeToPdf.PPTX:
                return true;
            default:
                return false;
        }
    }

    /**
     * 完成秒传，如果文件不存在会返回失败
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件秒传接口，需要post文件基本信息和预处理信息")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/secondpass", method = RequestMethod.POST)
    public void secondPass(String token, long size,
                           HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("utf8");
        if (fileStore.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            Triple<FileInfo, Map<String, Object>, InputStream> formData
                    = fetchUploadFormFromRequest(request);
            completedFileStoreAndPretreat(token, size, formData.getLeft(),
                formData.getMiddle(), request, response);
        } else {
            //临时文件大小相等 说明上传已完成，也可以秒传
            long tempFileSize = SystemTempFileUtils.checkTempFileSize(
                SystemTempFileUtils.getTempFilePath(token, size));
            if(tempFileSize == size){
                Triple<FileInfo, Map<String, Object>, InputStream> formData
                    = fetchUploadFormFromRequest(request);
                completedFileStoreAndPretreat(token, size, formData.getLeft(),
                    formData.getMiddle(), request, response);
            } else {
                JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "文件不存在无法实现秒传，MD5(uploadedSize/fileSize)："
                        + token+"("+tempFileSize+"/"+size+")", response);
            }
        }
    }

    protected boolean checkUploadAuthorization(HttpServletRequest request, HttpServletResponse response){

        String uploadToken = request.getParameter(UPLOAD_FILE_TOKEN_NAME);
        if( fileUploadAuthorizedManager.checkAuthorization(uploadToken)<1){
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_FORBIDDEN,
                    "没有权限上传文件,请检查参数:" + UPLOAD_FILE_TOKEN_NAME, response);
            return false;
        }
        return true;
    }

    /**
     * 续传文件（range） 如果文件已经传输完成 对文件进行保存
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "断点续传接口")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/range", method = {RequestMethod.POST})
    public void uploadFileRange(
            String token, long size,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if(checkUploadToken && !checkUploadAuthorization(request, response)){
            return;
        }

        Triple<FileInfo, Map<String, Object>, InputStream> formData
                = fetchUploadFormFromRequest(request);
        if (fileStore.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            completedFileStoreAndPretreat(token, size, formData.getLeft(),
                    formData.getMiddle(), request, response);
            return;
        }
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);

        try {
            long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, formData.getRight(), token, size, request);
            if (uploadSize == size) {
                //上传到临时区成功
                completedFileStoreAndPretreat(token, size, formData.getLeft(),
                    formData.getMiddle(), request, response);

            } else /*if (uploadSize > 0)*/ {
                JSONObject json = UploadDownloadUtils.makeRangeUploadJson(uploadSize, token, token+"_"+size);
                JsonResultUtils.writeOriginalJson(json.toString(), response);
            }
            OperationLogCenter.log(OperationLog.create().operation(FileLogController.LOG_OPERATION_NAME)
                .unit(formData.getLeft().getLibraryId())
                .user(WebOptUtils.getCurrentUserCode(request))
                .method("上传").tag(formData.getLeft().getFileId())
                .time(DatetimeOpt.currentUtilDate())
                .content(formData.getLeft().getFileName())
                .newObject(formData.getLeft()));

        }catch (ObjectException e){
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeHttpErrorMessage(e.getExceptionCode(),
                    e.getMessage(), response);
        }

    }

    /**
     * 上传整个文件适用于IE8
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件整体上传结构，适用于IE8")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = {"/file", "/upload"}, method = RequestMethod.POST)
    public void uploadFile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if(checkUploadToken && !checkUploadAuthorization(request, response)){
            return;
        }
        request.setCharacterEncoding("utf8");

        Triple<FileInfo, Map<String, Object>, InputStream> formData
                = fetchUploadFormFromRequest(request);
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String token = formData.getLeft().getFileMd5();
        boolean needCheck = !StringUtils.isBlank(token);
        Long size = NumberBaseOpt.parseLong(
            request.getParameter("size"), -1l);
        if(size<1){
            size= NumberBaseOpt.parseLong(
                request.getParameter("fileSize"), -1l);
        }
        String tempFilePath = needCheck ?
            SystemTempFileUtils.getTempFilePath(token, size) :
            SystemTempFileUtils.getRandomTempFilePath();
        try {
            // 整体上传清除 残留文件
            if(FileSystemOpt.existFile(tempFilePath)) {// 临时文件已存在
                FileSystemOpt.deleteFile(tempFilePath);
            }
            int fileSize = FileIOOpt.writeInputStreamToFile(formData.getRight(), tempFilePath);
            File tempFile = new File(tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(tempFile);

            boolean isValid = fileSize != 0;
            if (needCheck) {
                isValid = size == (long)fileSize && token.equals(fileMd5);
            } else {
                String renamePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
                tempFile.renameTo(new File(renamePath));
            }

            if (isValid && !StringUtils.isBlank(formData.getLeft().getFileName())) {
                FileInfo fileInfo = formData.getLeft();
                fileInfo.setFileMd5(fileMd5);
                completedFileStoreAndPretreat(fileMd5, fileSize,
                    formData.getLeft(), formData.getMiddle(), request, response);
            } else {
                FileSystemOpt.deleteFile(tempFilePath);
                JsonResultUtils.writeErrorMessageJson("文件上传出错，fileName参数必须传，如果传了token和size参数请检查是否正确，并确认选择的文件！", response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }
}
