package com.centit.fileserver.backup;

import com.centit.fileserver.backup.po.FileBackupInfo;
import com.centit.fileserver.backup.service.BackupServiceImpl;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringRegularOpt;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BackupApplication {

    public static Map<String, String> parseArgs(String[] args) {
        if(args==null || args.length==0)
            return null;
        Map<String, String> argMap = new HashMap<>();
        String key="noname", value;
        boolean getValue = false;
        for(String s : args){
            if(getValue){
                value = StringRegularOpt.trimString(s);
                argMap.put(key, value);
                key="noname";
                getValue = false;
            }else{
                if("=".equals(s)){
                    getValue = true;
                } else {
                    if (!"noname".equals(key)) {
                        argMap.put(key, "true");
                    }
                    int p = s.indexOf('=');
                    if (p > 0) {
                        key = StringRegularOpt.trimString(s.substring(0, p));
                        value = StringRegularOpt.trimString(s.substring(p + 1));
                        argMap.put(key, value);
                        key = "noname";
                    } else {
                        key = StringRegularOpt.trimString(s);
                    }
                }
            }
        }
        return argMap;
    }
    public static FileBackupInfo mapToBackup(Map<String, String> args) {
        FileBackupInfo info = new FileBackupInfo();
        info.setBackupId(args.get("backupId"));
        info.setOsId(args.get("osId"));
        String destPath = args.get("destPath");
        if(StringUtils.isNotBlank(destPath)) {
            if(!destPath.endsWith("/") && !destPath.endsWith("\\")){
                destPath = destPath + File.pathSeparator;
            }
            info.setDestPath(destPath);
        }
        String beginTime = args.get("beginTime");
        if(StringUtils.isNotBlank(beginTime)) {
            Date time = DatetimeOpt.smartPraseDate(beginTime);
            if(time !=null){
                info.setBeginTime(DatetimeOpt.truncateToDay(time));
            }
        }
        String endTime = args.get("endTime");
        if(StringUtils.isNotBlank(endTime)) {
            Date time = DatetimeOpt.smartPraseDate(endTime);
            if(time !=null){
                info.setEndTime(DatetimeOpt.addDays(DatetimeOpt.truncateToDay(time),1));
            }
        }
        return info;
    }

    public static void main(String[] args) {
        boolean haveArgs = true;

        if(args==null) {
            haveArgs = false;
        }
        FileBackupInfo backupInfo = mapToBackup(parseArgs(args));
        if(StringUtils.isBlank(backupInfo.getBackupId()) && StringUtils.isBlank(backupInfo.getDestPath())) {
            haveArgs = false;
        }
        if(!haveArgs) { // pring help
            System.out.println("Usage: java -jar backup.jar backupId=<backupId> destPath=<destPath> osId=<osId> beginTime=<beginTime> endTime=<endTime>");
            System.out.println();
            System.out.println("backupId: 备份ID，继续上次没有完成的备份");
            System.out.println("osId: 应用ID，备份指定应用的附件");
            System.out.println("beginTime: 新增附件的时间，从指定时间开始备份");
            System.out.println("endTime: 新增附件的时间，备份指定时间前的文件");
            return;
        }
        System.out.println("正在准备备份，创建备份列表，可能需要几分钟，请耐心等待......");
        BackupServiceImpl backupService = new BackupServiceImpl();
        backupService.init();
        backupService.createFileBackupList(backupInfo);
        System.out.println("开始备份，当前备份ID（backupId）为："+ backupInfo.getBackupId() +"供需要备份："+backupInfo.getFileCount() +"个文件");
        int filseCount = 0;
        do{
            filseCount = backupService.doBackup(backupInfo, 300);
            System.out.println("备份进度，成功："+ backupInfo.getSuccessCount() +"失败："+ backupInfo.getErrorCount()
                + "剩余："+ (backupInfo.getFileCount()-backupInfo.getSuccessCount()-backupInfo.getErrorCount()));
        }while(filseCount == 300);
        System.out.println("备份完成，当前备份ID（backupId）为："+ backupInfo.getBackupId() +"备份成功："+ backupInfo.getSuccessCount()
            +"个文件，备份失败："+ backupInfo.getErrorCount() +"个文件。");
    }
}
