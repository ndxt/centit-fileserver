package com.centit.fileserver.service;

import com.centit.fileserver.po.FileFavorite;
import com.centit.support.database.utils.PageDesc;

import java.util.List;
import java.util.Map;

/**
 * FileFavorite  Service.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件收藏
 */

public interface FileFavoriteManager {
    void updateFileFavorite(FileFavorite fileFavorite);

    void deleteFileFavorite(String favoriteId);

    FileFavorite getFileFavorite(String favoriteId);

    void createFileFavorite(FileFavorite fileFavorite);

    List<FileFavorite> listFileFavorite(Map<String, Object> param, PageDesc pageDesc);
}
