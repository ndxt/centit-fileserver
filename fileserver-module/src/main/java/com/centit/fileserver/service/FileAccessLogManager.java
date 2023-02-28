package com.centit.fileserver.service;


import com.alibaba.fastjson2.JSONArray;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

public interface FileAccessLogManager extends BaseEntityManager<FileAccessLog, String> {
    void saveNewAccessLog(FileAccessLog fileAccessLog);

    int saveAllNewLogs(List<FileAccessLog> fileAccessLogList);


    void deleteObjectsByFileId(String fileId);

    JSONArray listAccessLog(Map<String,Object>queryParamsMap,
            PageDesc pageDesc);
}
