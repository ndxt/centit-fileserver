package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.po.FileShowInfo;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.model.basedata.IUnitInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by codefan on 17-1-17.
 */
@Service("localFileManager")
@Transactional
public class LocalFileManagerImpl implements LocalFileManager {

    @Autowired
    @NotNull
    protected FileInfoDao fileInfoDao;

    @Override
    public Set<IUnitInfo> listUserUnit(String topUnit, String userCode) {
        return CodeRepositoryUtil.getUserUnits(topUnit, userCode);
    }

    /**
     * 获取用户文件列表
     * @param userCode 用户代码
     * @param fileShowPath 文件目录
     * @return 文件列表
     */
    @Override
    public List<FileShowInfo> listUserFiles(String userCode, String fileShowPath) {
        List<FileShowInfo> files = fileInfoDao.listUserFiles(userCode,fileShowPath);
        Set<String> dirs = fileInfoDao.listUserDirectories(userCode,fileShowPath);
        if(dirs !=null){
            for(String dir : dirs) {
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath+ LocalFileManager.FILE_PATH_SPLIT +dir);
                file.setCatalogType("p");
                file.setFileType("d");
                file.setFileName(dir);
                files.add(file);
            }
        }
        return files;
    }

    @Override
    public List<FileShowInfo> listFolderFiles(String topUnit, Map<String, Object> searchColumn) {
        return fileInfoDao.listFolderFiles(topUnit, searchColumn);
    }

    @Override
    public List<FileShowInfo> listUnitFiles(String unitCode, String fileShowPath) {
        List<FileShowInfo> files = fileInfoDao.listUnitFiles(unitCode,fileShowPath);
        Set<String> dirs = fileInfoDao.listUnitDirectories(unitCode,fileShowPath);
        if(dirs !=null){
            for(String dir : dirs) {
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath + LocalFileManager.FILE_PATH_SPLIT + dir);
                file.setCatalogType("d");
                file.setFileType("d");
                file.setFileName(dir);
                files.add(file);
            }
        }
        return files;
    }

    @Override
    public List<FileShowInfo> listUserFileVersions(String userCode, String fileShowPath, String fileName) {
        return fileInfoDao.listUserFileVersions(userCode, fileShowPath, fileName);
    }

    @Override
    public List<FileShowInfo> listUnitFileVersions(String unitCode, String fileShowPath, String fileName) {
        return fileInfoDao.listUnitFileVersions(unitCode, fileShowPath, fileName);
    }

}

