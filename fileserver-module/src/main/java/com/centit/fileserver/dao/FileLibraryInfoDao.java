package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;


/**
 * FileLibraryInfoDao  Repository.
 * create by scaffold 2020-08-18 13:38:13
 *
 * @author codefan@sina.com
 * 文件库信息
 */

@Repository
public class FileLibraryInfoDao extends BaseDaoImpl<FileLibraryInfo, String> {

    private static final Logger logger = LoggerFactory.getLogger(FileLibraryInfoDao.class);

    @Override
    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<String, String>();
        filterField.put("libraryId", CodeBook.EQUAL_HQL_ID);
        filterField.put("libraryName", CodeBook.EQUAL_HQL_ID);
        filterField.put("libraryType", CodeBook.EQUAL_HQL_ID);
        filterField.put("createUser", CodeBook.EQUAL_HQL_ID);
        filterField.put("createTime", CodeBook.EQUAL_HQL_ID);
        filterField.put("ownUnit", CodeBook.EQUAL_HQL_ID);
        filterField.put("ownUser", CodeBook.EQUAL_HQL_ID);
        filterField.put("isCreateFolder", CodeBook.EQUAL_HQL_ID);
        filterField.put("isUpload", CodeBook.EQUAL_HQL_ID);
        filterField.put("authCode", CodeBook.EQUAL_HQL_ID);

        return filterField;
    }
}
