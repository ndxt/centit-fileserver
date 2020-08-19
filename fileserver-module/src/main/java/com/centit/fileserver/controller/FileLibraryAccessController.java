package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileLibraryAccess;
import com.centit.fileserver.service.FileLibraryAccessManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
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
 * FileLibraryAccess  Controller.
 * create by scaffold 2020-08-18 13:38:15
 * @author codefan@sina.com
 * 项目库授权信息
*/


@Controller
@RequestMapping("/libraryaccess")
@Api(value = "FILE_LIBRARY_ACCESS", tags = "项目库授权信息")
public class FileLibraryAccessController  extends BaseController {

	private final FileLibraryAccessManager fileLibraryAccessMag;

    public FileLibraryAccessController(FileLibraryAccessManager fileLibraryAccessMag) {
        this.fileLibraryAccessMag = fileLibraryAccessMag;
    }


    /**
     * 查询所有   项目库授权信息  列表
     * @return {data:[]}
     */
    @RequestMapping(method = RequestMethod.GET)
	@ApiOperation(value = "查询所有项目库授权信息列表")
	@WrapUpResponseBody
    public PageQueryResult<FileLibraryAccess> list(HttpServletRequest request, PageDesc pageDesc) {
        Map<String, Object> searchColumn = collectRequestParameters(request);
        List<FileLibraryAccess> fileLibraryAccesss = fileLibraryAccessMag.listFileLibraryAccess(
            searchColumn, pageDesc);
		return PageQueryResult.createResult(fileLibraryAccesss,pageDesc);
    }

    /**
     * 查询单个  项目库授权信息

	 * @param accessId  access_id
     * @return {data:{}}
     */
    @RequestMapping(value = "/{accessId}", method = {RequestMethod.GET})
	@ApiOperation(value = "查询单个项目库授权信息")
	@WrapUpResponseBody
	public FileLibraryAccess getFileLibraryAccess(@PathVariable String accessId) {

    	return fileLibraryAccessMag.getFileLibraryAccess( accessId);
    }

    /**
     * 新增 项目库授权信息
     *
     * @param fileLibraryAccess  {@link FileLibraryAccess}
     */
    @RequestMapping(method = {RequestMethod.POST})
	@ApiOperation(value = "新增项目库授权信息")
	@WrapUpResponseBody
    public void createFileLibraryAccess(@RequestBody FileLibraryAccess fileLibraryAccess,HttpServletRequest request, HttpServletResponse response) {
    	fileLibraryAccess.setCreateUser(WebOptUtils.getCurrentUserCode(request));
        fileLibraryAccessMag.createFileLibraryAccess(fileLibraryAccess);
        JsonResultUtils.writeSingleDataJson(fileLibraryAccess,response);
    }

    /**
     * 删除单个  项目库授权信息

	 * @param accessId  access_id
     */
    @RequestMapping(value = "/{accessId}", method = {RequestMethod.DELETE})
	@ApiOperation(value = "删除单个项目库授权信息")
	@WrapUpResponseBody
	public void deleteFileLibraryAccess(@PathVariable String accessId) {
    	fileLibraryAccessMag.deleteFileLibraryAccess( accessId);
    }

    /**
     * 更新 项目库授权信息

	 * @param fileLibraryAccess  {@link FileLibraryAccess}
     */
    @RequestMapping( method = {RequestMethod.PUT})
	@ApiOperation(value = "更新项目库授权信息")
	@WrapUpResponseBody
    public void updateFileLibraryAccess(@RequestBody FileLibraryAccess fileLibraryAccess) {
		fileLibraryAccessMag.updateFileLibraryAccess(fileLibraryAccess);
    }
}
