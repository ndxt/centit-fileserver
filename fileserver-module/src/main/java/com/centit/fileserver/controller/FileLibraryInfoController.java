package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * FileLibraryInfo  Controller.
 * create by scaffold 2020-08-18 13:38:13
 *
 * @author codefan@sina.com
 * 文件库信息
 */


@Controller
@RequestMapping("library")
@Api(value = "FILE_LIBRARY_INFO", tags = "文件库信息")
public class FileLibraryInfoController extends BaseController {

    private final FileLibraryInfoManager fileLibraryInfoMag;

    public FileLibraryInfoController(FileLibraryInfoManager fileLibraryInfoMag) {
        this.fileLibraryInfoMag = fileLibraryInfoMag;
    }


    /**
     * 查询所有   文件库信息  列表
     *
     * @return {data:[]}
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "查询所有文件库信息列表")
    @WrapUpResponseBody
    public PageQueryResult<FileLibraryInfo> list(HttpServletRequest request, PageDesc pageDesc) {
        Map<String, Object> searchColumn = collectRequestParameters(request);
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoMag.listFileLibraryInfo(
            searchColumn, pageDesc);
        return PageQueryResult.createResult(fileLibraryInfos, pageDesc);
    }

    /**
     * 查询单个  文件库信息
     *
     * @param libraryId library_id
     * @return {data:{}}
     */
    @RequestMapping(value = "/{libraryId}", method = {RequestMethod.GET})
    @ApiOperation(value = "查询单个文件库信息")
    @WrapUpResponseBody
    public FileLibraryInfo getFileLibraryInfo(@PathVariable String libraryId) {

        return fileLibraryInfoMag.getFileLibraryInfo(libraryId);
    }

    /**
     * 新增 文件库信息
     *
     * @param fileLibraryInfo {@link FileLibraryInfo}
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件库信息")
    @WrapUpResponseBody
    public void createFileLibraryInfo(@RequestBody FileLibraryInfo fileLibraryInfo, HttpServletResponse response) {
        fileLibraryInfoMag.createFileLibraryInfo(fileLibraryInfo);
        JsonResultUtils.writeSingleDataJson(fileLibraryInfo.getLibraryId(), response);
    }

    /**
     * 删除单个  文件库信息
     *
     * @param libraryId library_id
     */
    @RequestMapping(value = "/{libraryId}", method = {RequestMethod.DELETE})
    @ApiOperation(value = "删除单个文件库信息")
    @WrapUpResponseBody
    public void deleteFileLibraryInfo(@PathVariable String libraryId) {

        fileLibraryInfoMag.deleteFileLibraryInfo(libraryId);


    }

    /**
     * 更新 文件库信息
     *
     * @param fileLibraryInfo {@link FileLibraryInfo}
     */
    @RequestMapping(method = {RequestMethod.PUT})
    @ApiOperation(value = "更新文件库信息")
    @WrapUpResponseBody
    public void updateFileLibraryInfo(@RequestBody FileLibraryInfo fileLibraryInfo) {
        fileLibraryInfoMag.updateFileLibraryInfo(fileLibraryInfo);
    }
}
