package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.framework.model.basedata.IUnitInfo;
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
import java.util.List;

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
    @ApiOperation(value = "查询用户拥有文件库列表")
    @WrapUpResponseBody
    public PageQueryResult<FileLibraryInfo> list(HttpServletRequest request) {
        String userCode = getUserCode(request);
        if (userCode == null) {
            return null;
        }
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoMag.listFileLibraryInfo(
            userCode);
        return PageQueryResult.createResult(fileLibraryInfos, null);
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

    @RequestMapping(value="/unitpath",method = RequestMethod.GET)
    @ApiOperation(value = "根据用户查询机构全路径")
    @WrapUpResponseBody
    public List<IUnitInfo> listUnitPathsByUserCode(HttpServletRequest request){
        String userCode = getUserCode(request);
        if (userCode == null) {
            return null;
        }
        return fileLibraryInfoMag.listUnitPathsByUserCode(userCode);
    }

    private String getUserCode(HttpServletRequest request) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        if (StringUtils.isBlank(userCode)) {
            userCode = (String) collectRequestParameters(request).get("userCode");
        }
        if (StringUtils.isBlank(userCode)) {
            return null;
        }
        return userCode;
    }
    @RequestMapping(value="/initpersonlib",method = {RequestMethod.POST})
    @ApiOperation(value = "初始化个人文件库")
    @WrapUpResponseBody
    public void initPersonLibrary(HttpServletRequest request){
        fileLibraryInfoMag.initPersonLibrary(WebOptUtils.getCurrentUserCode(request));
    }
    @RequestMapping(value="/initunitlib",method = {RequestMethod.POST})
    @ApiOperation(value = "初始化机构库")
    @WrapUpResponseBody
    public void initUnitLibrary(HttpServletRequest request){
        fileLibraryInfoMag.initUnitLibrary(WebOptUtils.getCurrentUnitCode(request),WebOptUtils.getCurrentUserCode(request));
    }
    /**
     * 新增 文件库信息
     *
     * @param fileLibraryInfo {@link FileLibraryInfo}
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件库信息")
    @WrapUpResponseBody
    public void createFileLibraryInfo(@RequestBody FileLibraryInfo fileLibraryInfo,HttpServletRequest request,HttpServletResponse response) {
        fileLibraryInfo.setCreateUser(WebOptUtils.getCurrentUserCode(request));
        if(fileLibraryInfo.getFileLibraryAccesss()!=null) {
            fileLibraryInfo.getFileLibraryAccesss().forEach(e -> e.setCreateUser(fileLibraryInfo.getCreateUser()));
        }
        fileLibraryInfoMag.createFileLibraryInfo(fileLibraryInfo);
        JsonResultUtils.writeSingleDataJson(fileLibraryInfo, response);
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
    public void updateFileLibraryInfo(@RequestBody FileLibraryInfo fileLibraryInfo,HttpServletRequest request,HttpServletResponse response) {
        fileLibraryInfo.setUpdateUser(WebOptUtils.getCurrentUserCode(request));
        fileLibraryInfoMag.updateFileLibraryInfo(fileLibraryInfo);
        JsonResultUtils.writeSingleDataJson(fileLibraryInfo, response);
    }

}
