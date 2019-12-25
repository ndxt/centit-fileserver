package com.centit.fileserver.utils;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.file.FileMD5Maker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public abstract class SystemTempFileUtils {

    private static Logger logger = LoggerFactory.getLogger(SystemTempFileUtils.class);

    private static String TEMP_FILE_DIRECTORY = null;

    public static synchronized void setTempFileDirectory(String tempFileDirectory){
        if(tempFileDirectory.endsWith("/") || tempFileDirectory.endsWith("\\")) {
            TEMP_FILE_DIRECTORY = tempFileDirectory;
        }else{
            TEMP_FILE_DIRECTORY = tempFileDirectory + "/";
        }
    }

    public static String getTempFilePath(String fileMd5, long size){
        return getTempDirectory() // SysParametersUtils.getTempHome() + File.separatorChar
                     + fileMd5 +"_"+size+".tmp";
    }

    public static String getTempDirectory(){
        if(StringUtils.isBlank(TEMP_FILE_DIRECTORY)){
            setTempFileDirectory(System.getProperty("java.io.tmpdir"));
        }
        return TEMP_FILE_DIRECTORY; // SysParametersUtils.getTempHome()
                    //+ File.separatorChar ;
    }

    public static String getTempFilePath(String fileId){
        return getTempDirectory() //SysParametersUtils.getTempHome()+ File.separatorChar
            + fileId +".tmp";
    }

    public static String getRandomTempFilePath(){
        return getTempDirectory() //SysParametersUtils.getTempHome()+ File.separatorChar
                     + UuidOpt.getUuidAsString32() +".tmp";
    }

    public static long checkTempFileSize(String filePath){
        File f = new File(filePath);
        if(!f.exists())
            return 0;
        return f.length();
    }

    public static boolean checkFileCompleted(String filePath, String fileMd5){
        try {
            return StringUtils.equals(fileMd5,
                    FileMD5Maker.makeFileMD5(new File(filePath)));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static boolean checkMd5AndSize(String md5SizeExt) {
        int len = md5SizeExt.length();
        if(len<34){
            return false;
        }
        if(md5SizeExt.charAt(32) != '_'){
            return false;
        }
        int pos = md5SizeExt.indexOf('.');
        String fileSize = pos<0? md5SizeExt.substring(33)
            : md5SizeExt.substring(33,pos);
        return StringRegularOpt.isNumber(fileSize);
    }

    public static Pair<String, Long> fetchMd5AndSize(String md5SizeExt) {
        String fileMd5 =  md5SizeExt.substring(0,32);
        int pos = md5SizeExt.indexOf('.');
        //String extName = md5SizeExt.substring(pos);
        long fileSize = pos<0? NumberBaseOpt.parseLong(md5SizeExt.substring(33),0l)
            : NumberBaseOpt.parseLong(md5SizeExt.substring(33,pos),0l);
        return Pair.of(fileMd5, fileSize);
    }
}
