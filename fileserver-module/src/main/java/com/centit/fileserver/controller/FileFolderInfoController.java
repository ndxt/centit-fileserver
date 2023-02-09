package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileShowInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.*;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.*;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * FileFolderInfo  Controller.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件夹信息
 */


@Controller
@RequestMapping("/folder")
@Api(value = "FILE_FOLDER_INFO", tags = "文件夹信息")
public class FileFolderInfoController extends BaseController {

    private static final long MAX_ZIP_FILE_SIZE = 4 * 1024 * 1024 * 1024l;

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired
    private LocalFileManager localFileManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    @Autowired
    private FileFolderInfoManager fileFolderInfoMag;

    @Autowired
    private FileLibraryInfoManager fileLibraryInfoManager;


    @RequestMapping(value = "/prev/{folderId}", method = RequestMethod.GET)
    @ApiOperation(value = "查询文件夹所有上级文件夹接口")
    @WrapUpResponseBody
    public List<FileFolderInfo> list(@PathVariable String folderId) {
        FileFolderInfo fileFolderInfo = fileFolderInfoMag.getFileFolderInfo(folderId);
        String[] paths = StringUtils.split(fileFolderInfo.getFolderPath(), "/");
        List<FileFolderInfo> fileFolderInfos = new ArrayList<>();
        fileFolderInfos.add(fileFolderInfo);
        fileFolderInfos.add(getFileFolderInfo(fileLibraryInfoManager.getFileLibrary(fileFolderInfo.getLibraryId())));
        for (String path : paths) {
            if (!"-1".equals(path)) {
                fileFolderInfos.add(fileFolderInfoMag.getFileFolderInfo(path));
            }
        }
        return fileFolderInfos;
    }

    /**
     * 查询所有   文件夹信息  列表
     *
     * @param request   HttpServletRequest
     * @param folderId  String
     * @param libraryId String
     * @return {data:[]}
     */
    @RequestMapping(value = "/{libraryId}/{folderId}", method = RequestMethod.GET)
    @ApiOperation(value = "按库查询所有文件夹及文件夹信息列表")
    @WrapUpResponseBody
    public List<FileShowInfo> list(@PathVariable String libraryId, @PathVariable String folderId, HttpServletRequest request) {
        Map<String, Object> searchColumn = CollectionsOpt.createHashMap("libraryId", libraryId, "parentFolder", folderId);
        searchColumn.put("favoriteUser", WebOptUtils.getCurrentUserCode(request));
        List<FileFolderInfo> fileFolderInfos = fileFolderInfoMag.listFileFolderInfo(
            searchColumn, null);
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        boolean queryFile = !StringBaseOpt.isNvl(request.getParameter("fileName"));
        if (queryFile) {
            searchColumn.put("fileName", request.getParameter("fileName"));
        }
        List<FileShowInfo> fileShowInfos = localFileManager.listFolderFiles(topUnit, searchColumn);
        if (!queryFile) {
            for (FileFolderInfo fileFolderInfo : fileFolderInfos) {
                FileShowInfo fileShowInfo = fileFolderToFileShow(topUnit, fileFolderInfo);
                fileShowInfos.add(fileShowInfo);
            }
        }
        return fileShowInfos;
    }

