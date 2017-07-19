package com.centit.fileserver.service;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.core.service.BaseEntityManager;

import java.util.Map;

public interface FileStoreInfoManager extends BaseEntityManager<FileStoreInfo, String>{
	/**
	 * @param originalFile 原始文件
	 * @throws Exception
	 */
	public String saveNewFile(FileStoreInfo originalFile);
	
	/**
	 * 删除文件
	 * @param originalFile 原始文件
	 * @throws Exception
	 */
	public void deleteFile(FileStoreInfo originalFile);
	
	/**
	 * 
	 * @param queryParamsMap
	 * @param pageDesc
	 * @return
	 */
	public JSONArray listStoredFiles(Map<String,Object>queryParamsMap,
			PageDesc pageDesc);
	
	//public void saveSynFile(FileStoreInfo file,String filePath) throws Exception;

	public JSONArray listOptsByOs(String osId);

	public JSONArray listFileOwners(String osId,String optId);

	public JSONArray listFilesByOwner(String osId, String optId,String owner);
}
