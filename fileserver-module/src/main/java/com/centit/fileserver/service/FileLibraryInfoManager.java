package com.centit.fileserver.service;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.framework.model.basedata.IUnitInfo;

import java.util.List;
import java.util.Set;

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
    void initUnitLibrary(String unitCode,String userCode);
    Set<String> getUnits(String userCode);

    boolean checkAuth(FileInfo fileInfo, String userCode, String authCode);
}
