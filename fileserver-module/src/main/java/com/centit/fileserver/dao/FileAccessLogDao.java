package com.centit.fileserver.dao;


import com.centit.fileserver.po.FileAccessLog;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class FileAccessLogDao extends BaseDaoImpl<FileAccessLog, String> {

    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<String, String>();
        filterField.put("fileId" , CodeBook.EQUAL_HQL_ID);
        return filterField;
    }

}
