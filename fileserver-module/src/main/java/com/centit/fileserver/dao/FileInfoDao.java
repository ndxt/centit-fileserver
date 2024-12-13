package com.centit.fileserver.dao;

import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileShowInfo;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.QueryAndNamedParams;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FileInfoDao extends BaseDaoImpl<FileInfo, String> {

    @Override
    public Map<String, String> getFilterField() {
        Map<String, String> filterField = new HashMap<>();

        filterField.put("groupId" , CodeBook.EQUAL_HQL_ID);
        filterField.put("isValid" , CodeBook.EQUAL_HQL_ID);
        filterField.put("files", " fileId in (?) ");
        filterField.put("fileName",CodeBook.EQUAL_HQL_ID);
        filterField.put("fileShowPath",CodeBook.EQUAL_HQL_ID);
        filterField.put("fileState",CodeBook.EQUAL_HQL_ID);
        filterField.put("pathLike","file_show_path like :pathLike");
        return filterField;
    }

    private static String trimFilePath(String filePath){
        if(StringUtils.isBlank(filePath)) {
            return "";
        }
        String tfp = filePath.trim();
        if(tfp.endsWith("/") || tfp.endsWith("\\")) {
            return tfp.substring(0,tfp.length()-1);
        }
        return tfp;
    }

    //subStr('你好.hell0.world,', length('你好.')+1, instr( subStr('你好.hell0.world,',length('你好.')+1),'.')-1)
    public Set<String> listUserDirectories(String userCode, String fileShowPath) {
        //StringUtils.indexOf(DatabaseOptUtils.getDialectName(),"Oracle")>=0
        //这个地方需要根据不同的数据库编写不同的sql语句
        Set<String> dirs = new HashSet<>();
        DBType dbt = DBType.mapDBType(this.getConnection());
        List<?> objects = null;
        if (StringUtils.isBlank(fileShowPath)) {
            String sqlsenOralce = "select distinct subStr( CONCAT(FILE_SHOW_PATH,'/'), 1,instr( CONCAT(FILE_SHOW_PATH,'/'),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH is not null and FILE_SHOW_PATH<>'/'";
            String sqlsenMysql= "select distinct subString( CONCAT(FILE_SHOW_PATH,'/'), 1,instr( CONCAT(FILE_SHOW_PATH,'/'),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH is not null and FILE_SHOW_PATH<>'' and FILE_SHOW_PATH<>'/'";
            objects = DatabaseOptUtils.listObjectsByNamedSql(this,
                    dbt==DBType.MySql?sqlsenMysql:sqlsenOralce,
                    CollectionsOpt.createHashMap("uc",userCode));
        } else {
            String fsp = trimFilePath(fileShowPath)+ LocalFileManager.FILE_PATH_SPLIT;
            String sqlsenOralce = "select distinct subStr(CONCAT(FILE_SHOW_PATH,'/'), length(:fsp)+1, " +
                    "instr( subStr(CONCAT(FILE_SHOW_PATH,'/'),length(:fsp)+1),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH like :fspmatch";
            String sqlsenMysql = "select distinct subString(CONCAT(FILE_SHOW_PATH,'/'), length(:fsp)+1, " +
                    "instr( subString(CONCAT(FILE_SHOW_PATH,'/'),length(:fsp)+1),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH like :fspmatch";
            objects = DatabaseOptUtils.listObjectsByNamedSql(this,
                    dbt==DBType.MySql?sqlsenMysql:sqlsenOralce,
                    CollectionsOpt.createHashMap(
                            "fsp",fsp,//".",
                            "uc",userCode,
                            "fspmatch",fsp+"%"));

        }
        if(objects !=null){
            for(Object obj:objects){
                String sd =StringBaseOpt.objectToString(obj);
                if(StringUtils.isNotBlank(sd)) {
                    dirs.add(sd);
                }
            }
        }
        return dirs;
    }

    //subStr('你好.hell0.world,', length('你好.')+1, instr( subStr('你好.hell0.world,',length('你好.')+1),'.')-1)
    public Set<String> listUnitDirectories(String unitCode, String fileShowPath) {
        //StringUtils.indexOf(DatabaseOptUtils.getDialectName(),"Oracle")>=0
        //这个地方需要根据不同的数据库编写不同的sql语句
        Set<String> dirs = new HashSet<>();
        DBType dbt = DBType.mapDBType(this.getConnection());
        List<?> objects = null;
        if (StringUtils.isBlank(fileShowPath)) {
            String sqlsenOralce = "select distinct subStr(CONCAT(FILE_SHOW_PATH,'/'), 1,instr( CONCAT(FILE_SHOW_PATH,'/'),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH is not null and FILE_SHOW_PATH<>'/'";
            String sqlsenMysql = "select distinct subString(CONCAT(FILE_SHOW_PATH,'/'), 1,instr( CONCAT(FILE_SHOW_PATH,'/'),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH is not null and FILE_SHOW_PATH<>'' and FILE_SHOW_PATH<>'/'";
            objects = DatabaseOptUtils.listObjectsByNamedSql(this,
                    dbt==DBType.MySql?sqlsenMysql:sqlsenOralce,
                    CollectionsOpt.createHashMap("uc",unitCode));
        } else {
            String fsp = trimFilePath(fileShowPath)+ LocalFileManager.FILE_PATH_SPLIT;
            String sqlsenOralce = "select distinct subStr(CONCAT(FILE_SHOW_PATH,'/'), length(:fsp)+1, " +
                    "instr( subStr(CONCAT(FILE_SHOW_PATH,'/'),length(:fsp)+1),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH like :fspmatch";
            String sqlsenMysql = "select distinct subString(CONCAT(FILE_SHOW_PATH,'/'), length(:fsp)+1, " +
                    "instr( subString(CONCAT(FILE_SHOW_PATH,'/'),length(:fsp)+1),'/')-1) " +
                    "from FILE_INFO " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH like :fspmatch";
            objects = DatabaseOptUtils.listObjectsByNamedSql(this,
                    dbt==DBType.MySql?sqlsenMysql:sqlsenOralce,
                    CollectionsOpt.createHashMap(
                            "fsp",fsp,// ".",
                            "uc",unitCode,
                            "fspmatch",fsp+"%"));
        }

        if(objects !=null){
            for(Object obj:objects){
                String sd =StringBaseOpt.objectToString(obj);
                if(StringUtils.isNotBlank(sd)) {
                    dirs.add(sd);
                }
            }
        }
        return dirs;
    }

    public List<FileShowInfo> listFolderFiles(String topUnit, Map<String, Object> searchColumn){
        String sqlsen="select a.file_name,b.file_id,a.file_sum,b.ENCRYPT_TYPE,b.create_time,c.file_size,b.file_show_path," +
            "d.favorite_id,b.file_type,b.download_times,b.file_owner from " +
            "(select a.FILE_NAME,parent_folder,library_id, count(1) as FILE_SUM, max(a.CREATE_TIME) as CREATE_TIME from FILE_INFO a  " +
            "where file_state='N'  [:parentFolder | and parent_folder=:parentFolder] " +
            "[:libraryId | and library_id=:libraryId]  [:fileName | and file_name=:fileName]" +
            "group by FILE_NAME,parent_folder,library_id) a " +
            "join file_info b on a.file_name=b.file_name and a.create_time=b.create_time " +
            "and a.library_id=b.library_id and a.parent_folder=b.parent_folder " +
            "join FILE_STORE_INFO c on b.FILE_MD5=c.FILE_MD5 left join file_favorite d on b.file_id=d.file_id [:favoriteUser | and d.favorite_user=:favoriteUser] "+
            "where 1=1  [:fileName | and b.file_name=:fileName]";
        QueryAndNamedParams qap = QueryUtils.translateQuery( sqlsen, searchColumn);
        List<Object[]> objects =  DatabaseOptUtils.listObjectsByNamedSql(this,
            qap.getQuery(), qap.getParams());

        List<FileShowInfo> files = new ArrayList<>();
        if(objects !=null){
            for(Object[] objs:objects){
                FileShowInfo file = new FileShowInfo();
                file.setCatalogType("p");
                file.setFileType(StringBaseOpt.objectToString(objs[8]));
                file.setFileName(StringBaseOpt.objectToString(objs[0]));
                file.setAccessToken(StringBaseOpt.objectToString(objs[1]));
                file.setVersions(NumberBaseOpt.castObjectToInteger(objs[2]));
                file.setEncrypt(StringUtils.equals(StringBaseOpt.objectToString(objs[3]),"D"));
                file.setCreateTime(DatetimeOpt.castObjectToDate(objs[4]));
                file.setFileSize(NumberBaseOpt.castObjectToLong(objs[5]));
                file.setFileShowPath(StringBaseOpt.objectToString(objs[6]));
                file.setFavoriteId(StringBaseOpt.objectToString(objs[7]));
                file.setDownloadTimes(NumberBaseOpt.castObjectToInteger(objs[9]));
                file.setOwnerName(CodeRepositoryUtil.getUserName(topUnit, StringBaseOpt.objectToString(objs[10])));
                files.add(file);
            }
        }
        return files;

    }
    public List<FileShowInfo> listUserFiles(String userCode, String fileShowPath) {
        List<Object[]> objects = null;
        if (StringUtils.isBlank(fileShowPath) || StringUtils.equals(fileShowPath,".")) {
            String sqlsen = "select a.FILE_NAME, max(a.FILE_ID) as FILE_ID, " +
                    "count(1) as FILE_SUM, min(a.ENCRYPT_TYPE) as ENCRYPT_TYPE, " +
                    "max(a.CREATE_TIME) as CREATE_TIME, max(b.FILE_SIZE) as FILE_SIZE,max(a.file_type) fileType  " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and (FILE_SHOW_PATH is null or FILE_SHOW_PATH='' or FILE_SHOW_PATH='/') " +
                    "group by FILE_NAME";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",userCode));
        }else{
            String fsp = trimFilePath(fileShowPath);//+ LocalFileManager.FILE_PATH_SPLIT;
            String sqlsen = "select a.FILE_NAME, max(a.FILE_ID) as FILE_ID, " +
                    "count(1) as FILE_SUM, min(a.ENCRYPT_TYPE) as ENCRYPT_TYPE, " +
                    "max(a.CREATE_TIME) as CREATE_TIME, max(b.FILE_SIZE) as FILE_SIZE,max(a.file_type) fileType  " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_OWNER = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and (FILE_SHOW_PATH=:fsp or FILE_SHOW_PATH=:fsp2) " +
                    "group by FILE_NAME";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",userCode,
                            "fsp",fsp,
                            "fsp2",fsp + LocalFileManager.FILE_PATH_SPLIT));
        }
        List<FileShowInfo> files = new ArrayList<>();
        if(objects !=null){
            for(Object[] objs:objects){
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath);
                file.setCatalogType("p");
                file.setFileType(StringBaseOpt.objectToString(objs[6]));
                file.setFileName(StringBaseOpt.objectToString(objs[0]));
                file.setAccessToken(StringBaseOpt.objectToString(objs[1]));
                file.setVersions(NumberBaseOpt.castObjectToInteger(objs[2]));
                file.setEncrypt(StringUtils.equals(StringBaseOpt.objectToString(objs[3]),"D"));
                if(objs[4] instanceof java.util.Date ) {
                    file.setCreateTime((Date)objs[4]);
                }
                file.setFileSize(NumberBaseOpt.castObjectToLong(objs[5]));

                files.add(file);
            }
        }
        return files;
    }

    public List<FileShowInfo> listUnitFiles(String unitCode, String fileShowPath) {
        List<Object[]> objects = null;
        if (StringUtils.isBlank(fileShowPath) || StringUtils.equals(fileShowPath,"/")) {
            String sqlsen = "select a.FILE_NAME, max(a.FILE_ID) as FILE_ID ," +
                    "count(1) as FILE_SUM, min(a.ENCRYPT_TYPE) as ENCRYPT_TYPE, " +
                    "max(a.CREATE_TIME) as CREATE_TIME, max(b.FILE_SIZE) as FILE_SIZE " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and (FILE_SHOW_PATH is null or FILE_SHOW_PATH='' or FILE_SHOW_PATH='/') " +
                    "group by FILE_NAME";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",unitCode));
        }else{
            String fsp = trimFilePath(fileShowPath);//+ LocalFileManager.FILE_PATH_SPLIT;
            String sqlsen = "select a.FILE_NAME, max(a.FILE_ID) as FILE_ID ," +
                    "count(1) as FILE_SUM, min(a.ENCRYPT_TYPE) as ENCRYPT_TYPE, " +
                    "max(a.CREATE_TIME) as CREATE_TIME, max(b.FILE_SIZE) as FILE_SIZE " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and (FILE_SHOW_PATH=:fsp or FILE_SHOW_PATH=:fsp2) " +
                    "group by FILE_NAME";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",unitCode,
                            "fsp",fsp,
                            "fsp2",fsp + LocalFileManager.FILE_PATH_SPLIT));
        }
        List<FileShowInfo> files = new ArrayList<>();
        if(objects !=null){
            for(Object[] objs:objects){
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath);
                file.setCatalogType("d");
                file.setFileType("f");
                file.setFileName(StringBaseOpt.objectToString(objs[0]));
                file.setAccessToken(StringBaseOpt.objectToString(objs[1]));
                file.setVersions(NumberBaseOpt.castObjectToInteger(objs[2]));
                file.setEncrypt(StringUtils.equals(StringBaseOpt.objectToString(objs[3]),"D"));
                if(objs[4] instanceof java.util.Date ) {
                    file.setCreateTime((Date)objs[4]);
                }
                file.setFileSize(NumberBaseOpt.castObjectToLong(objs[5]));

                files.add(file);
            }
        }
        return files;
    }

    public List<FileShowInfo> listUserFileVersions(String userCode, String fileShowPath,String fileName) {
        List<Object[]> objects = null;
        if (StringUtils.isBlank(fileShowPath) || StringUtils.equals(fileShowPath,".")) {
            String sqlsen = "select a.FILE_ID, a.ENCRYPT_TYPE, a.CREATE_TIME, b.FILE_SIZE,c.favorite_id,a.file_type,a.download_times " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                "left join file_favorite c on a.file_id=c.file_id and c.favorite_user=:favoriteUser " +
                "where FILE_OWNER = :uc and file_state='N' " +
                    "and (FILE_SHOW_PATH is null or FILE_SHOW_PATH='' or FILE_SHOW_PATH='/') " +
                    "and FILE_NAME=:fn order by a.CREATE_TIME desc";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "favoriteUser",userCode,
                            "fn",fileName));
        }else{
            fileShowPath="/"+fileShowPath;
            String sqlsen = "select a.FILE_ID, a.ENCRYPT_TYPE, a.CREATE_TIME, b.FILE_SIZE,c.favorite_id,a.file_type,a.download_times " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                "left join file_favorite c on a.file_id=c.file_id and c.favorite_user=:favoriteUser " +
                "where  file_state='N' " +
                    "and FILE_SHOW_PATH=:fsp " +
                    "and FILE_NAME=:fn order by a.CREATE_TIME desc";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "favoriteUser",userCode,
                            "fsp",fileShowPath,
                            "fn",fileName));
        }
        List<FileShowInfo> files = new ArrayList<>();
        if(objects !=null){
            for(Object[] objs:objects){
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath);
                file.setCatalogType("p");
                file.setFileType(StringBaseOpt.objectToString(objs[5]));
                file.setFileName(fileName);
                file.setAccessToken(StringBaseOpt.objectToString(objs[0]));
                file.setVersions(1);
                file.setEncrypt(StringUtils.equals(StringBaseOpt.objectToString(objs[1]),"D"));
                if(objs[2] instanceof java.util.Date ) {
                    file.setCreateTime((Date)objs[2]);
                }
                file.setFileSize(NumberBaseOpt.castObjectToLong(objs[3]));
                file.setFavoriteId(StringBaseOpt.objectToString(objs[4]));
                file.setDownloadTimes(NumberBaseOpt.castObjectToInteger(objs[6]));
                files.add(file);
            }
        }
        return files;
    }

    public List<FileShowInfo> listUnitFileVersions(String unitCode, String fileShowPath,String fileName) {
        List<Object[]> objects = null;
        if (StringUtils.isBlank(fileShowPath) || StringUtils.equals(fileShowPath,".")) {
            String sqlsen = "select a.FILE_ID, a.ENCRYPT_TYPE, a.CREATE_TIME, b.FILE_SIZE " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and (FILE_SHOW_PATH is null or FILE_SHOW_PATH='' or FILE_SHOW_PATH='/') " +
                    "and FILE_NAME=:fn and FILE_STATE='A'";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",unitCode,
                            "fn",fileName));
        }else{
            String sqlsen = "select a.FILE_ID, a.ENCRYPT_TYPE, a.CREATE_TIME, b.FILE_SIZE " +
                    "from FILE_INFO a join FILE_STORE_INFO b on a.FILE_MD5=b.FILE_MD5 " +
                    "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' " +
                    "and FILE_SHOW_PATH=:fsp " +
                    "and FILE_NAME=:fn and FILE_STATE='A'";
            objects = (List<Object[]>)DatabaseOptUtils.listObjectsByNamedSql(this,
                    sqlsen, CollectionsOpt.createHashMap(
                            "uc",unitCode,
                            "fsp",fileShowPath,
                            "fn",fileName));
        }


        List<FileShowInfo> files = new ArrayList<>();
        if(objects !=null){
            for(Object[] objs:objects){
                FileShowInfo file = new FileShowInfo();
                file.setFileShowPath(fileShowPath);
                file.setCatalogType("d");
                file.setFileType("f");
                file.setFileName(fileName);
                file.setAccessToken(StringBaseOpt.objectToString(objs[0]));
                file.setVersions(1);
                file.setEncrypt(StringUtils.equals(StringBaseOpt.objectToString(objs[1]),"D"));
                if(objs[2] instanceof java.util.Date ) {
                    file.setCreateTime((Date)objs[2]);
                }
                file.setFileSize(NumberBaseOpt.castObjectToLong(objs[3]));

                files.add(file);
            }
        }
        return files;
    }

    public List<FileInfo> listFileStoreInfo(String libraryCode, String fileShowPath,String fileName) {
        List<FileInfo> objects = null;
        if (StringUtils.isBlank(fileShowPath) || StringUtils.equals(fileShowPath,".")) {
            String hqlsen =  "where FILE_UNIT = ? and (FILE_SHOW_PATH is null or FILE_SHOW_PATH='' or FILE_SHOW_PATH='/') " +
                    "and FILE_NAME = ? order by CREATE_TIME desc";
            objects =  this.listObjectsByFilter(hqlsen,new Object[]{libraryCode , fileName});
        }else{
            String hqlsen = "where FILE_UNIT = ? and FILE_SHOW_PATH = ? and FILE_NAME = ? order by CREATE_TIME desc";
            objects = this.listObjectsByFilter(hqlsen,new Object[]{libraryCode, fileShowPath,fileName});
        }
        return objects;
    }

    public FileInfo getListVersionFileByPath(String libraryCode, String parentFolder, String fileName) {
        List<FileInfo> objects = null;
        String hqlsen = "where FILE_UNIT = ? and parent_folder = ? and FILE_NAME = ? order by CREATE_TIME  desc";
        objects = this.listObjectsByFilter(hqlsen,new Object[]{libraryCode, parentFolder, fileName});
        return objects!=null && objects.size()>0 ? objects.get(0) : null;
    }
}

