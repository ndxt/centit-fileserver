package com.centit.fileserver.backup.service;

import com.centit.fileserver.backup.dao.DatabaseConfig;
import com.centit.fileserver.backup.dao.FileBackupInfoDao;
import com.centit.fileserver.backup.dao.FileBackupListDao;
import com.centit.fileserver.backup.po.FileBackupInfo;
import com.centit.fileserver.dao.FileInfoDao;
import com.centit.fileserver.dao.FileStoreInfoDao;


import javax.sql.DataSource;

public class BackupServiceImpl {

    private FileInfoDao fileInfoDao;
    private FileStoreInfoDao fileStoreInfoDao;

    private FileBackupInfoDao fileBackupInfoDao;

    private FileBackupListDao fileBackupListDao;

    public void init(){
        DataSource dataSource = DatabaseConfig.createDataSource();
        fileInfoDao = new FileInfoDao();
        fileInfoDao.setDataSource(dataSource);
        fileStoreInfoDao = new FileStoreInfoDao();
        fileStoreInfoDao.setDataSource(dataSource);
        fileInfoDao.getJdbcTemplate().execute(DatabaseConfig::checkBackupTables);

        fileBackupInfoDao = new FileBackupInfoDao();
        fileBackupInfoDao.setDataSource(dataSource);
        fileBackupListDao = new FileBackupListDao();
        fileBackupListDao.setDataSource(dataSource);
    }

    public int createFileBackupList(FileBackupInfo backupInfo){

        return 0;
    }
}
