package com.centit.fileserver.backup.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.backup.po.FileBackupInfo;
import com.centit.fileserver.backup.po.FileBackupList;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.support.common.ObjectException;
import com.centit.support.database.utils.DBType;
import org.apache.commons.lang3.StringUtils;


public class FileBackupListDao extends BaseDaoImpl<FileBackupList, JSONObject> {

    public int createBackupList(FileBackupInfo backupInfo){
        StringBuilder sql = new StringBuilder("insert into FILE_BACKUP_LIST(BACKUP_ID, FILE_ID, BACKUP_STATUS)")
            .append(" select '").append(backupInfo.getBackupId())
            .append("', FILE_ID, 'I' from FILE_INFO ");
        int n=0;
        if(StringUtils.isNotBlank(backupInfo.getOsId())){
            sql.append("where library_id = :osId");
            n++;
        }
        if(backupInfo.getBeginTime() !=null){
            sql.append( n>0 ? " and" : "where ");
            sql.append(" CREATE_TIME >= :beginTime");
            n++;
        }

        if(backupInfo.getEndTime() !=null){
            sql.append( n>0 ? " and" : "where ");
            sql.append(" CREATE_TIME < :endTime");
        }

        return DatabaseOptUtils.doExecuteNamedSql(this, sql.toString(), JSONObject.from(backupInfo));
    }

    public JSONArray getBackupList(FileBackupInfo backupInfo, int limit){
        String sqlSen = "select  a.FILE_ID, c.FILE_MD5, c.FILE_STORE_PATH, c.IS_TEMP, " +
            " from FILE_BACKUP_LIST a join FILE_INFO b on a.FILE_ID=b.FILE_ID " +
            " join FILE_STORE_INFO c on b.FILE_MD5 = c.FILE_MD5" +
            " where a.BACKUP_ID = '" + backupInfo.getBackupId() +"' and a.BACKUP_STATUS='I' ";
        DBType dbType = this.getDBtype();
        switch (dbType) {
            case Oracle:
            case DM:
            case KingBase:
            case GBase:
            case Oscar:
                sqlSen = sqlSen+ " and rownum <= " +limit;
                break;
            case DB2:
                sqlSen =  limit > 1 ? sqlSen + " fetch first " + limit + " rows only" :
                    sqlSen + " fetch first 1 row only";
                break;
            case SqlServer:
            case Access:
                sqlSen = "select top " + limit +  sqlSen.substring(6);
                break;
            case MySql:
            case H2:
            case PostgreSql:
                sqlSen = sqlSen+ " limit " +limit;
                break;
            default:
                throw new ObjectException(ObjectException.ORM_METADATA_EXCEPTION,
                    "不支持的数据库类型：" + dbType);
        }
        return DatabaseOptUtils.listObjectsBySqlAsJson(this, sqlSen);
    }

    public void deleteFileList(String backupId, String fileId){
        String sqlSen = "delete from FILE_BACKUP_LIST where BACKUP_ID = '" + backupId +"' and FILE_ID = '" + fileId +"'";
        DatabaseOptUtils.doExecuteSql(this, sqlSen);
    }

    public void markError(String backupId, String fileId){
        String sqlSen = "update FILE_BACKUP_LIST set BACKUP_STATUS = 'E' where BACKUP_ID = '" + backupId +"' and FILE_ID = '" + fileId +"'";
        DatabaseOptUtils.doExecuteSql(this, sqlSen);
    }

}
