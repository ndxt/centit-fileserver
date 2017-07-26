package com.centit.fileserver.service;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.core.service.BaseEntityManager;

import java.util.Map;

public interface FileStoreInfoManager extends BaseEntityManager<FileStoreInfo, String>{
	/**
	 * @param originalFile 原始文件
	 */
	String saveNewFile(FileStoreInfo originalFile);
	
	/**
	 * 删除文件
	 * @param originalFile 原始文件
	 */
	void deleteFile(FileStoreInfo originalFile);
	
	/**
	 * 
	 * @param queryParamsMap Map<String,Object>
	 * @param pageDesc PageDesc
	 * @return JSONArray
	 */
	JSONArray listStoredFiles(Map<String,Object> queryParamsMap,
			PageDesc pageDesc);
	
	//void saveSynFile(FileStoreInfo file,String filePath) throws Exception;

	JSONArray listOptsByOs(String osId);

	JSONArray listFileOwners(String osId,String optId);

	JSONArray listFilesByOwner(String osId, String optId,String owner);
}
