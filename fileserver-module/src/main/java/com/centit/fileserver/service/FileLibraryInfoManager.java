package com.centit.fileserver.service;

import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

/**
 * FileLibraryInfo  Service.
 * create by scaffold 2020-08-18 13:38:13
 *
 * @author codefan@sina.com
 * 文件库信息
 */

public interface FileLibraryInfoManager {
    void updateFileLibraryInfo(FileLibraryInfo fileLibraryInfo);

    void deleteFileLibraryInfo(String libraryId);

    FileLibraryInfo getFileLibraryInfo(String libraryId);

    void createFileLibraryInfo(FileLibraryInfo fileLibraryInfo);

    List<FileLibraryInfo> listFileLibraryInfo(Map<String, Object> param, PageDesc pageDesc);
}
