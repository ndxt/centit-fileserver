package com.centit.fileserver.service;

import com.centit.fileserver.po.FileShowInfo;
import com.centit.framework.model.basedata.IUnitInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocalFileManager{
    String LOCAL_OS_ID = "FILE_SVR";
    String LOCAL_OPT_ID = "LOCAL_FILE";
    String FILE_PATH_SPLIT = "/";

    Set<IUnitInfo> listUserUnit(String userCode);

    /**
     * 获取用户文件列表
     * @param userCode 用户代码
     * @param fileShowPath 文件目录
     * @return 文件列表
     */
    List<FileShowInfo> listUserFiles(String userCode,String fileShowPath);
    List<FileShowInfo> listFolderFiles(Map<String, Object> searchColumn);


    List<FileShowInfo> listUnitFiles(String unitCode,String fileShowPath);

    List<FileShowInfo> listUserFileVersions(String userCode,String fileShowPath,String fileName);

    List<FileShowInfo> listUnitFileVersions(String unitCode,String fileShowPath,String fileName);
}
