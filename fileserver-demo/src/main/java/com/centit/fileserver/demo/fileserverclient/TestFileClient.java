package com.centit.fileserver.demo.fileserverclient;

import com.alibaba.fastjson.JSON;
import com.centit.fileserver.client.FileClientImpl;
import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileInfo;
import com.centit.framework.appclient.AppSession;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

//@Service
public class TestFileClient {

    private static Logger logger = LoggerFactory.getLogger(TestFileClient.class);

    private static AppSession appSession;
    private static FileClientImpl fileClient;
    private static final String fileId= "402805b85779c1b2015779c22ac30000";
    //http://codefanbook:8180/product-uploader/service/download/pfile/402805b85779c1b2015779c22ac30000

    public static void init(){
        appSession = new AppSession("http://codefanpc:8180/product-file",false,"u0000000","000000");
        fileClient = new FileClientImpl();
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
        FileInfo fi = new FileInfo();
        fi.setOsId("FILE_SVR");
        fi.setOptId("LOCAL_FILE");
        fi.setFileName("node-v6.9.5-linux-x64.tar.xz");
//        fsi.setFileStorePath("codefan/temp");
        fi.setFileOwner("u0000000");
        fi.setFileDesc("测试文件断点上传！"+DatetimeOpt.currentDatetime());
        File file = new File("/home/codefan/node-v6.9.5-linux-x64.tar.xz");
        long fileLen = file.length();
        long upload = 0;

        while(upload < fileLen) {
            FileInfo ftemp = null;
            try {
                ftemp = fileClient.uploadFileRange(fi, file, upload, 102400);
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
            if(ftemp != null) {
//                upload = ftemp.getFileSize();
            }
            System.out.println(JSON.toJSONString(ftemp));
        }
    }

    public static void testUploadFile() {
        FileInfo fi = new FileInfo();
        fi.setOsId("FILE_SVR");
        fi.setOptId("LOCAL_FILE");
        fi.setFileName("server-productsvr.cer");
//        fi.setFileStorePath("codefan/temp");
        fi.setFileDesc("文件存储信息已被修改！"+DatetimeOpt.currentDatetime());
        try {
            fi = fileClient.uploadFile(fi, new File("/home/codefan/temp/server-productsvr.cer"));
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        System.out.println(JSON.toJSONString(fi));
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
            FileInfo fi = fileClient.getFileInfo(fileId);
            fi.setFileDesc("文件存储信息已被修改！" + DatetimeOpt.currentDatetime());
            fileClient.updateFileInfo(fi);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        System.out.println("down!");
    }

    public static void testDownloadFileInfo() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = fileClient.allocHttpClient();
            fileClient.downloadFile(httpClient, fileId, "D:\\Projects\\RunData\\temp\\download.zip");
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
        fileClient.releaseHttpClient(httpClient);
        System.out.println("down!");
    }
}
