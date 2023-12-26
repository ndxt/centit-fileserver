package com.centit.fileserver.backup.dao;

import com.centit.fileserver.backup.po.FileBackupInfo;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;

public class FileBackupInfoDao extends BaseDaoImpl<FileBackupInfo, String> {

    public void increaseErrorCount(String backupId){
        DatabaseOptUtils.doExecuteSql(this,
        "update FILE_BACKUP_INFO set ERROR_COUNT = ERROR_COUNT+ 1 where BACKUP_ID = '"+backupId+"'");
    }

    public void increaseSuccessCount(String backupId){
        DatabaseOptUtils.doExecuteSql(this,
            "update FILE_BACKUP_INFO set ERROR_COUNT = ERROR_COUNT+ 1 where BACKUP_ID = '"+backupId+"'");
    }
}
