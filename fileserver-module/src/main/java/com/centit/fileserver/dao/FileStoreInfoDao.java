package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileStoreInfo;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.support.algorithm.CollectionsOpt;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FileStoreInfoDao extends BaseDaoImpl<FileStoreInfo, String> {

    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<>();
        filterField.put("fileMd5" , CodeBook.EQUAL_HQL_ID);
        return filterField;
    }

    public List<FileStoreInfo> listTempFile(int limit){
        return this.listObjectsByProperties(
            CollectionsOpt.createHashMap( "isTemp", "T"), 0, limit);
    }
}

