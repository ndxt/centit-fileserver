package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.fileaccess.PretreatInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.FileUploadAuthorizedManager;
import com.centit.fileserver.utils.*;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ObjectException;
import com.centit.framework.common.ResponseData;
import com.centit.framework.core.controller.BaseController;
import com.centit.search.service.Indexer;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/upload")

public class UploadController extends BaseController {
    public static final String UPLOAD_FILE_TOKEN_NAME = "uploadToken";

    @Value("${file.check.duplicate:true}")
    protected boolean checkDuplicate;

    @Value("${file.index.keepsingle.showpath:true}")
    protected boolean keepSingleIndexByShowpath;

    @Value("${file.check.upload.token:false}")
    protected boolean checkUploadToken;


    @Resource
    protected FileStore fileStore;

    @Resource
    protected Indexer documentIndexer;

    @Resource
    private FileOptTaskQueue fileOptTaskQueue;

    @Resource
    protected FileInfoManager fileInfoManager;

    @Resource
    private FileStoreInfoManager fileStoreInfoManager;

    @Resource
    private FileUploadAuthorizedManager fileUploadAuthorizedManager;


    private static FileInfo fetchFileInfoFromRequest(HttpServletRequest request){

        FileInfo fileInfo = new FileInfo();

        fileInfo.setFileMd5(request.getParameter("token"));
        Long fileSize = NumberBaseOpt.parseLong(
                request.getParameter("size"), -1l);
        if(fileSize<1){
            fileSize= NumberBaseOpt.parseLong(
                    request.getParameter("fileSize"), -1l);
        }
//        fileInfo.setFileSize(fileSize);
        String fileName = request.getParameter("name");
        if(StringUtils.isBlank(fileName))
            fileName = request.getParameter("fileName");

        String fileState = request.getParameter("fileState");
        if(StringUtils.isNotBlank(fileState))
            fileInfo.setFileState(fileState);

        fileInfo.setFileName(fileName);//*
        fileInfo.setOsId(request.getParameter("osId"));//*
        fileInfo.setOptId(request.getParameter("optId"));
        fileInfo.setOptMethod(request.getParameter("optMethod"));
        fileInfo.setOptTag(request.getParameter("optTag"));
        //这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
        String filePath = request.getParameter("filePath");
        if(StringUtils.isBlank(filePath))
            filePath = request.getParameter("fileShowPath");
        fileInfo.setFileShowPath(filePath);
        fileInfo.setFileOwner(request.getParameter("fileOwner"));
        fileInfo.setFileUnit(request.getParameter("fileUnit"));
        fileInfo.setFileDesc(request.getParameter("fileDesc"));
        fileInfo.setCreateTime(DatetimeOpt.currentUtilDate());

        return fileInfo;
    }

    private static PretreatInfo fetchPretreatInfoFromRequest(HttpServletRequest request){
        PretreatInfo pretreatInfo = new PretreatInfo();
        pretreatInfo.setFileId(request.getParameter("fileId") );
        pretreatInfo.setFileMd5(request.getParameter("token"));
        Long fileSize = NumberBaseOpt.parseLong(
                request.getParameter("size"), -1l);
        if(fileSize<1){
            fileSize= NumberBaseOpt.parseLong(
                    request.getParameter("fileSize"), -1l);
        }
        pretreatInfo.setFileSize(fileSize);
        pretreatInfo.setIsIndex(StringRegularOpt.isTrue(request.getParameter("index")));
//        pretreatInfo.setIsIsUnzip(StringRegularOpt.isTrue(request.getParameter("unzip")));
        pretreatInfo.setAddPdf(StringRegularOpt.isTrue(request.getParameter("pdf")));
        pretreatInfo.setWatermark(request.getParameter("watermark"));
        pretreatInfo.setAddThumbnail(StringRegularOpt.isTrue(request.getParameter("thumbnail")));
        pretreatInfo.setThumbnailHeight(NumberBaseOpt.parseLong(
                request.getParameter("height"), 200l).intValue());
        pretreatInfo.setThumbnailWidth(NumberBaseOpt.parseLong(
                request.getParameter("width"), 300l).intValue());
        //encryptType 加密方式 N : 没有加密 Z：zipFile D: DES加密
        String encryptType = request.getParameter("encryptType");
        if("zip".equalsIgnoreCase(encryptType) || "Z".equals(encryptType))
            pretreatInfo.setEncryptType("Z");
        if("des".equalsIgnoreCase(encryptType) || "D".equals(encryptType)) // 待删除
            pretreatInfo.setEncryptType("A");
        //AES 暂未实现
        if("aes".equalsIgnoreCase(encryptType) || "A".equals(encryptType))
            pretreatInfo.setEncryptType("A");
        pretreatInfo.setEncryptPassword(request.getParameter("password"));

        return pretreatInfo;
    }

