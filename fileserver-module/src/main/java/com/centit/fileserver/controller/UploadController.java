package com.centit.fileserver.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.dao.FileFolderInfoDao;
import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.FileUploadAuthorizedManager;
import com.centit.fileserver.task.DocumentIndexOpt;
import com.centit.fileserver.task.FileOptTaskExecutor;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.image.SvgUtils;
import com.centit.support.office.DocOptUtil;
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
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/upload")
@Api(value = "文件断点上传，并且保存文件信息接口", tags = "文件断点上传，并且保存文件信息接口")
public class UploadController extends BaseController {
    public static final String UPLOAD_FILE_TOKEN_NAME = "uploadToken";
    protected Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${fileserver.index.keepsingle.showpath:true}")
    protected boolean keepSingleIndexByShowpath;

    @Value("${fileserver.check.upload.token:false}")
    protected boolean checkUploadToken;
    //是否同步处理，默认同步处理
    @Value("${fileserver.pretreatment.sync:true}")
    protected boolean pretreatmentAsSync;

    @Value("${fileserver.run-as-springboot:false}")
    protected static boolean runAsSpringBoot;

    @Autowired
    protected FileStore fileStore;

   /* @Autowired
    private FileTaskQueue fileOptTaskQueue;*/

    @Autowired
    protected FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    private FileUploadAuthorizedManager fileUploadAuthorizedManager;

    @Autowired
    FileOptTaskExecutor fileOptTaskExecutor;

    @Autowired
    FileFolderInfoDao fileFolderInfoDao;

    @Autowired
    DocumentIndexOpt documentIndexOpt;

