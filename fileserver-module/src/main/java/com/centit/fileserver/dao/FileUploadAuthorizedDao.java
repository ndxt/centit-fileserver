package com.centit.fileserver.dao;


import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class FileUploadAuthorizedDao extends BaseDaoImpl<FileUploadAuthorized, String> {

    public Map<String, String> getFilterField() {
        if( filterField == null){
            filterField = new HashMap<String, String>();

            filterField.put("fileId" , CodeBook.EQUAL_HQL_ID);
        }
        return filterField;
    }

}
