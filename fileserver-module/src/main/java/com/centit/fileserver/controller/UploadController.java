package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.fileaccess.FilePretreatment;
import com.centit.fileserver.fileaccess.FileStoreFactory;
import com.centit.fileserver.fileaccess.PretreatInfo;
import com.centit.fileserver.fileaccess.SystemTempFileUtils;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ObjectException;
import com.centit.framework.common.ResponseData;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Controller
@RequestMapping("/upload")

public class UploadController extends BaseController {

    @Value("${file.check.duplicate}")
    protected boolean checkDuplicate;

    @Value("${file.index.keepsingle.showpath}")
    protected boolean keepSingleIndexByShowpath;

    @Resource
    private FileStoreInfoManager fileStoreInfoManager;

    private static FileStoreInfo fetchFileInfoFromRequest(HttpServletRequest request){

        FileStoreInfo fileInfo = new FileStoreInfo();

        fileInfo.setFileMd5(request.getParameter("token"));
        Long fileSize = NumberBaseOpt.parseLong(
                request.getParameter("size"), -1l);
        if(fileSize<1){
            fileSize= NumberBaseOpt.parseLong(
                    request.getParameter("fileSize"), -1l);
        }
        fileInfo.setFileSize(fileSize);
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
        pretreatInfo.setIsIsUnzip(StringRegularOpt.isTrue(request.getParameter("unzip")));
        pretreatInfo.setAddPdf(StringRegularOpt.isTrue(request.getParameter("pdf")));
        pretreatInfo.setWatermark(request.getParameter("watermark"));
        pretreatInfo.setAddThumbnail(StringRegularOpt.isTrue(request.getParameter("thumbnail")));
        pretreatInfo.setThumbnailHeight(NumberBaseOpt.parseLong(
                request.getParameter("height"), 200l).intValue());
        pretreatInfo.setThumbnailWidth(NumberBaseOpt.parseLong(
                request.getParameter("width"), 300l).intValue());
        //encryptType 加密方式 N : 没有加密 Z：zipFile D:DES加密
        String encryptType = request.getParameter("encryptType");
        if("zip".equalsIgnoreCase(encryptType) || "Z".equals(encryptType))
            pretreatInfo.setEncryptType("Z");
        if("des".equalsIgnoreCase(encryptType) || "D".equals(encryptType))
            pretreatInfo.setEncryptType("D");
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

        FileStore fs = FileStoreFactory.createDefaultFileStore();

        JsonResultUtils.writeOriginalObject(fs.checkFile(token, size), response);
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
        FileStore fs = FileStoreFactory.createDefaultFileStore();
        long tempFileSize = 0;
        // 如果文件已经存在则完成秒传，无需再传
        if (fs.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
            //添加完成 后 相关的处理  类似与 uploadRange
            FileStoreInfo fileInfo = fetchFileInfoFromRequest(request);
            if (StringUtils.isNotBlank(fileInfo.getFileName()) &&
                    StringUtils.isNotBlank(fileInfo.getOsId()) &&
                    StringUtils.isNotBlank(fileInfo.getOptId())) {
                PretreatInfo pretreatInfo = fetchPretreatInfoFromRequest(request);
                completedFileStoreAndPretreat(fs, token, size, fileInfo, pretreatInfo, response);
                return;
            }
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

    private Triple<FileStoreInfo, PretreatInfo, InputStream>
    fetchUploadFormFromRequest(HttpServletRequest request) throws IOException {
        FileStoreInfo fileInfo = fetchFileInfoFromRequest(request);
        PretreatInfo pretreatInfo = fetchPretreatInfoFromRequest(request);
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
            return new ImmutableTriple<>(fileInfo, pretreatInfo, request.getInputStream());

        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
//		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;
        String fileName = fileInfo.getFileName();

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {

            CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();

            FileItem fi = cMultipartFile.getFileItem();
            if (fi.isFormField()) {
                if (StringUtils.equals("fileInfo", fi.getFieldName())) {
                    try {
                        FileStoreInfo fsi = JSON.parseObject(fi.getString(), FileStoreInfo.class);
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
                                               FileStoreInfo fileInfo, PretreatInfo pretreatInfo,
                                               HttpServletResponse response) {
        try {
            JSONObject json = completedFileStoreAndPretreat(fs, fileMd5, size, fileInfo, pretreatInfo);
            JsonResultUtils.writeOriginalJson(json.toString(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeAjaxErrorMessage(
                    FileServerConstant.ERROR_FILE_PRETREAT,
                    "文件上传成功，但是在保存前：" + e.getMessage(), response);
        }
    }

    /**
     * 解压缩文件
     * @param fs 文件的物理存储接口
     * @param fileStoreInfo 文件对象
     * @param pretreatInfo  PretreatInfo
     * @param rootPath 根路径
     * @throws Exception Exception
     */
    private void unzip(FileStore fs, FileStoreInfo fileStoreInfo, PretreatInfo pretreatInfo, String rootPath) {

        File zipFile = null;
        try {
            zipFile = fs.getFile(fileStoreInfo.getFileStorePath());
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

                FileStore fsTemp = FileStoreFactory.createDefaultFileStore();
                String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
                int size = FileIOOpt.writeInputStreamToFile(zis, tempFilePath);
                String token = FileMD5Maker.makeFileMD5(new File(tempFilePath));
                fsTemp.saveFile(tempFilePath, token, size);

                FileStoreInfo fileStoreInfoTemp = new FileStoreInfo();
                fileStoreInfoTemp.copyNotNullProperty(fileStoreInfo);
                fileStoreInfoTemp.setFileMd5(token);
                fileStoreInfoTemp.setFileName(name.substring(di + 1));
                fileStoreInfoTemp.setFileType(FileType.getFileExtName(name.substring(di + 1)));

                // ① name: test/4.东航国际运输条件.docx && showPath: null =========> showPath: null
                // ② name: test/4.东航国际运输条件.docx && showPath: a =========> showPath: a
                // ③ name: test/b/4.东航国际运输条件.docx && showPath: null =========> showPath: b
                // ④ name: test/b/4.东航国际运输条件.docx && showPath: a =========> showPath: a/b
                if (fi == di) {
                    // 情况 ① ②
                    fileStoreInfoTemp.setFileShowPath(rootPath);
                } else {
                    // 情况 ③ ④
                    fileStoreInfoTemp.setFileShowPath(rootPath == null ? name.substring(fi + 1, di) : (rootPath + name.substring(fi, di)));
                }

                fileStoreInfoTemp.setFileStorePath(fsTemp.getFileStoreUrl(token, size));

                PretreatInfo pretreatInfoTemp = new PretreatInfo();
                pretreatInfoTemp.copyNotNullProperty(pretreatInfo);
                pretreatInfoTemp.setIsIsUnzip(false);

                completedFileStoreAndPretreat(fsTemp, token, size, fileStoreInfoTemp, pretreatInfoTemp);

                FileSystemOpt.deleteFile(tempFilePath);
            }
        } catch (IOException ie){
            throw new ObjectException(ie);
        }
    }

    private JSONObject completedFileStoreAndPretreat(FileStore fs, String fileMd5, long size,
                                               FileStoreInfo fileInfo, PretreatInfo pretreatInfo)  {

        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileSize(size);
        fileInfo.setFileStorePath(fs.getFileStoreUrl(fileMd5, size));

        fileStoreInfoManager.saveNewObject(fileInfo);
        String fileId = fileInfo.getFileId();
        try {
            if (pretreatInfo.needPretreat()) {
                fileInfo = FilePretreatment.pretreatment(fs, fileInfo, pretreatInfo);
            }

            // 只有zip文件才需要解压
            if (pretreatInfo.getIsUnzip() && "zip".equals(fileInfo.getFileType())) {
                unzip(fs, fileInfo, pretreatInfo, fileInfo.getFileShowPath());
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }

        if(checkDuplicate){
            FileStoreInfo duplicateFile = fileStoreInfoManager.getDuplicateFile(fileInfo);
            if(duplicateFile != null){
                if("I".equals(duplicateFile.getIndexState())){
                    Indexer indexer = IndexerSearcherFactory.obtainIndexer(
                            IndexerSearcherFactory.loadESServerConfigFormProperties(
                                    SysParametersUtils.loadProperties()), FileDocument.class);
                    indexer.deleteDocument(
                            FileDocument.ES_DOCUMENT_TYPE, duplicateFile.getFileId());
                }
                fileStoreInfoManager.deleteFile(duplicateFile);
            }
        }

        if(keepSingleIndexByShowpath ){
            FileStoreInfo duplicateFile = fileStoreInfoManager.getDuplicateFileByShowPath(fileInfo);
            if(duplicateFile != null){
                if("I".equals(duplicateFile.getIndexState())){
                    Indexer indexer = IndexerSearcherFactory.obtainIndexer(
                            IndexerSearcherFactory.loadESServerConfigFormProperties(
                                    SysParametersUtils.loadProperties()), FileDocument.class);
                    indexer.deleteDocument(
                            FileDocument.ES_DOCUMENT_TYPE, duplicateFile.getFileId());
                }
            }
        }

        fileStoreInfoManager.updateObject(fileInfo);
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
        FileStore fs = FileStoreFactory.createDefaultFileStore();

        if (fs.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            Triple<FileStoreInfo, PretreatInfo, InputStream> formData
                    = fetchUploadFormFromRequest(request);
            completedFileStoreAndPretreat(fs, token, size, formData.getLeft(), formData.getMiddle(), response);
            return;
        } else
            JsonResultUtils.writeAjaxErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "文件不存在无法实现秒传，MD5：" + token, response);
    }

    /**
     * 续传文件（range） 如果文件已经传输完成 对文件进行保存
     * @param token token
     * @param size 大小
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/range", method = {RequestMethod.POST})
    public void uploadFileRange(
            String token, long size,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, NoSuchAlgorithmException {

        Triple<FileStoreInfo, PretreatInfo, InputStream> formData
                = fetchUploadFormFromRequest(request);
        //TODO 添加权限验证 : OSID + Token
        FileStore fs = FileStoreFactory.createDefaultFileStore();
        if (fs.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            completedFileStoreAndPretreat(fs, token, size, formData.getLeft(),
                    formData.getMiddle(), response);
            return;
        }

        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);

        try {
            long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, formData.getRight(), token, size, request);
            if(uploadSize==0){
                //上传到临时区成功
                fs. saveFile(tempFilePath, token, size);
                completedFileStoreAndPretreat(fs, token, size, formData.getLeft(),
                        formData.getMiddle(), response);
                FileSystemOpt.deleteFile(tempFilePath);
                return;
            }else if( uploadSize>0){
                JSONObject json = UploadDownloadUtils.makeRangeUploadJson(uploadSize);
                FileStoreInfo fileInfo = new FileStoreInfo();
                fileInfo.setFileSize(uploadSize);
                json.put(ResponseData.RES_DATA_FILED, fileInfo);
                JsonResultUtils.writeOriginalJson(json.toString(), response);
            }
        }catch (ObjectException e){
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeAjaxErrorMessage(e.getExceptionCode(),
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
        request.setCharacterEncoding("utf8");
        Triple<FileStoreInfo, PretreatInfo, InputStream> formData
                = fetchUploadFormFromRequest(request);
        String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
        try {
            int fileSize = FileIOOpt.writeInputStreamToFile(formData.getRight(), tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
            FileStore fs = FileStoreFactory.createDefaultFileStore();
            fs.saveFile(tempFilePath);
            completedFileStoreAndPretreat(fs, fileMd5, fileSize, formData.getLeft(), formData.getMiddle(), response);
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
            JsonResultUtils.writeAjaxErrorMessage(
                    FileServerConstant.ERROR_FILE_PRETREAT,
                    "文件上传成功，但是在保存前：" + e.getMessage(), response);
        }
    }


    private Pair<String, InputStream> fetchInputStreamFromRequest(HttpServletRequest request) throws IOException {
        String fileName = request.getParameter("name");
        if(StringUtils.isBlank(fileName))
            fileName = request.getParameter("fileName");
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
            return new ImmutablePair<String, InputStream>(fileName, request.getInputStream());

        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
//		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
            CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();
            FileItem fi = cMultipartFile.getFileItem();
            if (! fi.isFormField())  {
                fileName = fi.getName();
                fis = fi.getInputStream();
            }
        }
        return  new ImmutablePair<>(fileName, fis);
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
        Pair<String, InputStream> fileInfo = fetchInputStreamFromRequest(request);
        FileStore fs = FileStoreFactory.createDefaultFileStore();
        long tempFileSize = 0;
        // 如果文件已经存在则完成秒传，无需再传
        if (fs.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
            //添加完成 后 相关的处理  类似与 uploadRange
            completedStoreFile(fs, token, size, fileInfo.getLeft(), response);
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
        Pair<String, InputStream> fileInfo = fetchInputStreamFromRequest(request);
        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);

        FileStore fs = FileStoreFactory.createDefaultFileStore();
        if (fs.checkFile(token, size)) {// 如果文件已经存在则完成秒传，无需再传。
            completedStoreFile(fs, token, size, fileInfo.getLeft(), response);
            return;
        }

        try {
            long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, fileInfo.getRight(), token, size, request);
            if(uploadSize==0){
                completedStoreFile(fs, token, size, fileInfo.getLeft(), response);
                return;
            }else if( uploadSize>0){

                JsonResultUtils.writeOriginalJson(UploadDownloadUtils.
                        makeRangeUploadJson(uploadSize).toJSONString(), response);
            }

        }catch (ObjectException e){
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeAjaxErrorMessage(e.getExceptionCode(),
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
            Pair<String, InputStream> fileInfo = fetchInputStreamFromRequest(request);
            int fileSize = FileIOOpt.writeInputStreamToFile(fileInfo.getRight() , tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
            FileStore fs = FileStoreFactory.createDefaultFileStore();
            fs.saveFile(tempFilePath);
            completedStoreFile(fs, fileMd5, fileSize, fileInfo.getLeft(), response);
            FileSystemOpt.deleteFile(tempFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }
}