    /**
     * 判断文件是否存在，如果文件已经存在可以实现秒传
     *
     * @param request fileStoreUrl token size
     * @return 是否存在
     */
    @ApiOperation(value = "检查文件是否存在")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400,
        allowedHeaders = "*", methods = RequestMethod.GET)
    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    @WrapUpResponseBody
    public boolean checkFileExists(HttpServletRequest request) {

        String fileStoreUrl = request.getParameter("fileStoreUrl");
        if (StringUtils.isNotBlank(fileStoreUrl)) {
            return fileStore.checkFile(fileStoreUrl);
        }

        String fileId = request.getParameter("fileId");
        if (StringUtils.isNotBlank(fileId)) {
            FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
            if (fileInfo == null) {
                return false;
            }
            FileStoreInfo storeInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            if (storeInfo == null) {
                return false;
            }
            return fileStore.checkFile(storeInfo.getFileStorePath());
        }

        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        Long fileSize = NumberBaseOpt.parseLong(
            WebOptUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);
        return fileStore.checkFile(fileStore.matchFileStoreUrl(fileInfo, fileSize));
    }

    /**
     * 获取文件 断点位置，前端根据断点位置续传
     *
     * @param request token size
     * @return 文件信息
     */
    @ApiOperation(value = "检查续传点，如果signal为continue请续传，如果为secondpass表示文件已存在需要调用秒传接口")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.GET)
    @RequestMapping(value = "/range", method = {RequestMethod.GET})
    @WrapUpResponseBody
    public JSONObject checkFileRange(HttpServletRequest request) {
        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        Long fileSize = NumberBaseOpt.parseLong(
            WebOptUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);
        return UploadDownloadUtils.checkFileRange(fileStore, fileInfo, fileSize);
    }

    @RequestMapping(value = "/indexSyncFile", method = RequestMethod.POST)
    @ApiOperation(value = "处理未转储文件")
    @WrapUpResponseBody
    public JSONArray addSaveFileOpt(HttpServletRequest request) {
        JSONArray jsonArray = fileInfoManager.listStoredFiles(CollectionsOpt.createHashMap("isTemp", "D"), null);
        for (Object o : jsonArray) {
            FileInfo fileInfo = JSON.to(FileInfo.class , o);
            documentIndexOpt.doFileIndex(fileInfo, fileInfo.getFileSize());
        }
        return jsonArray;
    }

    /**
     * 完成秒传，如果文件不存在会返回失败
     *
     * @param token    token
     *  size/fileSize     大小
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件秒传接口，需要post文件基本信息和预处理信息")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/secondpass", method = RequestMethod.POST)
    public void secondPass(String token,
                           HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        request.setCharacterEncoding("utf8");
        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        Long fileSize = NumberBaseOpt.parseLong(
            WebOptUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);

        fileInfo.setFileMd5(token);
        fileInfo.setFileSize(fileSize);
        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, fileSize);
        String fileStorePath = fileStore.matchFileStoreUrl(fileInfo, fileSize);
        if (fileStore.checkFile(fileStorePath)) {
            Triple<FileInfo, Map<String, Object>, InputStream> formData
                = fetchUploadFormFromRequest(request);
            //用于其他的预处理操作
            FileIOOpt.writeInputStreamToFile(fileStore.loadFileStream(fileStorePath), new File(tempFilePath));
            completedFileStoreAndPretreat(tempFilePath, fileInfo,
                formData.getMiddle(), request, response);
        } else {
            //临时文件大小相等 说明上传已完成，也可以秒传
            long tempFileSize = SystemTempFileUtils.checkTempFileSize(tempFilePath);
            if (tempFileSize == fileSize) {
                Triple<FileInfo, Map<String, Object>, InputStream> formData
                    = fetchUploadFormFromRequest(request);
                completedFileStoreAndPretreat(tempFilePath, fileInfo,
                    formData.getMiddle(), request, response);
            } else {
                FileSystemOpt.deleteFile(tempFilePath);
                JsonResultUtils.writeErrorMessageJson(
                    FileServerConstant.ERROR_FILE_SIZE_ERROR,
                    "文件大小不一致。\r\n MD5(uploadedSize - fileSize)："
                        + token + "(" + tempFileSize + "-" + fileSize + ")", response);
            }
        }
    }

    /**
     * 续传文件（range） 如果文件已经传输完成 对文件进行保存
     *
     * @param token    token
     * @param size     大小
     * @param request  HttpServletRequest
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
        if (checkUploadToken && uploadIsForbidden(request, response)) {
            return;
        }
        Triple<FileInfo, Map<String, Object>, InputStream> formData
            = fetchUploadFormFromRequest(request);
        FileInfo fileInfo = formData.getLeft();
        fileInfo.setFileMd5(token);
        fileInfo.setFileSize(size);
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);
        if (fileStore.checkFile(fileStore.matchFileStoreUrl(formData.getLeft(), size))) {
            completedFileStoreAndPretreat(tempFilePath, fileInfo,
                formData.getMiddle(), request, response);
            return;
        }

        long uploadSize = UploadDownloadUtils.uploadRange(tempFilePath, formData.getRight(), token, size, request);
        if (uploadSize == size) {
            completedFileStoreAndPretreat(tempFilePath, fileInfo,
                formData.getMiddle(), request, response);

        } else {
            JSONObject json = UploadDownloadUtils.makeRangeUploadJson(uploadSize, token, token + "_" + size);
            JsonResultUtils.writeOriginalJson(json.toString(), response);
        }
        OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
            .unit(formData.getLeft().getLibraryId())
            .topUnit(WebOptUtils.getCurrentTopUnit(request))
            .correlation(WebOptUtils.getCorrelationId(request))
            .loginIp(WebOptUtils.getRequestAddr(request))
            .user(WebOptUtils.getCurrentUserCode(request))
            .method("上传").tag(formData.getLeft().getFileId())
            .content(formData.getLeft().getFileName())
            .newObject(formData.getLeft()));
    }

    /**
     * 上传整个文件适用于IE8
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件整体上传结构，适用于IE8")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = {"/file", "/upload"}, method = RequestMethod.POST)
    public void uploadFile(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        if (checkUploadToken && uploadIsForbidden(request, response)) {
            return;
        }
        request.setCharacterEncoding("utf8");

        Triple<FileInfo, Map<String, Object>, InputStream> formData = fetchUploadFormFromRequest(request);
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String token = formData.getLeft().getFileMd5();
        boolean needCheck = !StringUtils.isBlank(token);
        Long size = NumberBaseOpt.parseLong(
            request.getParameter("size"), -1l);
        if (size < 1) {
            size = NumberBaseOpt.parseLong(
                request.getParameter("fileSize"), -1l);
        }
        String tempFilePath = needCheck ?
        SystemTempFileUtils.getTempFilePath(token, size) :
        SystemTempFileUtils.getRandomTempFilePath();

        if (FileSystemOpt.existFile(tempFilePath)) {
            FileSystemOpt.deleteFile(tempFilePath);
        }
        int fileSize = FileIOOpt.writeInputStreamToFile(formData.getRight(), tempFilePath);
        File tempFile = new File(tempFilePath);
        String fileMd5 = FileMD5Maker.makeFileMD5(tempFile);

        boolean isValid = fileSize != 0;
        if (needCheck) {
            isValid = size == (long) fileSize && token.equals(fileMd5);
        } else {
            tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
            tempFile.renameTo(new File(tempFilePath));
        }

        if (isValid && !StringUtils.isBlank(formData.getLeft().getFileName())) {
            FileInfo fileInfo = formData.getLeft();
            fileInfo.setFileMd5(fileMd5);
            String fileName = fileInfo.getFileName();
            if (!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(fileName))) {
                fileName = new String(fileName.getBytes("iso-8859-1"), "utf-8");
            }
            fileInfo.setFileName(fileName);
            fileInfo.setFileSize(fileSize);
            completedFileStoreAndPretreat(tempFilePath,
                fileInfo, formData.getMiddle(), request, response);
        } else {
            FileSystemOpt.deleteFile(tempFilePath);
            JsonResultUtils.writeErrorMessageJson("文件上传出错，fileName参数必须传，如果传了token和size参数请检查是否正确，并确认选择的文件！", response);
        }

    }

    private InputStream fetchISFromCommonsResolver(HttpServletRequest request, FileInfo fileInfo, Map<String, Object> pretreatInfo) throws IOException {
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
                if (StringUtils.isBlank(fileInfo.getFileName()) && StringUtils.isNotBlank(fn)) {
                    fileInfo.setFileName(fn);
                }
                fis = fi.getInputStream();
            }
        }
        return fis;
    }

    private FileInfo fetchFileInfoFromRequest(HttpServletRequest request) {

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(request.getParameter("fileId"));
        fileInfo.setFileCatalog(request.getParameter("fileCatalog"));
        fileInfo.setFileMd5(WebOptUtils
            .getRequestFirstOneParameter(request, "fileMd5", "token"));
        fileInfo.setFileName(WebOptUtils
            .getRequestFirstOneParameter(request, "name", "fileName"));
        String fileState = request.getParameter("fileState");
        if (StringUtils.isNotBlank(fileState)) {
            fileInfo.setFileState(fileState);
        }
        fileInfo.setOsId(request.getParameter("osId"));//*
        fileInfo.setOptId(request.getParameter("optId"));
        fileInfo.setOptMethod(request.getParameter("optMethod"));
        fileInfo.setOptTag(request.getParameter("optTag"));
        //这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
        fileInfo.setFileOwner(WebOptUtils.getCurrentUserCode(request));
        String fileUnit = request.getParameter("fileUnit");
        if (StringUtils.isBlank(fileUnit)) {
            fileUnit = WebOptUtils.getCurrentTopUnit(request);
        }
        fileInfo.setFileUnit(fileUnit);
        fileInfo.setFileDesc(request.getParameter("fileDesc"));
        fileInfo.setLibraryId(request.getParameter("libraryId"));
        fileInfo.setCreateTime(DatetimeOpt.currentUtilDate());
        fileInfo.setFileShowPath(WebOptUtils
            .getRequestFirstOneParameter(request, "filePath", "fileShowPath"));
        String rootFolderId = "-1";
        if (!StringUtils.isBlank(fileInfo.getLibraryId()) && StringUtils.isBlank(fileInfo.getFileShowPath())) {
            if (FileInfo.FILE_CATALOG_APPLICATION.equals(fileInfo.getFileCatalog())) {
                fileInfo.setFileShowPath(FileInfo.FOLDER_DEFAULT_BREAK + rootFolderId);
            } else if (FileInfo.FILE_CATALOG_MODEL.equals(fileInfo.getFileCatalog())) {
                String resourceFolderId = getFolderIdByFolderName(fileInfo.getLibraryId(), rootFolderId, FileInfo.FOLDER_RESOURCES_NAME);
                String path = FileInfo.FOLDER_DEFAULT_BREAK + rootFolderId + FileInfo.FOLDER_DEFAULT_BREAK + resourceFolderId;
                fileInfo.setFileShowPath(path);
            } else if (FileInfo.FILE_CATALOG_RUN.equals(fileInfo.getFileCatalog())) {
                String attachmentFolderId = getFolderIdByFolderName(fileInfo.getLibraryId(), rootFolderId, FileInfo.FOLDER_ATTACHMENTS_NAME);
                String dateFolderName = DatetimeOpt.convertDateToString(new Date(), "yyyy-MM");
                String dateFolderId = getFolderIdByFolderName(fileInfo.getLibraryId(), attachmentFolderId, dateFolderName);
                String path = FileInfo.FOLDER_DEFAULT_BREAK + rootFolderId + FileInfo.FOLDER_DEFAULT_BREAK + attachmentFolderId + FileInfo.FOLDER_DEFAULT_BREAK + dateFolderId;
                fileInfo.setFileShowPath(path);
            }
        }
        if (StringUtils.isBlank(fileInfo.getFileShowPath())) {
            fileInfo.setFileShowPath(FileInfo.FOLDER_DEFAULT_BREAK + rootFolderId);
        }
        return fileInfo;
    }

    private String getFolderIdByFolderName(String libraryId, String parentFolder, String folderName) {
        List<FileFolderInfo> fileFolderInfos = fileFolderInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("libraryId", libraryId, "parentFolder", parentFolder, "folderName", folderName));
        if (fileFolderInfos != null && fileFolderInfos.size() > 0) {
            return fileFolderInfos.get(0).getFolderId();
        } else {
            FileFolderInfo fileFolderInfo = new FileFolderInfo();
            fileFolderInfo.setLibraryId(libraryId);
            fileFolderInfo.setFolderName(folderName);
            fileFolderInfo.setParentFolder(parentFolder);
            fileFolderInfoDao.saveNewObject(fileFolderInfo);
            return fileFolderInfo.getFolderId();
        }
    }

    private static Map<String, Object> fetchPretreatInfoFromRequest(HttpServletRequest request) {
        Map<String, Object> pretreatInfo = collectRequestParameters(request);
        pretreatInfo.put("fileMd5",
            WebOptUtils.getRequestFirstOneParameter(request, "fileMd5", "token"));
        Long fileSize = NumberBaseOpt.parseLong(
            WebOptUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);
        pretreatInfo.put("fileSize", fileSize);
        return pretreatInfo;
    }

    private InputStream fetchISFromStandardResolver(HttpServletRequest request, FileInfo fileInfo, Map<String, Object> pretreatInfo) throws IOException {
        MultipartResolver resolver = new StandardServletMultipartResolver();
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
            MultipartFile cMultipartFile = entry.getValue();
            org.springframework.core.io.Resource resource = cMultipartFile.getResource();
            if (resource.isFile()) {
                String fileName = resource.getFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    fileInfo.setFileName(fileName);
                }
                fis = cMultipartFile.getInputStream();
            } else {
                String resourceName = resource.getFilename();
                if ("fileInfo".equals(resourceName)) {
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

    private Triple<FileInfo, Map<String, Object>, InputStream>
    fetchUploadFormFromRequest(HttpServletRequest request) throws IOException {
        FileInfo fileInfo = fetchFileInfoFromRequest(request);
        Map<String, Object> pretreatInfo = fetchPretreatInfoFromRequest(request);
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new ImmutableTriple<>(fileInfo, pretreatInfo, request.getInputStream());
        }
        InputStream fis = runAsSpringBoot ? fetchISFromStandardResolver(request, fileInfo, pretreatInfo)
            : fetchISFromCommonsResolver(request, fileInfo, pretreatInfo);
        return new ImmutableTriple<>(fileInfo, pretreatInfo, fis);
    }

    /**
     * 处理文件信息 并按照指令对文件进行加工
     * param fs 文件的物理存储接口
     *
     * @param tempFilePath 临时文件路径
     * @param fileInfo     文件对象
     * @param pretreatInfo PretreatInfo对象
     * @param response     HttpServletResponse
     */
    private void completedFileStoreAndPretreat(String tempFilePath,
                                               FileInfo fileInfo, Map<String, Object> pretreatInfo,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        try {
            if (checkUploadToken) {
                String uploadToken = request.getParameter(UPLOAD_FILE_TOKEN_NAME);
                fileUploadAuthorizedManager.consumeAuthorization(uploadToken);
            }
            JSONObject json = storeAndPretreatFile(tempFilePath, fileInfo, pretreatInfo);
            JsonResultUtils.writeOriginalJson(json.toString(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson(
                FileServerConstant.ERROR_FILE_PRETREAT,
                "文件上传成功，但是在保存前：" +
                    ObjectException.extortExceptionMessage(e), response);
        }
    }

    private JSONObject storeAndPretreatFile(String tempFilePath,
                                            FileInfo fileInfo, Map<String, Object> pretreatInfo) {
        //fileInfo.setFileMd5(fileMd5);
        boolean isUpdateFile = false;
        if (StringUtils.isBlank(fileInfo.getFileId())) {
            fileInfo.setFileId(UuidOpt.getUuidAsString());
        } else {
            FileInfo dbFile = fileInfoManager.getObjectById(fileInfo.getFileId());
            isUpdateFile = dbFile!=null;
        }
        if(StringUtils.isBlank(fileInfo.getOsId())) {
            fileInfo.setOsId("NOTSET");
        }
        if(StringUtils.isBlank(fileInfo.getOptId())) {
            fileInfo.setOptId("NOTSET");
        }
        String retMsg = "文件上传成功！";
        if(FileIOUtils.hasSensitiveExtName(fileInfo.getFileName())){
            fileInfo.setFileName(fileInfo.getFileName()+".rn");
            retMsg = "文件上传成功,但是因为文件名敏感已被重命名为"+fileInfo.getFileName();
        }
        //pdf svg 去除脚本
        if(StringUtils.endsWithIgnoreCase(fileInfo.getFileName(), ".svg") ) {
            SvgUtils.removeSvgJSAction(tempFilePath, tempFilePath);
            retMsg = "文件上传成功！但SVG文件中有Script脚本，已经被移除";
        } else if(StringUtils.endsWithIgnoreCase(fileInfo.getFileName(), ".pdf") ) {
            if(DocOptUtil.pdfContainsJSAction(tempFilePath)){
                fileInfo.setFileName(fileInfo.getFileName()+".rn");
                retMsg = "PDF文件包含JavaScript代码为避免XSS漏洞文件已被重命名为"+fileInfo.getFileName();
            }
        }

        boolean needSave = false;
        String fileId = fileInfo.getFileId();
        if(! isUpdateFile){
            FileInfo dbFile = fileInfoManager.getDuplicateFile(fileInfo);
            if (dbFile == null) {
                fileInfoManager.saveNewFile(fileInfo);
                needSave = true;
            } else {
                fileId = dbFile.getFileId();
            }
        } else {
            fileInfoManager.updateObject(fileInfo);
            needSave = true;
        }

        if (needSave) {
            try {
                // 先保存一个 临时文件； 如果文件已经存在是不会保存的
                if(!fileStore.checkFile(tempFilePath)){
                    fileInfoManager.deleteObject(fileInfo);
                    throw new ObjectException(ObjectException.UNKNOWN_EXCEPTION,
                        "找不到临时文件，请重新上传！");
                }
                fileStoreInfoManager.saveTempFileInfo(fileInfo, tempFilePath, fileInfo.getFileSize());
                if (pretreatmentAsSync)
                    fileOptTaskExecutor.runOptTask(fileInfo, fileInfo.getFileSize(), pretreatInfo);
                else
                    fileOptTaskExecutor.addOptTask(fileInfo, fileInfo.getFileSize(), pretreatInfo);
            } catch (Exception e) {
                throw new ObjectException(ObjectException.UNKNOWN_EXCEPTION, e.getMessage(), e);
                //logger.error(e.getMessage(), e);
            }
            return UploadDownloadUtils.makeRangeUploadCompleteJson(
                fileInfo.getFileMd5(), fileInfo.getFileSize(), fileInfo.getFileName(), fileId, retMsg);
        } else {
            return UploadDownloadUtils.makeRangeUploadCompleteJson(
                fileInfo.getFileMd5(), fileInfo.getFileSize(), fileInfo.getFileName(), fileId, retMsg);
        }

    }

    private boolean uploadIsForbidden(HttpServletRequest request, HttpServletResponse response) {

        String uploadToken = request.getParameter(UPLOAD_FILE_TOKEN_NAME);
        if (fileUploadAuthorizedManager.checkAuthorization(uploadToken) < 1) {
            JsonResultUtils.writeHttpErrorMessage(
                FileServerConstant.ERROR_FILE_FORBIDDEN,
                "没有权限上传文件,请检查参数:" + UPLOAD_FILE_TOKEN_NAME, response);
            return true;
        }
        return false;
    }
}
