package com.centit.fileserver.service;

import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.framework.model.basedata.IUnitInfo;
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

public interface FileLibraryInfoManager extends BaseEntityManager<FileLibraryInfo, String> {
    void updateFileLibraryInfo(FileLibraryInfo fileLibraryInfo);

    void deleteFileLibraryInfo(String libraryId);

    FileLibraryInfo getFileLibraryInfo(String libraryId);

    void createFileLibraryInfo(FileLibraryInfo fileLibraryInfo);

    List<FileLibraryInfo> listFileLibraryInfo(String userCode);
    List<IUnitInfo> listUnitPathsByUserCode(String userCode);
    void initPersonLibrary(String userCode);
}
