package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.framework.core.common.JsonResultUtils;
import com.centit.framework.core.common.ResponseData;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.dao.PageDesc;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.UuidOpt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/access")
public class AccessManagerController extends BaseController {
	@Resource
	private FileAccessLogManager fileAccessLogManager;
	@Resource
	private FileStoreInfoManager fileStoreInfoManager;
	
	private void applyAccess(FileAccessLog accessLog , HttpServletResponse response)
			throws Exception{
		String fileId = accessLog.getFileId();	
		FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileId);
		if(fileStoreInfo==null){
			JsonResultUtils.writeAjaxErrorMessage(
					FileServerConstant.ERROR_FILE_NOT_EXIST,
					"文件不存："+fileId, response);
			return;
		}
		String ar = accessLog.getAccessRight();
		if(StringUtils.isBlank(ar))
			ar ="A";
		accessLog.setFileId(fileId);
		accessLog.setAccessToken( UuidOpt.getUuidAsString32());
		accessLog.setAuthTime(DatetimeOpt.currentUtilDate());
		accessLog.setAccessRight(ar);
		accessLog.setAccessTimes(0);
		fileStoreInfo.addDownloadTimes();		
		fileAccessLogManager.saveNewAccessLog(accessLog);
		fileStoreInfoManager.updateObject(fileStoreInfo);
		JsonResultUtils.writeSingleDataJson(accessLog, response);
	}
	
	@RequestMapping(value="/apply", method = RequestMethod.POST)
	public void accessFile(@Valid FileAccessLog accessLog , HttpServletResponse response) throws Exception{
		applyAccess(accessLog , response);		
	}
	
	@RequestMapping(value="/japply", method = RequestMethod.POST)
	public void accessFileByJson(@RequestBody FileAccessLog accessLog , HttpServletResponse response) throws Exception{
		applyAccess(accessLog , response);		
	}
	
	@RequestMapping(value="/log/{token}", method = RequestMethod.GET)
	public void getAccessLog(@PathVariable("token") String token,
			HttpServletRequest request, HttpServletResponse response){
		
		FileAccessLog accessLog = fileAccessLogManager.getObjectById(token);
		if(accessLog !=null){
			JsonResultUtils.writeSingleDataJson(accessLog, response);
		}else{
			JsonResultUtils.writeErrorMessageJson(
					"请求日志不存在："+token, response);
		}		
	}
	
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public void listAccessLog( PageDesc pageDesc,
			HttpServletRequest request, HttpServletResponse response){

		Map<String, Object> queryParamsMap = convertSearchColumn(request);
		
		JSONArray listObjects = fileAccessLogManager.listAccessLog(queryParamsMap, pageDesc);
		ResponseData resData = new ResponseData();
	    resData.addResponseData(OBJLIST, listObjects);
	    resData.addResponseData(PAGE_DESC, pageDesc);
	
	    JsonResultUtils.writeResponseDataAsJson(resData, response);
	}

	@RequestMapping(value="/list/{fileId}", method = RequestMethod.GET)
	public void listAccessLogByFileId(@PathVariable("fileId") String fileId, PageDesc pageDesc,
			HttpServletRequest request, HttpServletResponse response){
		
		Map<String, Object> filterMap = convertSearchColumn(request);
		filterMap.put("fileId", fileId);
		
		List<FileAccessLog> listObjects = fileAccessLogManager.listObjects(filterMap, pageDesc);
		ResponseData resData = new ResponseData();
		resData.addResponseData(OBJLIST, listObjects);
		resData.addResponseData(PAGE_DESC, pageDesc);
		
		JsonResultUtils.writeResponseDataAsJson(resData, response);
	}
}