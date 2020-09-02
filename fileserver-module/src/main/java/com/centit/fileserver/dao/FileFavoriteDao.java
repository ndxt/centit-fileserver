package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileFavorite;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;


/**
 * FileFavoriteDao  Repository.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件收藏
 */

@Repository
public class FileFavoriteDao extends BaseDaoImpl<FileFavorite, String> {

    private static final Logger logger = LoggerFactory.getLogger(FileFavoriteDao.class);

    @Override
    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<String, String>();
        filterField.put("favoriteId", CodeBook.EQUAL_HQL_ID);
        filterField.put("fileId", CodeBook.EQUAL_HQL_ID);
        filterField.put("favoriteUser", CodeBook.EQUAL_HQL_ID);
        filterField.put("favoriteTime", CodeBook.EQUAL_HQL_ID);
        filterField.put("withFile","file_id in (select file_id from file_info a join file_store_info b on a.file_md5=b.file_md5)");
        return filterField;
    }
}
