package com.centit.fileserver.utils.demo;

import com.centit.framework.common.SysParametersUtils;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.file.FileMD5Maker;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;


public class SystemTempFileUtils {
	
	public static String getTempFilePath(String fileMd5, long size){
		return SysParametersUtils.getTempHome()
					+ File.separatorChar + fileMd5 +"_"+size+".tmp";
	}
	
	public static String getTempDirectory(){
		return SysParametersUtils.getTempHome()
					+ File.separatorChar ;
	}
	
	public static String getRandomTempFilePath(){
		return SysParametersUtils.getTempHome()
					+ File.separatorChar + UuidOpt.getUuidAsString32() +".tmp";
	}
	
	public static long checkTempFileSize(String filePath){
		File f = new File(filePath);
		if(!f.exists()) return 0;
		return f.length();
	}
	
	public static boolean checkFileCompleted(String filePath, String fileMd5){
		try {
			return StringUtils.equals(fileMd5,
					FileMD5Maker.makeFileMD5(new File(filePath)));
		} catch (IOException e) {
			return false;
		}
	}

}