    /**
     * 判断文件是否存在，如果文件已经存在可以实现秒传
     *
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400,
            allowedHeaders = "*", methods = RequestMethod.GET)
    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    public void checkFileExists(String token, long size,
                                HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JsonResultUtils.writeOriginalObject(fileStore.checkFile(token, size), response);
    }

    /**
     * 获取文件 断点位置，前端根据断点位置续传
     *
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.GET)
    @RequestMapping(value = "/range", method = {RequestMethod.GET})
    public void checkFileRange(String token, long size,
                               HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //FileRangeInfo fr = new FileRangeInfo(token,size);
        long tempFileSize = 0;
        // 如果文件已经存在则完成秒传，无需再传
        if (fileStore.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
            //添加完成 后 相关的处理  类似与 uploadRange
            /*FileInfo fileInfo = fetchFileInfoFromRequest(request);
            if (StringUtils.isNotBlank(fileInfo.getFileName()) &&
                    StringUtils.isNotBlank(fileInfo.getOsId()) &&
                    StringUtils.isNotBlank(fileInfo.getOptId())) {
                PretreatInfo pretreatInfo = fetchPretreatInfoFromRequest(request);
                completedFileStoreAndPretreat(fileStore, token, size, fileInfo, pretreatInfo, request, response);
                return;
            }*/
            tempFileSize = size;
        } else {
            //检查临时目录中的文件大小，返回文件的起始点
            //String tempFilePath = FileUploadUtils.getTempFilePath(token, size);
            tempFileSize = SystemTempFileUtils.checkTempFileSize(
                    SystemTempFileUtils.getTempFilePath(token, size));
        }

        JsonResultUtils.writeOriginalJson(UploadDownloadUtils.
                makeRangeUploadJson(tempFileSize).toJSONString(), response);
    }

    private Triple<FileInfo, PretreatInfo, InputStream>
    fetchUploadFormFromRequest(HttpServletRequest request) throws IOException {
        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        PretreatInfo pretreatInfo = fetchPretreatInfoFromRequest(request);
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
            return new ImmutableTriple<>(fileInfo, pretreatInfo, request.getInputStream());

        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
//        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;
        String fileName = fileInfo.getFileName();

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {

            CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();

            FileItem fi = cMultipartFile.getFileItem();
            if (fi.isFormField()) {
                if (StringUtils.equals("fileInfo", fi.getFieldName())) {
                    try {
                        FileInfo fsi = JSON.parseObject(fi.getString(), FileInfo.class);
                        fileInfo.copyNotNullProperty(fsi);
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                } else if (StringUtils.equals("pretreatInfo", fi.getFieldName())) {
                    try {
                        PretreatInfo pi = JSON.parseObject(fi.getString(), PretreatInfo.class);
                        pretreatInfo.copyNotNullProperty(pi);
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            } else {
                fileName = fi.getName();
                fis = fi.getInputStream();
            }
        }
        fileInfo.setFileName(fileName);
        return new ImmutableTriple<>(fileInfo, pretreatInfo, fis);
    }

    /**
     * 处理文件信息 并按照指令对文件进行加工
     * @param fs 文件的物理存储接口
     * @param fileMd5 加密
     * @param size 大小
     * @param fileInfo 文件对象
     * @param pretreatInfo PretreatInfo对象
     * @param response HttpServletResponse
     */
    private void completedFileStoreAndPretreat(FileStore fs, String fileMd5, long size,
                                      FileInfo fileInfo, PretreatInfo pretreatInfo,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            JSONObject json = storeAndPretreatFile(fs, fileMd5, size, fileInfo, pretreatInfo);
            JsonResultUtils.writeOriginalJson(json.toString(), response);
            if(checkUploadToken){
                String uploadToken = request.getParameter(UPLOAD_FILE_TOKEN_NAME);
                fileUploadAuthorizedManager.consumeAuthorization(uploadToken);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_PRETREAT,
                    "文件上传成功，但是在保存前：" + e.getMessage(), response);
        }
    }


    /**
     * 解压缩文件
     * @param fs 文件的物理存储接口
     * @param fileInfo 文件对象
     * @param pretreatInfo  PretreatInfo
     * @param rootPath 根路径
     * @throws Exception Exception
     */
    /*
    private void unzip(FileStore fs, FileInfo fileInfo, PretreatInfo pretreatInfo, String rootPath) {
        File zipFile = null;
        try {
            zipFile = fs.getFile(fileInfo.getFileStorePath());
        }catch (IOException e){
            throw new ObjectException(e);
        }

        try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry.getName());

                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                int fi = name.indexOf('/');
                int di = name.lastIndexOf('/');

                String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
                int size = FileIOOpt.writeInputStreamToFile(zis, tempFilePath);
                String token = FileMD5Maker.makeFileMD5(new File(tempFilePath));
                fileStore.saveFile(tempFilePath, token, size);

                FileInfo fileInfoTemp = new FileInfo();
                fileInfoTemp.copyNotNullProperty(fileInfo);
                fileInfoTemp.setFileMd5(token);
                fileInfoTemp.setFileName(name.substring(di + 1));
                fileInfoTemp.setFileType(FileType.getFileExtName(name.substring(di + 1)));

                // ① name: test/4.东航国际运输条件.docx && showPath: null =========> showPath: null
                // ② name: test/4.东航国际运输条件.docx && showPath: a =========> showPath: a
                // ③ name: test/b/4.东航国际运输条件.docx && showPath: null =========> showPath: b
                // ④ name: test/b/4.东航国际运输条件.docx && showPath: a =========> showPath: a/b
                if (fi == di) {
                    // 情况 ① ②
                    fileInfoTemp.setFileShowPath(rootPath);
                } else {
                    // 情况 ③ ④
                    fileInfoTemp.setFileShowPath(rootPath == null ? name.substring(fi + 1, di) : (rootPath + name.substring(fi, di)));
                }

                fileInfoTemp.setFileStorePath(fileStore.getFileStoreUrl(token, size));

                PretreatInfo pretreatInfoTemp = new PretreatInfo();
                pretreatInfoTemp.copyNotNullProperty(pretreatInfo);
                pretreatInfoTemp.setIsIsUnzip(false);

                storeAndPretreatFile(fileStore, token, size, fileInfoTemp, pretreatInfoTemp);

                FileSystemOpt.deleteFile(tempFilePath);
            }
        } catch (IOException ie){
            throw new ObjectException(ie);
        }
    }*/

    private JSONObject storeAndPretreatFile(FileStore fs, String fileMd5, long size,
                                            FileInfo fileInfo, PretreatInfo pretreatInfo)  {

        fileInfo.setFileMd5(fileMd5);
//        fileInfo.setFileSize(size);
//        fileInfo.setFileStorePath(fs.getFileStoreUrl(fileMd5, size));

        fileInfoManager.saveNewObject(fileInfo);
        fileStoreInfoManager.increaseFileReferenceCount(fileMd5);
        String fileId = fileInfo.getFileId();
        try {
            if (pretreatInfo.needPretreat()) {
//                fileInfo = FilePretreatment.pretreatment(fs,documentIndexer, fileInfo, pretreatInfo);
                if (pretreatInfo.getIsIndex()) {
                    FileOptTaskInfo indexTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_DOCUMENT_INDEX);
                    indexTaskInfo.setTaskOptParam("fileId", fileId);
                    indexTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                    fileOptTaskQueue.add(indexTaskInfo);
                }

                if (pretreatInfo.getAddPdf()) {
                    FileOptTaskInfo addPdfTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_CREATE_PDF);
                    addPdfTaskInfo.setTaskOptParam("fileId", fileId);
                    addPdfTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                    fileOptTaskQueue.add(addPdfTaskInfo);
                }

                // 生成水印函数有问题，先注释掉
//                if (!StringUtils.isBlank(pretreatInfo.getWatermark())) {
//                    FileOptTaskInfo pdfWatermarkTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_PDF_WATERMARK);
//                    pdfWatermarkTaskInfo.setTaskOptParam("fileId", fileId);
//                    pdfWatermarkTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
//                    pdfWatermarkTaskInfo.setTaskOptParam("watermark", pretreatInfo.getWatermark());
//                    fileOptTaskQueue.add(pdfWatermarkTaskInfo);
//                }

                if (pretreatInfo.getAddThumbnail()) {
                    FileOptTaskInfo thumbnailTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ADD_THUMBNAIL);
                    thumbnailTaskInfo.setTaskOptParam("fileId", fileId);
                    thumbnailTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                    thumbnailTaskInfo.setTaskOptParam("width", pretreatInfo.getThumbnailWidth());
                    thumbnailTaskInfo.setTaskOptParam("height", pretreatInfo.getThumbnailHeight());
                    fileOptTaskQueue.add(thumbnailTaskInfo);
                }

                if ("A".equals(pretreatInfo.getEncryptType())) {
                    FileOptTaskInfo aesEncryptTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_AES_ENCRYPT);
                    aesEncryptTaskInfo.setTaskOptParam("fileId", fileId);
                    aesEncryptTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                    aesEncryptTaskInfo.setTaskOptParam("password", pretreatInfo.getEncryptPassword());
                    fileOptTaskQueue.add(aesEncryptTaskInfo);
                } else if ("Z".equals(pretreatInfo.getEncryptType())) {
                    if(StringUtils.isBlank(pretreatInfo.getEncryptPassword())) {
                        FileOptTaskInfo zipTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ZIP);
                        zipTaskInfo.setTaskOptParam("fileId", fileId);
                        zipTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                        fileOptTaskQueue.add(zipTaskInfo);
                    } else {
                        FileOptTaskInfo encryptZipTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_ENCRYPT_ZIP);
                        encryptZipTaskInfo.setTaskOptParam("fileId", fileId);
                        encryptZipTaskInfo.setTaskOptParam("fileSize", pretreatInfo.getFileSize());
                        encryptZipTaskInfo.setTaskOptParam("password", pretreatInfo.getEncryptPassword());
                        fileOptTaskQueue.add(encryptZipTaskInfo);
                    }
                }
            }

            // 只有zip文件才需要解压
            /*if (pretreatInfo.getIsUnzip() && "zip".equals(fileInfo.getFileType())) {
                unzip(fs, fileInfo, pretreatInfo, fileInfo.getFileShowPath());
            }*/
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }

        if(checkDuplicate){
            FileInfo duplicateFile = fileInfoManager.getDuplicateFile(fileInfo);
            if(duplicateFile != null){
                if(documentIndexer != null && "I".equals(duplicateFile.getIndexState())){
                    documentIndexer.deleteDocument(duplicateFile.getFileId());
                }
                fileInfoManager.deleteFile(duplicateFile);
            }
        }

        if(keepSingleIndexByShowpath ){
            FileInfo duplicateFile = fileInfoManager.getDuplicateFileByShowPath(fileInfo);
            if(duplicateFile != null){
                if(documentIndexer != null && "I".equals(duplicateFile.getIndexState())){
                    documentIndexer.deleteDocument(duplicateFile.getFileId());
                }
            }
        }

