package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileFavoriteDao;
import com.centit.fileserver.po.FileFavorite;
import com.centit.fileserver.service.FileFavoriteManager;
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


    @Override
    public void updateFileFavorite(FileFavorite fileFavorite) {
        fileFavoriteDao.updateObject(fileFavorite);
        fileFavoriteDao.saveObjectReferences(fileFavorite);
    }

    @Override
    public void createFileFavorite(FileFavorite fileFavorite) {
        fileFavoriteDao.saveNewObject(fileFavorite);
        fileFavoriteDao.saveObjectReferences(fileFavorite);
    }

    @Override
    public List<FileFavorite> listFileFavorite(Map<String, Object> param, PageDesc pageDesc) {
        return fileFavoriteDao.listObjectsByProperties(param, pageDesc);
    }


    @Override
    public FileFavorite getFileFavorite(String favoriteId) {
        return fileFavoriteDao.getObjectById(favoriteId);
    }

    @Override
    public void deleteFileFavorite(String favoriteId) {
        fileFavoriteDao.deleteObjectById(favoriteId);
    }

}

