package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileLibraryAccess;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;


/**
 * FileLibraryAccessDao  Repository.
 * create by scaffold 2020-08-18 13:38:15
 *
 * @author codefan@sina.com
 * 项目库授权信息
 */

@Repository
public class FileLibraryAccessDao extends BaseDaoImpl<FileLibraryAccess, String> {

    private static final Logger logger = LoggerFactory.getLogger(FileLibraryAccessDao.class);

    @Override
    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<String, String>();

        filterField.put("accessId", CodeBook.EQUAL_HQL_ID);


        filterField.put("libraryId", CodeBook.EQUAL_HQL_ID);

        filterField.put("accessUsercode", CodeBook.EQUAL_HQL_ID);

        filterField.put("createUser", CodeBook.EQUAL_HQL_ID);

        filterField.put("createTime", CodeBook.EQUAL_HQL_ID);

        return filterField;
    }
}
