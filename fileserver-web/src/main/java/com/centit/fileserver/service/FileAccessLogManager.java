package com.centit.fileserver.service;


import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.core.service.BaseEntityManager;

import java.util.List;
import java.util.Map;

public interface FileAccessLogManager extends BaseEntityManager<FileAccessLog, String>{
	public String saveNewAccessLog(FileAccessLog fileAccessLog);
		
	public List<String> saveAllNewLogs(List<FileAccessLog> fileAccessLogList);


	public void deleteObjectsByFileId(String fileId);
	
	public JSONArray listAccessLog(Map<String,Object>queryParamsMap,
			PageDesc pageDesc);
}
