package com.centit.fileserver.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.dao.DictionaryMapUtils;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Service("fileInfoManager")
@Transactional
public class FileInfoManagerImpl
        extends BaseEntityManagerImpl<FileInfo, String, FileInfoDao>
     implements FileInfoManager {

    @Value("${spring.datasource.url}")
    private String connUrl;

    @Autowired//(name ="fileInfoDao")
    @NotNull
    @Override
    protected void setBaseDao(FileInfoDao baseDao) {
        super.baseDao = baseDao;
    }

    @Override
    public void saveNewObject(FileInfo originalFile){

        if(StringUtils.isBlank(originalFile.getOsId()))
            originalFile.setOsId("NOTSET");
        if(StringUtils.isBlank(originalFile.getOptId()))
            originalFile.setOptId("NOTSET");
        this.baseDao.saveNewObject(originalFile);


    }


    @Override
    public List<FileInfo> listFileInfo(Map<String, Object> param, PageDesc pageDesc) {
        return baseDao.listObjectsByProperties(param,pageDesc);
    }

    @Override
    public void saveNewFile(FileInfo originalFile){
         saveNewObject(originalFile);
    }

    @Override
    public void deleteFile(FileInfo originalFile){

        originalFile.setFileState("D");
        this.baseDao.mergeObject(originalFile);
    }
    @Override
    public  void writeDownloadFileLog(FileInfo fileInfo, String userCode) {
        fileInfo.addDownloadTimes();
        OperationLogCenter.log(OperationLog.create().operation("FileServerLog").user(userCode)
            .method("下载").tag(fileInfo.getFileId())
            .time(DatetimeOpt.currentUtilDate()).content(fileInfo.getFileName()).newObject(fileInfo));
        updateObject(fileInfo);
    }
    @Override
    public JSONArray listStoredFiles(Map<String, Object> queryParamsMap, PageDesc pageDesc) {
        String queryStatement =
                "select a.FILE_ID, a.FILE_MD5, a.FILE_NAME, b.FILE_STORE_PATH, a.FILE_TYPE,"
                + " a.FILE_STATE, a.FILE_DESC, a.INDEX_STATE, a.DOWNLOAD_TIMES, a.OS_ID,"
                + " a.OPT_ID, a.OPT_METHOD, a.OPT_TAG, a.CREATED, a.CREATE_TIME, b.FILE_SIZE,"
                + " a.ENCRYPT_TYPE, a.FILE_OWNER, a.FILE_UNIT, a.ATTACHED_TYPE, a.ATTACHED_FILE_MD5"
                + " from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 where 1=1 "
                + " [ :files | and a.FILE_ID in (:files) ] "
                        //:(SPLITFORIN)files 这个地方files如果不是数组而是逗号分隔的就需要添加这个预处理
                + " [ :(like)fileName | and a.FILE_NAME like :fileName] "
                + " [ :osId | and a.OS_ID = :osId ]"
                + " [ :optId | and a.OPT_ID = :optId ]"
                + " [ :owner | and a.FILE_OWNER = :owner ]"
                + " [ :unitCode | and a.FILE_UNIT = :unitCode ]"
                + " [ :beginDate | and a.CREATE_TIME >= :beginDate ]"
                + " [ :endDate | and a.CREATE_TIME < :endDate ]"
                + " order by a.CREATE_TIME desc";
        //System.out.println(qap.getQuery());
        JSONArray dataList = DictionaryMapUtils.mapJsonArray(
                DatabaseOptUtils.listObjectsByParamsDriverSqlAsJson(baseDao,
                    queryStatement,queryParamsMap , pageDesc), FileInfo.class );
        return dataList;
    }

    @Override
    public FileInfo getDuplicateFile(FileInfo originalFile){
        String queryStatement =
                " where FILE_ID <> ? and FILE_MD5 = ? and file_state='N'" +
                " and ( FILE_OWNER = ? or FILE_UNIT= ? )";
        List<FileInfo> duplicateFiles =
                baseDao.listObjectsByFilter( queryStatement, new Object[]
                {originalFile.getFileId(),originalFile.getFileMd5(),/*originalFile.getFileSize(),*/
                        originalFile.getFileOwner(),originalFile.getFileUnit()});
        if(duplicateFiles!=null && duplicateFiles.size()>0)
            return duplicateFiles.get(0);
        return null;
    }


    /**
     * 同步保存文件
     *
     * @param osId  String
     * @return  JSONArray
     */
    @Override
    public JSONArray listOptsByOs(String osId) {
        String queryStatement =
                "select OPT_ID , count(1) as FILE_COUNT " +
                        "from FILE_INFO " +
                        "where OS_ID = ? " +
                        "group by OPT_ID";
        JSONArray dataList = DatabaseOptUtils.listObjectsBySqlAsJson(
                baseDao,queryStatement,new Object[]{osId});
        return dataList;
    }

    @Override
    public JSONArray listFileOwners(String osId, String optId) {
        String queryStatement;
        DBType dbt = DBType.mapDBType(connUrl);
        if(dbt==DBType.MySql){
            queryStatement = "select ifnull(ifnull(FILE_OWNER,FILE_UNIT),'') as FILE_OWNER, " +
                    "count(1) as FILE_COUNT " +
                    "from FILE_INFO " +
                    "where OS_ID = ? and OPT_ID = ? " +
                    "group by ifnull(ifnull(FILE_OWNER,FILE_UNIT),'') ";
        }else {
            queryStatement = "select nvl(FILE_OWNER,FILE_UNIT) as FILE_OWNER, " +
                        "count(1) as FILE_COUNT " +
                    "from FILE_INFO " +
                    "where OS_ID = ? and OPT_ID = ? " +
                    "group by nvl(FILE_OWNER,FILE_UNIT) ";
        }
        JSONArray dataList = DatabaseOptUtils.listObjectsBySqlAsJson(
                baseDao,queryStatement,new Object[]{osId,optId});
        return dataList;
    }

    @Override
    public JSONArray listFilesByOwner(String osId, String optId, String owner) {
        String queryStatement =
                "select a.FILE_ID, a.FILE_MD5, a.FILE_NAME, b.FILE_STORE_PATH, a.FILE_TYPE,"
                        + " a.FILE_STATE, a.FILE_DESC, a.INDEX_STATE, a.DOWNLOAD_TIMES, a.OS_ID,"
                        + " a.OPT_ID, a.OPT_METHOD, a.OPT_TAG, a.CREATED, a.CREATE_TIME, b.FILE_SIZE,"
                        + " a.ENCRYPT_TYPE, a.FILE_OWNER, a.FILE_UNIT, a.ATTACHED_TYPE, a.ATTACHED_FILE_MD5 " // a.ATTACHED_STORE_PATH
                        + " from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 "
                        + "where a.OS_ID=? and a.OPT_ID = ? " +
                            "and (a.FILE_OWNER = ? or a.FILE_UNIT = ?) ";

        JSONArray dataList = DatabaseOptUtils.listObjectsBySqlAsJson(
                baseDao,queryStatement,new Object[]{osId,optId,owner,owner});
        return dataList;
    }

}