    @RequestMapping(value = "/downloadZip/{folderId}", method = {RequestMethod.GET})
    @ApiOperation(value = "将文件夹打包成zip文件下载")
    public void downloadAsZipStream(@PathVariable String folderId, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        FileFolderInfo folderInfo = fileFolderInfoMag.getFileFolderInfo(folderId);
        response.setContentType(FileType.mapExtNameToMimeType("zip"));
        response.setHeader("Content-Disposition", "attachment; filename="
            + URLEncoder.encode(folderInfo.getFolderName(), "UTF-8") + ".zip");
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        ZipOutputStream out = ZipCompressor.convertToZipOutputStream(response.getOutputStream());
        addFolder(topUnit, out, "", folderId, 0);
        out.close();
        OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
            .user(WebOptUtils.getCurrentUserCode(request)).unit(folderInfo.getLibraryId())
            .method("文件夹打包下载").tag(folderId).time(DatetimeOpt.currentUtilDate()).content(folderInfo.getFolderName()));
    }

    @RequestMapping(value = "/zip/{folderId}", method = {RequestMethod.GET})
    @ApiOperation(value = "将文件夹打包成zip, 并返回临时文件的路径")
    @WrapUpResponseBody
    public String downloadAsZip(@PathVariable String folderId, HttpServletRequest request) throws UnsupportedEncodingException {
        FileFolderInfo folderInfo = fileFolderInfoMag.getFileFolderInfo(folderId);
        String tempFileId = UuidOpt.getUuidAsString32();
        String zipFile = SystemTempFileUtils.getTempFilePath(tempFileId);
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        compressFolder(topUnit, zipFile, folderId);
        OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
            .user(WebOptUtils.getCurrentUserCode(request)).unit(folderInfo.getLibraryId())
            .method("文件夹打包下载").tag(folderId).time(DatetimeOpt.currentUtilDate()).content(folderInfo.getFolderName()).newObject(zipFile));
        return tempFileId + "?name=" + URLEncoder.encode(folderInfo.getFolderName(), "UTF-8") + ".zip";
    }

    /**
     * 查询单个  文件夹信息
     *
     * @param folderId folder_id
     * @return {data:{}}
     */
    @RequestMapping(value = "/{folderId}", method = {RequestMethod.GET})
    @ApiOperation(value = "查询单个文件夹信息")
    @WrapUpResponseBody
    public FileFolderInfo getFileFolderInfo(@PathVariable String folderId) {
        return fileFolderInfoMag.getFileFolderInfo(folderId);
    }

    /**
     * 新增 文件夹信息
     *
     * @param request        HttpServletRequest
     * @param fileFolderInfo {@link FileFolderInfo}
     * @return FileFolderInfo
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件夹信息")
    @WrapUpResponseBody
    public FileFolderInfo createFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request) {
        if (StringBaseOpt.isNvl(fileFolderInfo.getLibraryId())) {
            throw new ObjectException("库id不能为空");
        }
        String[] folderNames = StringUtils.split(fileFolderInfo.getFolderName(), "/");
        String folderPath = fileFolderInfo.getFolderPath();
        String parentId = fileFolderInfo.getFolderId();
        String libraryId = fileFolderInfo.getLibraryId();
        String userCode = WebOptUtils.getCurrentUserCode(request);
        for (int i = 0; i < folderNames.length; i++) {
            List<FileFolderInfo> fileFolderInfos = fileFolderInfoMag.listFileFolderInfo(
                CollectionsOpt.createHashMap("folderPath", folderPath,
                    "folderName", folderNames[i], "libraryId", libraryId), null);
            if (fileFolderInfos == null || fileFolderInfos.size() == 0) {
                fileFolderInfo.setFolderId(null);
                fileFolderInfo.setFolderName(folderNames[i]);
                fileFolderInfo.setFolderPath(folderPath);
                fileFolderInfo.setParentFolder(parentId);
                fileFolderInfo.setLibraryId(libraryId);
                fileFolderInfo.setCreateUser(userCode);
                fileFolderInfoMag.createFileFolderInfo(fileFolderInfo);
            } else {
                fileFolderInfo = fileFolderInfos.get(0);
            }
            folderPath = folderPath + "/" + fileFolderInfo.getFolderId();
            parentId = fileFolderInfo.getFolderId();
            ;
        }
        return fileFolderInfo;
    }

    /**
     * 删除单个  文件夹信息
     *
     * @param folderId folder_id
     */
    @RequestMapping(value = "/{folderId}", method = {RequestMethod.DELETE})
    @ApiOperation(value = "删除单个文件夹信息")
    @WrapUpResponseBody
    public void deleteFileFolderInfo(@PathVariable String folderId) {
        fileFolderInfoMag.deleteFileFolderInfo(folderId);
    }

    /**
     * 新增或保存 文件夹信息
     *
     * @param request        HttpServletRequest
     * @param response       HttpServletResponse
     * @param fileFolderInfo {@link FileFolderInfo}
     */
    @RequestMapping(method = {RequestMethod.PUT})
    @ApiOperation(value = "更新文件夹信息")
    @WrapUpResponseBody
    public void updateFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request,
                                     HttpServletResponse response) {
        fileFolderInfo.setUpdateUser(WebOptUtils.getCurrentUserCode(request));
        JsonResultUtils.writeSingleDataJson(fileFolderInfoMag.updateFileFolderInfo(fileFolderInfo), response);
    }

    private FileFolderInfo getFileFolderInfo(FileLibraryInfo fileLibraryInfo) {
        FileFolderInfo fileFolderInfo1 = new FileFolderInfo();
        fileFolderInfo1.setFolderName(fileLibraryInfo.getLibraryName());
        fileFolderInfo1.setFolderId(fileLibraryInfo.getLibraryId());
        fileFolderInfo1.setIsCreateFolder(fileLibraryInfo.getIsCreateFolder());
        fileFolderInfo1.setIsUpload(fileLibraryInfo.getIsUpload());
        fileFolderInfo1.setFolderPath("/");
        fileFolderInfo1.setParentFolder("0");
        return fileFolderInfo1;
    }

    private FileShowInfo fileFolderToFileShow(String topUnit, FileFolderInfo fileFolderInfo) {
        FileShowInfo fileShowInfo = new FileShowInfo();
        fileShowInfo.setFileName(fileFolderInfo.getFolderName());
        fileShowInfo.setFileShowPath(fileFolderInfo.getFolderPath());
        fileShowInfo.setFolder(true);
        fileShowInfo.setFolderId(fileFolderInfo.getFolderId());
        fileShowInfo.setParentPath(fileFolderInfo.getParentFolder());
        fileShowInfo.setCreateFolder(fileFolderInfo.getIsCreateFolder());
        fileShowInfo.setUploadFile(fileFolderInfo.getIsUpload());
        fileShowInfo.setCreateTime(fileFolderInfo.getCreateTime());
        fileShowInfo.setOwnerName(CodeRepositoryUtil.getUserName(topUnit, fileFolderInfo.getCreateUser()));
        return fileShowInfo;
    }

    private void addFolder(String topUnit, ZipOutputStream out, String basedir, String folderId, long currSize) throws IOException {
        List<FileShowInfo> fileList =
            localFileManager.listFolderFiles(topUnit,
                CollectionsOpt.createHashMap("parentFolder", folderId));
        if (fileList != null) {
            for (FileShowInfo file : fileList) {
                FileInfo fi = fileInfoManager.getObjectById(file.getAccessToken());
                FileStoreInfo fsi = fileStoreInfoManager.getObjectById(fi.getFileMd5());
                InputStream inputStream = fileStore.loadFileStream(fsi.getFileStorePath());
                if (inputStream == null) {
                    String tempFile = SystemTempFileUtils.getTempFilePath(fsi.getFileMd5(), fsi.getFileSize());
                    if (FileSystemOpt.existFile(tempFile)) {
                        inputStream = new FileInputStream(new File(tempFile));
                    }
                }
                if (inputStream != null) {
                    ZipCompressor.compressFile(inputStream
                        , fi.getFileName(), out, basedir);
                }

                if (currSize >= 0) {
                    currSize += fsi.getFileSize();
                    if (currSize > MAX_ZIP_FILE_SIZE) {
                        throw new ObjectException("zip文件大小超过约定的最大值！");
                    }
                }
            }
        }
        List<FileFolderInfo> fileFolderInfos = fileFolderInfoMag.listFileFolderInfo(
            CollectionsOpt.createHashMap("parentFolder", folderId), null);
        if (fileFolderInfos != null) {
            for (FileFolderInfo fileFolderInfo : fileFolderInfos) {
                addFolder(topUnit, out, basedir + fileFolderInfo.getFolderName() + "/", fileFolderInfo.getFolderId(), currSize);
            }
        }
    }

    private void compressFolder(String topUnit, String zipFilePathName, String folderId) {
        try {
            File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            /*CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());*/
            ZipOutputStream out = ZipCompressor.convertToZipOutputStream(fileOutputStream);
            addFolder(topUnit, out, "", folderId, 0);
            out.close();
        } catch (Exception e) {
            throw new ObjectException(e);
        }
    }
}
