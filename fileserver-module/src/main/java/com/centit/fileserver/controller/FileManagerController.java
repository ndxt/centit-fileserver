package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.fileaccess.FileStoreFactory;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileStore;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.ip.po.OsInfo;
import com.centit.framework.ip.service.IntegrationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/files")
public class FileManagerController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(FileManagerController.class);

	@Resource
	private FileStoreInfoManager fileStoreInfoManager;	
	
	@Resource
	private IntegrationEnvironment integrationEnvironment;

	/**
	 * 根据文件的id物理删除文件(同时删除文件和数据库记录)
	 * @param fileId 文件ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/{fileId}",method = RequestMethod.DELETE)
	public void delete(@PathVariable("fileId") String fileId, HttpServletResponse response){

		FileStoreInfo storeInfo =fileStoreInfoManager.getObjectById(fileId);
		if(storeInfo !=null){
			storeInfo.setFileState("D");
			fileStoreInfoManager.updateObject(storeInfo);
			JsonResultUtils.writeSuccessJson(response);
		}else{
			JsonResultUtils.writeErrorMessageJson(
					"文件不存在："+fileId, response);
		}

	}
	/**
	 * 根据文件的id物理删除文件(同时删除文件和数据库记录)
	 * @param fileId 文件ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/force/{fileId}",method = RequestMethod.DELETE)
	public void deleteForce(@PathVariable("fileId") String fileId, HttpServletResponse response){

		FileStoreInfo storeInfo =fileStoreInfoManager.getObjectById(fileId);
		if(storeInfo !=null){
			String path= storeInfo.getFileStorePath();
			FileStore fs = FileStoreFactory.createDefaultFileStore();
			try {
				fs.deleteFile(path);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				JsonResultUtils.writeErrorMessageJson(
						e.getMessage(), response);
				return;
			}
			storeInfo.setFileState("D");
			fileStoreInfoManager.updateObject(storeInfo);
			JsonResultUtils.writeSuccessJson(response);
		}else{
			JsonResultUtils.writeErrorMessageJson(
					"文件不存在："+fileId, response);
		}
		
	}
	
	/**
	 * 根据文件的id获取文件存储信息
	 * @param fileId 文件ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/{fileId}",method = RequestMethod.GET)
	public void getFileStoreInfo(@PathVariable("fileId") String fileId, HttpServletResponse response){

		FileStoreInfo storeInfo =fileStoreInfoManager.getObjectById(fileId);
		if(storeInfo !=null){
			JsonResultUtils.writeSingleDataJson(storeInfo, response);
		}else{
			JsonResultUtils.writeErrorMessageJson(
					"文件不存在："+fileId, response);
		}		
	}

	/**
	 * 更新文件存储信息
	 * @param storeInfo 文件对象
	 * @param response HttpServletResponse
	 */

	private void updateFileStoreInfo(FileStoreInfo storeInfo, HttpServletResponse response){
		FileStoreInfo dbstoreInfo =fileStoreInfoManager.getObjectById(storeInfo.getFileId());
		
		if(dbstoreInfo !=null){
			dbstoreInfo.copyNotNullProperty(storeInfo);
			fileStoreInfoManager.updateObject(dbstoreInfo);
			JsonResultUtils.writeSingleDataJson(storeInfo, response);
		}else{			
			JsonResultUtils.writeErrorMessageJson(
					"文件不存在："+storeInfo.getFileId(), response);
		}		
	}

	/**
	 * 根据文件的id修改文件存储信息，文件春粗信息按照表单的形式传送
	 * @param fileId 文件ID
	 * @param storeInfo 文件对象
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/{fileId}",method = RequestMethod.POST)
	public void postFileStoreInfo(@PathVariable("fileId") String fileId,
			@Valid FileStoreInfo storeInfo, HttpServletResponse response){
		storeInfo.setFileId(fileId);
		updateFileStoreInfo(storeInfo,response);
	}

	/**
	 * 根据文件的id修改文件存储信息，文件存储信息按照json的格式传送
	 * @param fileId 文件ID
	 * @param storeInfo 文件对象
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/j/{fileId}",method = RequestMethod.POST)
	public void jsonpostFileStoreInfo(@PathVariable("fileId") String fileId,
			@RequestBody FileStoreInfo storeInfo, HttpServletResponse response){
		storeInfo.setFileId(fileId);
		updateFileStoreInfo(storeInfo,response);
	}

	/**
	 * 根据相关的条件查询文件
	 * @param pageDesc 分页对象
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	@RequestMapping(method = RequestMethod.GET)
	public void listStroedFiles( PageDesc pageDesc,
			HttpServletRequest request, HttpServletResponse response) {
	
		Map<String, Object> queryParamsMap = convertSearchColumn(request);
		
		JSONArray listObjects = fileStoreInfoManager.listStoredFiles(queryParamsMap, pageDesc);
		ResponseMapData resData = new ResponseMapData();
	    resData.addResponseData(OBJLIST, listObjects);
	    resData.addResponseData(PAGE_DESC, pageDesc);
	
	    JsonResultUtils.writeResponseDataAsJson(resData, response);
	}


	/**
	 * 获取系统中的所有OS
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/oss",method = RequestMethod.GET)
	public void listOperationSystem(HttpServletRequest request, HttpServletResponse response) {
		List<OsInfo> osinfoList = integrationEnvironment.listOsInfos();
		JsonResultUtils.writeSingleDataJson(osinfoList, response);
	}


	/**
	 * 获取系统所有操作
	 * @param osId 项目编号
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/optids/{osId}",method = RequestMethod.GET)
    public void listOptsByOs(@PathVariable("osId") String osId,
                             HttpServletResponse response) {
        JSONArray listObjects = fileStoreInfoManager.listOptsByOs(osId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }


	/**
	 * 获取系统所有文件属主
	 * @param osId 项目编号
	 * @param optId 模块编号
	 * @param response HttpServletResponse
	 */
    @RequestMapping(value = "/owner/{osId}/{optId}",method = RequestMethod.GET)
    public void listFileOwners(@PathVariable("osId") String osId,
                             @PathVariable("optId") String optId,
                             HttpServletResponse response) {
        JSONArray listObjects = fileStoreInfoManager.listFileOwners(osId,optId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }

	/**
	 * 获取系统所有文件
	 * @param osId 项目编号
	 * @param optId 模块编号
	 * @param owner 所属者
	 * @param response HttpServletResponse
	 */
    @RequestMapping(value = "/files/{osId}/{optId}/{owner}",method = RequestMethod.GET)
    public void listFilesByOwner(@PathVariable("osId") String osId,
                               @PathVariable("optId") String optId,
                                 @PathVariable("owner") String owner,
                               HttpServletResponse response) {

        JSONArray listObjects = fileStoreInfoManager.listFilesByOwner(osId,optId,owner);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }
}