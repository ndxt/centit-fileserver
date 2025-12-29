package com.centit.fileserver.backup.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.backup.dao.DatabaseConfig;
import com.centit.fileserver.backup.dao.FileBackupInfoDao;
import com.centit.fileserver.backup.dao.FileBackupListDao;
import com.centit.fileserver.backup.po.FileBackupInfo;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

public class BackupServiceImpl {

    private FileBackupInfoDao fileBackupInfoDao;

    private FileBackupListDao fileBackupListDao;

    public void init(){
        DataSource dataSource = DatabaseConfig.createDataSource();
        fileBackupInfoDao = new FileBackupInfoDao();
        fileBackupInfoDao.setDataSource(dataSource);
        fileBackupListDao = new FileBackupListDao();
        fileBackupListDao.setDataSource(dataSource);
        fileBackupInfoDao.getJdbcTemplate().execute(DatabaseConfig::checkBackupTables);
    }

    public FileBackupInfo createFileBackupList(FileBackupInfo backupInfo){
        if(StringUtils.isNotBlank(backupInfo.getBackupId())){
            FileBackupInfo dbbkinfo = fileBackupInfoDao.getObjectById(backupInfo.getBackupId());
            if(dbbkinfo==null){
                throw new ObjectException(ObjectException.DATA_VALIDATE_ERROR, "备份记录不存在："+backupInfo.getBackupId());
            }
            if(StringUtils.isNotBlank(backupInfo.getDestPath())){
                dbbkinfo.setDestPath(backupInfo.getDestPath());
            }
            return dbbkinfo;
        } else {
            backupInfo.setBackupId(UuidOpt.getUuidAsString22());
            backupInfo.setCreateTime(DatetimeOpt.currentUtilDate());
            backupInfo.setErrorCount(0);
            backupInfo.setSuccessCount(0);

            int fc = fileBackupListDao.createBackupList(backupInfo);
            backupInfo.setFileCount(fc);
            if(fc>0)
                fileBackupInfoDao.saveNewObject(backupInfo);
            return backupInfo;
        }
    }

    public void recordCopyFile(String backupId, String fileId, String status){
        if("E".equals(status)){
            fileBackupInfoDao.increaseErrorCount(backupId);
            fileBackupListDao.markError(backupId, fileId);
        } else {
            fileBackupInfoDao.increaseSuccessCount(backupId);
            fileBackupListDao.deleteFileList(backupId, fileId);
        }
    }

    public int doBackup(FileBackupInfo backupInfo, int limitSum){
        JSONArray fileList = fileBackupListDao.getBackupList(backupInfo, limitSum);
        if(fileList==null)
            return 0;
        for(Object obj : fileList) {
            if(obj instanceof JSONObject) {
                JSONObject fileInfo = (JSONObject) obj;
                try {
                    String isTemp = fileInfo.getString("isTemp");
                    if("F".equals(isTemp)) {
                        String sourFilePath = DatabaseConfig.fileRootPath + fileInfo.getString("fileStorePath");
                        String destFilePath = backupInfo.getDestPath() + fileInfo.getString("fileStorePath");
                        FileSystemOpt.createDirect(new File(destFilePath).getParent());
                        FileSystemOpt.fileCopy(sourFilePath, destFilePath);
                        recordCopyFile(backupInfo.getBackupId(), fileInfo.getString("fileId"), "S");
                        backupInfo.setSuccessCount(backupInfo.getSuccessCount() + 1);
                    } else {
                        System.out.println("文件在临时存储去无法备份："+ fileInfo.getString("fileId"));
                        recordCopyFile(backupInfo.getBackupId(), fileInfo.getString("fileId"), "E");
                        backupInfo.setErrorCount(backupInfo.getErrorCount() + 1);
                    }
                } catch (IOException e) {
                    System.out.println("文件复制失败：" + fileInfo.getString("fileId")+ ":" +e.getMessage());
                    recordCopyFile(backupInfo.getBackupId(), fileInfo.getString("fileId"), "E");
                    backupInfo.setErrorCount(backupInfo.getErrorCount() + 1);
                }
            }
        }
        return fileList.size();
    }
}
