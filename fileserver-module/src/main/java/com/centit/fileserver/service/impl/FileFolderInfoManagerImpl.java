package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileFolderInfoDao;
import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileFolderInfoManager;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    private FileInfoDao fileInfoDao;

    @Override
    public FileFolderInfo updateFileFolderInfo(FileFolderInfo fileFolderInfo) {
        if (fileFolderInfo.getParentFolder().equals(fileFolderInfo.getFolderId())) {
            fileFolderInfo.setMsg("101不能移动到自身");
            return fileFolderInfo;
        }
        FileFolderInfo oldFileFolder = getFileFolderInfo(fileFolderInfo.getFolderId());
        List<FileFolderInfo> fileFolderInfos = listFileFolderInfo(CollectionsOpt.createHashMap("folderPath", fileFolderInfo.getFolderPath(),
            "folderName", fileFolderInfo.getFolderName(), "libraryId", fileFolderInfo.getLibraryId()), null);
        if (fileFolderInfos.size() == 1 && !fileFolderInfos.get(0).getFolderId().equals(fileFolderInfo.getFolderId())) {
            fileFolderInfo.setMsg("100文件夹已存在");
            return fileFolderInfo;
        }
        fileFolderInfoDao.updateObject(fileFolderInfo);
        if(!StringBaseOpt.isNvl(fileFolderInfo.getFolderPath()) && !StringBaseOpt.isNvl(fileFolderInfo.getLibraryId())) {
            if (!oldFileFolder.getFolderPath().equals(fileFolderInfo.getFolderPath()) ||
                !oldFileFolder.getLibraryId().equals(fileFolderInfo.getLibraryId())) {
                String oldPath = oldFileFolder.getFolderPath() + "/" + oldFileFolder.getFolderId();
                String newPath = fileFolderInfo.getFolderPath() + "/" + fileFolderInfo.getFolderId();
                DatabaseOptUtils.doExecuteSql(fileFolderInfoDao,
                    "update file_folder_info set library_id=?,folder_path=replace(folder_path,?,?) where folder_path like ?",
                    new Object[]{fileFolderInfo.getLibraryId(), oldPath, newPath, oldPath + "%"});
                DatabaseOptUtils.doExecuteSql(fileFolderInfoDao,
                    "update file_info set library_id=?,file_show_path=replace(file_show_path,?,?) where file_show_path like ?",
                    new Object[]{fileFolderInfo.getLibraryId(), oldPath, newPath, oldPath + "%"});
            }
        }
        if (!oldFileFolder.getFolderName().equals(fileFolderInfo.getFolderName())) {
            OperationLogCenter.log(OperationLog.create()
                .operation(FileIOUtils.LOG_OPERATION_NAME).user("admin")
                .unit(fileFolderInfo.getLibraryId())
                .method("更新文件夹信息").tag(fileFolderInfo.getFolderId()).time(DatetimeOpt.currentUtilDate())
                .content("更改文件夹名称").oldObject(oldFileFolder.getFolderName()).newObject(fileFolderInfo.getFolderName()));
        }
        return fileFolderInfo;
    }

    @Override
    public void createFileFolderInfo(FileFolderInfo fileFolderInfo) {
        fileFolderInfoDao.saveNewObject(fileFolderInfo);
        if (!StringBaseOpt.isNvl(fileFolderInfo.getOldFoldId())) {
            FileFolderInfo oldFileFolder = getFileFolderInfo(fileFolderInfo.getOldFoldId());
            String oldpath = oldFileFolder.getFolderPath() + "/" + oldFileFolder.getFolderId();
            String newpath = fileFolderInfo.getFolderPath() + "/" + fileFolderInfo.getFolderId();
            List<FileFolderInfo> fileFolderInfos = fileFolderInfoDao.listObjectsByProperties(CollectionsOpt.createHashMap("pathLike", oldpath + "%"));
            List<FileInfo> fileInfos = fileInfoDao.listObjectsByProperties(CollectionsOpt.createHashMap("pathLike", oldpath + "%"));
            if (fileFolderInfos.size() > 0) {
                for (FileFolderInfo folderInfo : fileFolderInfos) {
                    folderInfo.setOldFoldId(folderInfo.getFolderId());
                    folderInfo.setFolderId(UuidOpt.getUuidAsString32());
                    folderInfo.setFolderPath(StringUtils.replace(folderInfo.getFolderPath(), oldpath, newpath));
                    for (FileFolderInfo folderInfo1 : fileFolderInfos) {
                        folderInfo1.setFolderPath(StringUtils.replace(folderInfo1.getFolderPath(), folderInfo.getOldFoldId(), folderInfo.getFolderId()));
                    }
                    for (FileInfo fileInfo : fileInfos) {
                        fileInfo.setFileId(UuidOpt.getUuidAsString32());
                        String path = StringUtils.replace(fileInfo.getFileShowPath(), oldpath, newpath);
                        path = StringUtils.replace(path, folderInfo.getOldFoldId(), folderInfo.getFolderId());
                        fileInfo.setFileShowPath(path);
                        fileInfo.setLibraryId(fileFolderInfo.getLibraryId());
                    }
                    folderInfo.setLibraryId(fileFolderInfo.getLibraryId());
                }
            } else {
                for (FileInfo fileInfo : fileInfos) {
                    fileInfo.setFileId(UuidOpt.getUuidAsString32());
                    String path = StringUtils.replace(fileInfo.getFileShowPath(), oldpath, newpath);
                    fileInfo.setFileShowPath(path);
                    fileInfo.setLibraryId(fileFolderInfo.getLibraryId());
                }
            }
            for (FileFolderInfo folderInfo : fileFolderInfos) {
                folderInfo.setParentFolder(StringUtils.substringAfterLast(folderInfo.getFolderPath(), "/"));
                fileFolderInfoDao.saveNewObject(folderInfo);
            }
            for (FileInfo fileInfo : fileInfos) {
                fileInfo.setParentFolder(StringUtils.substringAfterLast(fileInfo.getFileShowPath(), "/"));
                fileInfoDao.saveNewObject(fileInfo);
            }
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileFolderInfo(String folderId) {
        FileFolderInfo fileFolderInfo = fileFolderInfoDao.getObjectById(folderId);
        fileFolderInfoDao.deleteObjectById(folderId);
        String path = fileFolderInfo.getFolderPath() + "/" + fileFolderInfo.getFolderId() + "%";
        DatabaseOptUtils.doExecuteNamedSql(fileFolderInfoDao, "delete from file_folder_info where folder_path like :path",
            CollectionsOpt.createHashMap("path", path));
        DatabaseOptUtils.doExecuteNamedSql(fileFolderInfoDao, "delete from file_info where file_show_path like :path",
            CollectionsOpt.createHashMap("path", path));
    }

}

