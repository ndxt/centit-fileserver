package com.centit.fileserver.utils.demo;

import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.common.SysParametersUtils;
import org.apache.commons.lang3.StringUtils;

public class FileStoreFactory {
	static public FileStore createDefaultFileStore(){
		//#文件存储方式 os：操作系统 ；oss：阿里对象服务器； hdf：分布式文件系统 
		//String fileStoreType= SysParametersUtils.getStringValue("filestore.type","os");
		return createFileStore();
	}

	static public FileStore createFileStore(/*String fileStoreType*/){
		//#文件存储方式 os：操作系统 ；oss：阿里对象服务器； hdf：分布式文件系统
		//if("os".equals(fileStoreType)){
			//return new OsFileStore(SysParametersUtils.getStringValue("os.file.base.dir"));
		//}
		String baseHome = SysParametersUtils.getStringValue("os.file.base.dir");
		if(StringUtils.isBlank(baseHome));
			baseHome= SysParametersUtils.getUploadHome();


		return new OsFileStore(baseHome);
	}
}
