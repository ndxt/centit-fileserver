package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.po.FileShowInfo;
import com.centit.fileserver.service.FileFolderInfoManager;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.common.ObjectException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final FileFolderInfoManager fileFolderInfoMag;
    private final LocalFileManager localFileManager;
    private final FileLibraryInfoManager fileLibraryInfoManager;

    public FileFolderInfoController(FileFolderInfoManager fileFolderInfoMag, LocalFileManager localFileManager, FileLibraryInfoManager fileLibraryInfoManager) {
        this.fileFolderInfoMag = fileFolderInfoMag;
        this.localFileManager = localFileManager;
        this.fileLibraryInfoManager = fileLibraryInfoManager;
    }

    @RequestMapping(value = "/prev/{folderId}", method = RequestMethod.GET)
    @ApiOperation(value = "查询文件夹所有上级文件夹接口")
    @WrapUpResponseBody
    public List<FileFolderInfo> list(@PathVariable String folderId, HttpServletRequest request) {
        FileFolderInfo fileFolderInfo = fileFolderInfoMag.getFileFolderInfo(folderId);
        String[] paths = StringUtils.split(fileFolderInfo.getFolderPath(), "/");
        List<FileFolderInfo> fileFolderInfos = new ArrayList<>();
        fileFolderInfos.add(fileFolderInfo);
        fileFolderInfos.add(getFileFolderInfo(fileLibraryInfoManager.getFileLibraryInfo(fileFolderInfo.getLibraryId())));
        for (String path : paths) {
            if (!"-1".equals(path)) {
                fileFolderInfos.add(fileFolderInfoMag.getFileFolderInfo(path));
            }
        }
        return fileFolderInfos;
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

    /**
     * 查询所有   文件夹信息  列表
     *
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
        List<FileShowInfo> fileShowInfos = localFileManager.listFolderFiles(searchColumn);
        for (FileFolderInfo fileFolderInfo : fileFolderInfos) {
            FileShowInfo fileShowInfo = fileFolderToFileShow(fileFolderInfo);
            fileShowInfos.add(fileShowInfo);
        }
        return fileShowInfos;
    }

    private FileShowInfo fileFolderToFileShow(FileFolderInfo fileFolderInfo) {
        FileShowInfo fileShowInfo = new FileShowInfo();
        fileShowInfo.setFileName(fileFolderInfo.getFolderName());
        fileShowInfo.setFileShowPath(fileFolderInfo.getFolderPath());
        fileShowInfo.setFolder(true);
        fileShowInfo.setFolderId(fileFolderInfo.getFolderId());
        fileShowInfo.setParentPath(fileFolderInfo.getParentFolder());
        fileShowInfo.setCreateFolder(fileFolderInfo.getIsCreateFolder());
        fileShowInfo.setUploadFile(fileFolderInfo.getIsUpload());
        fileShowInfo.setCreateTime(fileFolderInfo.getCreateTime());
        fileShowInfo.setOwnerName(CodeRepositoryUtil.getUserName(fileFolderInfo.getCreateUser()));
        return fileShowInfo;
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
     * @param fileFolderInfo {@link FileFolderInfo}
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件夹信息")
    @WrapUpResponseBody
    public void createFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request, HttpServletResponse response) {
        List<FileFolderInfo> fileFolderInfos = fileFolderInfoMag.listFileFolderInfo(CollectionsOpt.createHashMap("folderPath", fileFolderInfo.getFolderPath(),
            "folderName", fileFolderInfo.getFolderName(), "libraryId", fileFolderInfo.getLibraryId()), null);
        if (fileFolderInfos == null || fileFolderInfos.size() == 0) {
            fileFolderInfo.setCreateUser(WebOptUtils.getCurrentUserCode(request));
            fileFolderInfoMag.createFileFolderInfo(fileFolderInfo);
            JsonResultUtils.writeSingleDataJson(fileFolderInfo, response);
        } else {
            FileFolderInfo fileFolderInfo1 = fileFolderInfos.get(0);
            fileFolderInfo1.setMsg("100文件夹已存在");
            JsonResultUtils.writeSingleDataJson(fileFolderInfo1, response);
        }
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
     * @param fileFolderInfo {@link FileFolderInfo}
     */
    @RequestMapping(method = {RequestMethod.PUT})
    @ApiOperation(value = "更新文件夹信息")
    @WrapUpResponseBody
    public void updateFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request, HttpServletResponse response) {
        fileFolderInfo.setUpdateUser(WebOptUtils.getCurrentUserCode(request));
        JsonResultUtils.writeSingleDataJson(fileFolderInfoMag.updateFileFolderInfo(fileFolderInfo), response);
    }
}
