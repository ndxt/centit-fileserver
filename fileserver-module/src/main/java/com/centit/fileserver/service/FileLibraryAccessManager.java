package com.centit.fileserver.service;

import com.centit.fileserver.po.FileLibraryAccess;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

/**
 * FileLibraryAccess  Service.
 * create by scaffold 2020-08-18 13:38:15
 *
 * @author codefan@sina.com
 * 项目库授权信息
 */

public interface FileLibraryAccessManager {
    void updateFileLibraryAccess(FileLibraryAccess fileLibraryAccess);


    void deleteFileLibraryAccess(String accessId);

    FileLibraryAccess getFileLibraryAccess(String accessId);

    void createFileLibraryAccess(FileLibraryAccess fileLibraryAccess);

    List<FileLibraryAccess> listFileLibraryAccess(Map<String, Object> param, PageDesc pageDesc);
}
