package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileFavoriteDao;
import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.dao.FileStoreInfoDao;
import com.centit.fileserver.po.FileFavorite;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileFavoriteManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.support.database.utils.PageDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        param.put("withFile","1");
        List<FileFavorite> list=fileFavoriteDao.listObjects(param,pageDesc);
        list.forEach(e->{
            FileInfo fileInfo= fileInfoDao.getObjectById(e.getFileId());
            if(fileInfo!=null){
                e.setFileName(fileInfo.getFileName());
                e.setUploadUser(CodeRepositoryUtil.getUserName(fileInfo.getFileOwner()));
                FileStoreInfo fileStoreInfo=fileStoreInfoDao.getObjectById(fileInfo.getFileMd5());
                if(fileStoreInfo!=null){
                    e.setFileSize(fileStoreInfo.getFileSize());
                }
            }
        });

        return list;
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

