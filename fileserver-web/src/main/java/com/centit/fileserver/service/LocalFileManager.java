package com.centit.fileserver.service;

import com.centit.fileserver.po.FileShowInfo;
import com.centit.framework.model.basedata.IUnitInfo;

import java.util.List;
import java.util.Set;

public interface LocalFileManager{
    public static final String LOCAL_OS_ID = "FILE_SVR";
    public static final String LOCAL_OPT_ID = "LOCAL_FILE";
    public static final String FILE_PATH_SPLIT = "/";

    public Set<IUnitInfo> listUserUnit(String userCode);

    public List<FileShowInfo> listUserFiles(String userCode,String fileShowPath);

    public List<FileShowInfo> listUnitFiles(String unitCode,String fileShowPath);

    public List<FileShowInfo> listUserFileVersions(String userCode,String fileShowPath,String fileName);

    public List<FileShowInfo> listUnitFileVersions(String unitCode,String fileShowPath,String fileName);
}
