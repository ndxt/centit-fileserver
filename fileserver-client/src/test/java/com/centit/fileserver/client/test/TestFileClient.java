package com.centit.fileserver.client.test;

import com.alibaba.fastjson.JSON;
import org.apache.http.impl.client.CloseableHttpClient;

import com.centit.fileserver.client.DefaultFileClient;
import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileStoreInfo;
import com.centit.framework.appclient.AppSession;
import com.centit.support.algorithm.DatetimeOpt;

import java.io.File;

public class TestFileClient {
	public static AppSession appSession;
	public static DefaultFileClient fileClient;
	public static final String fileId= "402805b85779c1b2015779c22ac30000";
	//http://codefanbook:8180/product-uploader/service/download/pfile/402805b85779c1b2015779c22ac30000
		
	public static void init(){
		appSession = new AppSession(
				"http://codefanpc:8180/product-file",false,"u0000000","000000");
		fileClient = new DefaultFileClient();
		fileClient.setAppSession(appSession);
		fileClient.setFileServerExportUrl("http://codefanpc:8180/product-file");
	}

	public static void main(String[] args) throws Exception {		
		init();
		//testGetAccessToken();
		testUploadFileRange();
		//testDownloadFileInfo();
	}

	public static void testUploadFileRange() throws Exception{
		FileStoreInfo fsi = new FileStoreInfo();
		fsi.setOsId("FILE_SVR");
		fsi.setOptId("LOCAL_FILE");
		fsi.setFileName("node-v6.9.5-linux-x64.tar.xz");
		fsi.setFileStorePath("codefan/temp");
		fsi.setFileOwner("u0000000");
		fsi.setFileDesc("测试文件断点上传！"+DatetimeOpt.currentDatetime());
		File file = new File("/home/codefan/node-v6.9.5-linux-x64.tar.xz");
		long fileLen = file.length();
		long upload = 0;

		while(upload < fileLen) {
			FileStoreInfo ftemp = fileClient.uploadFileRange(fsi,file,upload,102400);
			upload = ftemp.getFileSize();
			System.out.println(JSON.toJSONString(ftemp));
		}
	}

	public static void testUploadFile() throws Exception{
		FileStoreInfo fsi = new FileStoreInfo();
		fsi.setOsId("FILE_SVR");
		fsi.setOptId("LOCAL_FILE");
		fsi.setFileName("server-productsvr.cer");
		fsi.setFileStorePath("codefan/temp");
		fsi.setFileDesc("文件存储信息已被修改！"+DatetimeOpt.currentDatetime());
		fsi = fileClient.uploadFile(fsi,new File("/home/codefan/temp/server-productsvr.cer"));
		System.out.println(JSON.toJSONString(fsi));
	}

	public static void testGetAccessToken() throws Exception{
		FileAccessLog aacessLog = new FileAccessLog();
		aacessLog.setFileId(fileId);
		aacessLog.setAccessRight("A");
		aacessLog.setAccessUsercode("u000001");
		aacessLog.setAccessUsename("测试管理员");
		aacessLog.setTokenExpireTime(DatetimeOpt.addMinutes(DatetimeOpt.currentUtilDate(), 1440));
		System.out.println(fileClient.getFileUrl(aacessLog));
	}
	
	public static void testUpdateFileInfo() throws Exception{
		FileStoreInfo fsi = fileClient.getFileStoreInfo(fileId);
		fsi.setFileDesc("文件存储信息已被修改！"+DatetimeOpt.currentDatetime());
		fileClient.updateFileStoreInfo(fsi);
		System.out.println("down!");
	}
	
	public static void testDownloadFileInfo() throws Exception{
		CloseableHttpClient httpClient = fileClient.getHttpClient();
		fileClient.downloadFile(httpClient,fileId,"D:\\Projects\\RunData\\temp\\download.zip");
		fileClient.releaseHttpClient(httpClient);
		System.out.println("down!");
	}
}
