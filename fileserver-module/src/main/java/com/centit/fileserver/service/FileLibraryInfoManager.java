package com.centit.fileserver.service;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.framework.jdbc.service.BaseEntityManager;
import com.centit.framework.model.basedata.UnitInfo;

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
    void updateFileLibrary(FileLibraryInfo fileLibraryInfo);

    void deleteFileLibrary(String libraryId);

    FileLibraryInfo getFileLibrary(String topUnit, String libraryId);

    void createFileLibrary(FileLibraryInfo fileLibraryInfo);

    List<FileLibraryInfo> listFileLibrary(String topUnit, String userCode);
    List<UnitInfo> listUnitPathsByUserCode(String topUnit, String userCode);
    FileLibraryInfo initPersonLibrary(String topUnit, String userCode);
    void initUnitLibrary(String topUnit, String unitCode,String userCode);
    Set<String> getUnits(String topUnit, String userCode);

    boolean checkAuth(String topUnit, FileInfo fileInfo, String userCode, String authCode);
}
