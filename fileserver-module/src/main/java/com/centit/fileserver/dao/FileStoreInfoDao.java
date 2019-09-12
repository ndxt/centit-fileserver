package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class FileStoreInfoDao extends BaseDaoImpl<FileStoreInfo, String> {

    public Map<String, String> getFilterField() {
        if( filterField == null){
            filterField = new HashMap<>();

            filterField.put("fileMd5" , CodeBook.EQUAL_HQL_ID);
        }
        return filterField;
    }
}

