package com.centit.fileserver.service;

import com.centit.fileserver.po.FileFolderInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

/**
 * FileFolderInfo  Service.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件夹信息
 */

public interface FileFolderInfoManager extends BaseEntityManager<FileFolderInfo, String> {
    FileFolderInfo updateFileFolderInfo(FileFolderInfo fileFolderInfo);

    void deleteFileFolderInfo(String folderId);

    FileFolderInfo getFileFolderInfo(String folderId);

    void createFileFolderInfo(FileFolderInfo fileFolderInfo);

    List<FileFolderInfo> listFileFolderInfo(Map<String, Object> param, PageDesc pageDesc);
}
