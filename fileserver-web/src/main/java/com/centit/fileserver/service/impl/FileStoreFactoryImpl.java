package com.centit.fileserver.service.impl;

import com.centit.fileserver.fileaccess.AliyunOssStore;
import com.centit.fileserver.service.FileStoreFactory;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.framework.common.SysParametersUtils;
import org.apache.commons.lang3.StringUtils;

public class FileStoreFactoryImpl implements FileStoreFactory {

	public FileStore createDefaultFileStore(){
		//#文件存储方式 os：操作系统 ；oss：阿里对象服务器； hdf：分布式文件系统 
		String fileStoreType= SysParametersUtils.getStringValue("filestore.type","os");
		return createFileStore(fileStoreType);
	}

	 public FileStore createFileStore(String fileStoreType){
		//#文件存储方式 os：操作系统 ；oss：阿里对象服务器； hdf：分布式文件系统
		if("os".equals(fileStoreType)){

			String baseHome = SysParametersUtils.getStringValue("os.file.base.dir");
			if(StringUtils.isBlank(baseHome));
				baseHome= SysParametersUtils.getUploadHome();
			return new OsFileStore(baseHome);
		}

		if("oss".equals(fileStoreType)){//ali-oss
			AliyunOssStore fs = new AliyunOssStore();
			fs.setEndPoint(SysParametersUtils.getStringValue("oos.endPoint"));
			fs.setAccessKeyId(SysParametersUtils.getStringValue("oos.accessKeyId"));
			fs.setSecretAccessKey(SysParametersUtils.getStringValue("oos.secretAccessKey"));
			fs.setBucketName(SysParametersUtils.getStringValue("oos.bucketName"));
			return fs;
		}

		return new OsFileStore(
				SysParametersUtils.getStringValue("fileserver.base.dir"));
	}
}