//        fileInfoManager.updateObject(fileInfo);
        // 返回响应
        JSONObject json = new JSONObject();
        json.put("start", size);
        json.put("name", fileInfo.getFileName());
        json.put("token", fileMd5);
        json.put("success", true);
        json.put("fileId", fileId);

        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, "上传成功");
        json.put(ResponseData.RES_DATA_FILED, fileInfo);

        return json;
    }

    /**
     * 完成秒传，如果文件不存在会返回失败
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/secondpass", method = RequestMethod.POST)
    public void secondPass(String token, long size,
                           HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("utf8");

        if (fileStore.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            Triple<FileInfo, PretreatInfo, InputStream> formData
                    = fetchUploadFormFromRequest(request);
            completedFileStoreAndPretreat(fileStore, token, size, formData.getLeft(), formData.getMiddle(), request, response);
            return;
        } else {
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "文件不存在无法实现秒传，MD5：" + token, response);
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
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/range", method = {RequestMethod.POST})
    public void uploadFileRange(
            String token, long size,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if(checkUploadToken && !checkUploadAuthorization(request, response)){
            return;
        }

        Triple<FileInfo, PretreatInfo, InputStream> formData
                = fetchUploadFormFromRequest(request);


        if (fileStore.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            completedFileStoreAndPretreat(fileStore, token, size, formData.getLeft(),
                    formData.getMiddle(), request, response);
            return;
        }

        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);

        try {
            long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, formData.getRight(), token, size, request);
            if (uploadSize==0) {
                //上传到临时区成功
//                fileStore.saveFile(tempFilePath, token, size);
                PretreatInfo pretreatInfo = formData.getMiddle();
                if ("N".equals(pretreatInfo.getEncryptType())) { // 不加密的文件保存到服务器
                    FileOptTaskInfo saveFileTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_SAVE_FILE);
                    saveFileTaskInfo.setTaskOptParam("fileMd5", token);
                    saveFileTaskInfo.setTaskOptParam("fileSize", size);
                    fileOptTaskQueue.add(saveFileTaskInfo);
                }

                completedFileStoreAndPretreat(fileStore, token, size, formData.getLeft(),
                    pretreatInfo, request, response);

            } else if (uploadSize > 0) {
                JSONObject json = UploadDownloadUtils.makeRangeUploadJson(uploadSize);
                FileInfo fileInfo = new FileInfo();
                //fileInfo.setFileSize(uploadSize);
                json.put(ResponseData.RES_DATA_FILED, fileInfo);
                JsonResultUtils.writeOriginalJson(json.toString(), response);
            }
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
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public void uploadFile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if(checkUploadToken && !checkUploadAuthorization(request, response)){
            return;
        }
        request.setCharacterEncoding("utf8");
        Triple<FileInfo, PretreatInfo, InputStream> formData
                = fetchUploadFormFromRequest(request);
        String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
        try {
            int fileSize = FileIOOpt.writeInputStreamToFile(formData.getRight(), tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));

//            fileStore.saveFile(tempFilePath);
            FileOptTaskInfo saveFileTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_SAVE_FILE);
            saveFileTaskInfo.setTaskOptParam("fileMd5", fileMd5);
            saveFileTaskInfo.setTaskOptParam("fileSize", fileSize);
            fileOptTaskQueue.add(saveFileTaskInfo);

            completedFileStoreAndPretreat(fileStore, fileMd5, fileSize,
                    formData.getLeft(), formData.getMiddle(), request, response);
            FileSystemOpt.deleteFile(tempFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }


    /**
     * 保存文件
     * @param fs 文件的物理存储接口
     * @param fileMd5 加密
     * @param size 大小
     * @param fileName 文件名
     * @param response HttpServletResponse
     */

    private void completedStoreFile(FileStore fs, String fileMd5, long size,
                                    String fileName, HttpServletResponse response) {
        try {

            String fileId =  fileMd5 +"_"+String.valueOf(size)+"."+
                    FileType.getFileExtName(fileName);
            // 返回响应

            Map<String,String> fileInfo= new HashMap<>();
            fileInfo.put("src","/service/download/unprotected/"+fileId+"?fileName="+fileName);
            fileInfo.put("fileId", fileId);
            fileInfo.put("token", fileMd5);
            fileInfo.put("name", fileName);

            JSONObject json = new JSONObject();
            json.put("start", size);
            json.put("name", fileName);
            json.put("token", fileMd5);
            json.put("success", true);
            json.put("fileId", fileId);

            json.put(ResponseData.RES_CODE_FILED, 0);
            json.put(ResponseData.RES_MSG_FILED, "上传成功");
            json.put(ResponseData.RES_DATA_FILED, fileInfo);

            JsonResultUtils.writeOriginalJson(json.toString(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_PRETREAT,
                    "文件上传成功，但是在保存前：" + e.getMessage(), response);
        }
    }


    /**
     * 获取文件 断点位置，前端根据断点位置续传
     *
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.GET)
    @RequestMapping(value = "/storerange", method = {RequestMethod.GET})
    public void checkStoreRange(String token, long size,
                               HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //FileRangeInfo fr = new FileRangeInfo(token,size);
        Pair<String, InputStream> fileInfo = UploadDownloadUtils.fetchInputStreamFromMultipartResolver(request);

        long tempFileSize = 0;
        // 如果文件已经存在则完成秒传，无需再传
        if (fileStore.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
            //添加完成 后 相关的处理  类似与 uploadRange
            completedStoreFile(fileStore, token, size, fileInfo.getLeft(), response);
            tempFileSize = size;
        } else {
            //检查临时目录中的文件大小，返回文件的其实点
            //String tempFilePath = FileUploadUtils.getTempFilePath(token, size);
            tempFileSize = SystemTempFileUtils.checkTempFileSize(
                    SystemTempFileUtils.getTempFilePath(token, size));
        }

        JsonResultUtils.writeOriginalJson(UploadDownloadUtils.
                makeRangeUploadJson(tempFileSize).toJSONString(), response);
    }


    /**
     * 续传文件（range） 如果文件已经传输完成 对文件进行保存
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/storerange", method = {RequestMethod.POST})
    public void storeRange(
            String token, long size,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Pair<String, InputStream> fileInfo = UploadDownloadUtils.fetchInputStreamFromMultipartResolver(request);
        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);

        if (fileStore.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            completedStoreFile(fileStore, token, size, fileInfo.getLeft(), response);
            return;
        }

        try {
            long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, fileInfo.getRight(), token, size, request);
            if(uploadSize==0){
                completedStoreFile(fileStore, token, size, fileInfo.getLeft(), response);
            }else if( uploadSize>0){

                JsonResultUtils.writeOriginalJson(UploadDownloadUtils.
                        makeRangeUploadJson(uploadSize).toJSONString(), response);
            }

        }catch (ObjectException e){
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeHttpErrorMessage(e.getExceptionCode(),
                    e.getMessage(), response);
        }
    }
    /**
     * 仅仅保存文件不记录任何记录
     * 上传整个文件适用于IE8
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void storeFile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("utf8");
        String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
        try {
            Pair<String, InputStream> fileInfo = UploadDownloadUtils.fetchInputStreamFromMultipartResolver(request);
            int fileSize = FileIOOpt.writeInputStreamToFile(fileInfo.getRight() , tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));

//            fileStore.saveFile(tempFilePath);
            FileOptTaskInfo saveFileTaskInfo = new FileOptTaskInfo(FileOptTaskInfo.OPT_SAVE_FILE);
            saveFileTaskInfo.setTaskOptParam("fileMd5", fileMd5);
            saveFileTaskInfo.setTaskOptParam("fileSize", fileSize);
            fileOptTaskQueue.add(saveFileTaskInfo);

            completedStoreFile(fileStore, fileMd5, fileSize, fileInfo.getLeft(), response);
            FileSystemOpt.deleteFile(tempFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }

}
