package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileFolderInfoManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * FileFolderInfo  Controller.
 * create by scaffold 2020-08-18 13:38:14
 * @author codefan@sina.com
 * 文件夹信息
*/


@Controller
@RequestMapping("/folder")
@Api(value = "FILE_FOLDER_INFO", tags = "文件夹信息")
public class FileFolderInfoController  extends BaseController {

	private final FileFolderInfoManager fileFolderInfoMag;
	private final LocalFileManager localFileManager;

    public FileFolderInfoController(FileFolderInfoManager fileFolderInfoMag,LocalFileManager localFileManager) {
        this.fileFolderInfoMag = fileFolderInfoMag;
        this.localFileManager=localFileManager;
    }


    /**
     * 查询所有   文件夹信息  列表
     * @return {data:[]}
     */
    @RequestMapping(value = "/{libraryId}/{folderId}",method = RequestMethod.GET)
	@ApiOperation(value = "按库查询所有文件夹及文件夹信息列表")
	@WrapUpResponseBody
    public PageQueryResult<Map<String, Object>> list(@PathVariable String libraryId, @PathVariable String folderId,HttpServletRequest request) {
        Map<String, Object> searchColumn = CollectionsOpt.createHashMap("libraryId",libraryId,"parentFolder",folderId);
        searchColumn.put("favoriteUser",WebOptUtils.getCurrentUserCode(request));
        List<Map<String, Object>> result=new ArrayList<>();
        result.add(CollectionsOpt.createHashMap("folders", fileFolderInfoMag.listFileFolderInfo(
            searchColumn, null)));
        result.add(CollectionsOpt.createHashMap("fileShowInfos", localFileManager.listFolderFiles(searchColumn)));
		return PageQueryResult.createResult(result,null);
    }

    /**
     * 查询单个  文件夹信息

	 * @param folderId  folder_id
     * @return {data:{}}
     */
    @RequestMapping(value = "/{folderId}", method = {RequestMethod.GET})
	@ApiOperation(value = "查询单个文件夹信息")
	@WrapUpResponseBody
	public FileFolderInfo getFileFolderInfo(@PathVariable String folderId) {
        return  fileFolderInfoMag.getFileFolderInfo( folderId);
    }

    /**
     * 新增 文件夹信息
     *
     * @param fileFolderInfo  {@link FileFolderInfo}
     */
    @RequestMapping(method = {RequestMethod.POST})
	@ApiOperation(value = "新增文件夹信息")
	@WrapUpResponseBody
    public void createFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request , HttpServletResponse response) {
        fileFolderInfo.setCreateUser(WebOptUtils.getCurrentUserCode(request));
        fileFolderInfoMag.createFileFolderInfo(fileFolderInfo);
        JsonResultUtils.writeSingleDataJson(fileFolderInfo,response);
    }

    /**
     * 删除单个  文件夹信息

	 * @param folderId  folder_id
     */
    @RequestMapping(value = "/{folderId}", method = {RequestMethod.DELETE})
	@ApiOperation(value = "删除单个文件夹信息")
	@WrapUpResponseBody
	public void deleteFileFolderInfo(@PathVariable String folderId) {

    	fileFolderInfoMag.deleteFileFolderInfo( folderId);


    }

    /**
     * 新增或保存 文件夹信息

	 * @param fileFolderInfo  {@link FileFolderInfo}
     */
    @RequestMapping( method = {RequestMethod.PUT})
	@ApiOperation(value = "更新文件夹信息")
	@WrapUpResponseBody
    public void updateFileFolderInfo(@RequestBody FileFolderInfo fileFolderInfo, HttpServletRequest request , HttpServletResponse response) {
        fileFolderInfo.setUpdateUser(WebOptUtils.getCurrentUserCode(request));
        fileFolderInfoMag.updateFileFolderInfo(fileFolderInfo);
        JsonResultUtils.writeSingleDataJson(fileFolderInfo,response);
    }
}
