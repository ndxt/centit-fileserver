package com.centit.fileserver.demo.fileserverclient;

import com.alibaba.fastjson.JSON;
import org.apache.http.impl.client.CloseableHttpClient;

import com.centit.fileserver.client.DefaultFileClient;
import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileStoreInfo;
import com.centit.framework.appclient.AppSession;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestFileClient {

	private static Logger logger = LoggerFactory.getLogger(TestFileClient.class);

	private static AppSession appSession;
	private static DefaultFileClient fileClient;
	private static final String fileId= "402805b85779c1b2015779c22ac30000";
	//http://codefanbook:8180/product-uploader/service/download/pfile/402805b85779c1b2015779c22ac30000
		
	public static void init(){
		appSession = new AppSession("http://codefanpc:8180/product-file",false,"u0000000","000000");
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

	public static void testUploadFileRange() {
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
			FileStoreInfo ftemp = null;
			try {
				ftemp = fileClient.uploadFileRange(fsi, file, upload, 102400);
			}catch (Exception e){
				logger.error(e.getMessage(), e);
			}
			upload = ftemp.getFileSize();
			System.out.println(JSON.toJSONString(ftemp));
		}
	}

	public static void testUploadFile() {
		FileStoreInfo fsi = new FileStoreInfo();
		fsi.setOsId("FILE_SVR");
		fsi.setOptId("LOCAL_FILE");
		fsi.setFileName("server-productsvr.cer");
		fsi.setFileStorePath("codefan/temp");
		fsi.setFileDesc("文件存储信息已被修改！"+DatetimeOpt.currentDatetime());
        try {
            fsi = fileClient.uploadFile(fsi, new File("/home/codefan/temp/server-productsvr.cer"));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
		System.out.println(JSON.toJSONString(fsi));
	}

	public static void testGetAccessToken() {
		FileAccessLog aacessLog = new FileAccessLog();
		aacessLog.setFileId(fileId);
		aacessLog.setAccessRight("A");
		aacessLog.setAccessUsercode("u000001");
		aacessLog.setAccessUsename("测试管理员");
		aacessLog.setTokenExpireTime(DatetimeOpt.addMinutes(DatetimeOpt.currentUtilDate(), 1440));
        try {
            System.out.println(fileClient.getFileUrl(aacessLog));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
	}
	
	public static void testUpdateFileInfo() {
	    try {
            FileStoreInfo fsi = fileClient.getFileStoreInfo(fileId);
            fsi.setFileDesc("文件存储信息已被修改！" + DatetimeOpt.currentDatetime());
            fileClient.updateFileStoreInfo(fsi);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
		System.out.println("down!");
	}
	
	public static void testDownloadFileInfo() {
        CloseableHttpClient httpClient = null;
	    try {
            httpClient = fileClient.getHttpClient();
            fileClient.downloadFile(httpClient, fileId, "D:\\Projects\\RunData\\temp\\download.zip");
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
		fileClient.releaseHttpClient(httpClient);
		System.out.println("down!");
	}
}
