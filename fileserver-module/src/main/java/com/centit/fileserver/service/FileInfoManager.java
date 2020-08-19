package com.centit.fileserver.service;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

public interface FileInfoManager extends BaseEntityManager<FileInfo, String> {
    List<FileInfo> listFileInfo(Map<String, Object> param, PageDesc pageDesc);
    /**
     * @param originalFile 原始文件
     */
    void saveNewFile(FileInfo originalFile);

    /**
     * 删除文件
     * @param originalFile 原始文件
     */
    void deleteFile(FileInfo originalFile);

    FileInfo getDuplicateFile(FileInfo originalFile);

    FileInfo getDuplicateFileByShowPath(FileInfo originalFile);
    //String owner, String unit, String fileMd5, long fileSize, String fileId)
    /**
     * @param queryParamsMap Map &lt; String,Object &gt;
     * @param pageDesc PageDesc
     * @return JSONArray 文件
     */
    JSONArray listStoredFiles(Map<String,Object> queryParamsMap,
            PageDesc pageDesc);

    //void saveSynFile(FileStoreInfo file,String filePath) throws Exception;

    JSONArray listOptsByOs(String osId);

    JSONArray listFileOwners(String osId,String optId);

    JSONArray listFilesByOwner(String osId, String optId,String owner);
}
