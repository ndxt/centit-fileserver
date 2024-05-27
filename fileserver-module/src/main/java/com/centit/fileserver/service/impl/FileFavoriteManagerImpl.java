package com.centit.fileserver.service.impl;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.dao.FileFavoriteDao;
import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.dao.FileStoreInfoDao;
import com.centit.fileserver.po.FileFavorite;
import com.centit.fileserver.po.FileFolderInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileFavoriteManager;
import com.centit.fileserver.service.FileFolderInfoManager;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.filter.RequestThreadLocal;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * FileFavorite  Service.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件收藏
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FileFavoriteManagerImpl extends BaseEntityManagerImpl<FileFavorite, String, FileFavoriteDao>
    implements FileFavoriteManager {
    @Autowired
    private FileFavoriteDao fileFavoriteDao;
    @Autowired
    private FileInfoDao fileInfoDao;
    @Autowired
    private FileStoreInfoDao fileStoreInfoDao;
    @Autowired
    private FileFolderInfoManager fileFolderInfoMag;
    @Autowired
    private FileLibraryInfoManager fileLibraryInfoManager;


    @Override
    public void updateFileFavorite(FileFavorite fileFavorite) {
        fileFavoriteDao.updateObject(fileFavorite);
    }

    @Override
    public void createFileFavorite(FileFavorite fileFavorite) {
        fileFavoriteDao.saveNewObject(fileFavorite);
    }

    @Override
    public List<FileFavorite> listFileFavorite(Map<String, Object> param, PageDesc pageDesc) {
        HttpServletRequest request = RequestThreadLocal.getLocalThreadWrapperRequest();
        String topUnit = WebOptUtils.getCurrentTopUnit(request);

        param.put("withFile", "1");
        List<FileFavorite> list = fileFavoriteDao.listObjectsByProperties(param, pageDesc);
        list.forEach(e -> {
            FileInfo fileInfo = fileInfoDao.getObjectById(e.getFileId());
            if (fileInfo != null) {
                e.setFileName(fileInfo.getFileName());
                e.setFileType(fileInfo.getFileType());
                e.setUploadUser(CodeRepositoryUtil.getUserName(topUnit,
                    fileInfo.getFileOwner()));
                e.setLibraryId(fileInfo.getLibraryId());
                e.setParentFolder(fileInfo.getParentFolder());
                e.setShowPath(getShowPath(fileInfo.getFileShowPath(), fileInfo.getLibraryId()));
                FileStoreInfo fileStoreInfo = fileStoreInfoDao.getObjectById(fileInfo.getFileMd5());
                if (fileStoreInfo != null) {
                    e.setFileSize(fileStoreInfo.getFileSize());
                }
            }
        });
        return list;
    }

    @Override
    public String getShowPath(String fileShowPath, String libraryId) {
        if(fileShowPath==null){
            return null;
        }
        String[] paths = StringUtils.split(fileShowPath, "/");
        StringBuilder showPath = new StringBuilder();
        for (String path : paths) {
            showPath.append("/");
            if (!"-1".equals(path)) {
                FileFolderInfo fileFolderInfo = fileFolderInfoMag.getFileFolderInfo(path);
                if (fileFolderInfo != null) {
                    showPath.append(fileFolderInfo.getFolderName());
                } else {
                    showPath.append(path);
                }
            } else {
                FileLibraryInfo fileLibraryInfo = fileLibraryInfoManager.getFileLibrary(libraryId);
                if (fileLibraryInfo != null) {
                    showPath.append(fileLibraryInfo.getLibraryName());
                } else {
                    showPath.append(path);
                }
            }
        }
        return showPath.toString();
    }


    @Override
    public FileFavorite getFileFavorite(String favoriteId) {
        return fileFavoriteDao.fetchObjectReferences(fileFavoriteDao.getObjectById(favoriteId));
    }

    @Override
    public void deleteFileFavorite(String favoriteId) {
        fileFavoriteDao.deleteObjectById(favoriteId);
    }

}

