package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileFolderInfo;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.support.algorithm.CollectionsOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * FileFolderInfoDao  Repository.
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * 文件夹信息
 */

@Repository
public class FileFolderInfoDao extends BaseDaoImpl<FileFolderInfo, String> {

    private static final Logger logger = LoggerFactory.getLogger(FileFolderInfoDao.class);

    @Override
    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<String, String>();
        filterField.put("folderId", CodeBook.EQUAL_HQL_ID);
        filterField.put("libraryId", CodeBook.EQUAL_HQL_ID);
        filterField.put("parentFolder", CodeBook.EQUAL_HQL_ID);
        filterField.put("folderPath", CodeBook.EQUAL_HQL_ID);
        filterField.put("isCreateFolder", CodeBook.EQUAL_HQL_ID);
        filterField.put("isUpload", CodeBook.EQUAL_HQL_ID);
        filterField.put("authCode", CodeBook.EQUAL_HQL_ID);
        filterField.put("folderName", CodeBook.EQUAL_HQL_ID);
        filterField.put("createUser", CodeBook.EQUAL_HQL_ID);
        filterField.put("createTime", CodeBook.EQUAL_HQL_ID);
        filterField.put("updateUser", CodeBook.EQUAL_HQL_ID);
        filterField.put("updateTime", CodeBook.EQUAL_HQL_ID);
        filterField.put("pathLike","folder_path like :pathLike");
        return filterField;
    }

    public FileFolderInfo getFolderInfo(String libraryId, List<String> path){
        if(path==null || path.size()==0){
            return this.getObjectByProperties(CollectionsOpt.createHashMap(
                "libraryId",  libraryId, "folderName", "."
            ));
        }

        FileFolderInfo folderInfo = this.getObjectByProperties(CollectionsOpt.createHashMap(
            "libraryId",  libraryId, "parentFolder_nv", "true",
            "folderName", path.get(0)));

        for(int i = 1; i< path.size(); i++){
            if(folderInfo != null){
                folderInfo = this.getObjectByProperties(CollectionsOpt.createHashMap(
                    "libraryId",  libraryId, "parentFolder", folderInfo.getFolderId(),
                    "folderName", path.get(i)));
            }
        }
        return folderInfo;
    }

}
