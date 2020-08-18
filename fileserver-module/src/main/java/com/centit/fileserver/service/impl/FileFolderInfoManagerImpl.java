package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileFolderInfoDao;
import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.service.FileFolderInfoManager;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.support.database.utils.PageDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * FileFolderInfo  Service.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件夹信息
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FileFolderInfoManagerImpl extends BaseEntityManagerImpl<FileFolderInfo, String, FileFolderInfoDao>
    implements FileFolderInfoManager {
    @Autowired
    private FileFolderInfoDao fileFolderInfoDao;


    @Override
    public void updateFileFolderInfo(FileFolderInfo fileFolderInfo) {
        fileFolderInfoDao.updateObject(fileFolderInfo);
        fileFolderInfoDao.saveObjectReferences(fileFolderInfo);
    }

    @Override
    public void createFileFolderInfo(FileFolderInfo fileFolderInfo) {
        fileFolderInfoDao.saveNewObject(fileFolderInfo);
        fileFolderInfoDao.saveObjectReferences(fileFolderInfo);
    }

    @Override
    public List<FileFolderInfo> listFileFolderInfo(Map<String, Object> param, PageDesc pageDesc) {
        return fileFolderInfoDao.listObjectsByProperties(param, pageDesc);
    }


    @Override
    public FileFolderInfo getFileFolderInfo(String folderId) {
        return fileFolderInfoDao.getObjectById(folderId);
    }

    @Override
    public void deleteFileFolderInfo(String folderId) {
        fileFolderInfoDao.deleteObjectById(folderId);
    }

}

