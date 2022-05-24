package com.centit.fileserver.store.plugin;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.file.FileIOOpt;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TxyunCosStore implements FileStore {

    private String region;
    private String appId;
    private String secretId;
    private String secretKey;
    private String endPoint;
    private String bucketName;

    public TxyunCosStore(){
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }


    private COSClient getCOSClient() {
        COSCredentials cosCredentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cosCredentials, clientConfig);
    }


    @Override
    public String saveFile(FileBaseInfo fileInfo, long fileSize,InputStream is) throws IOException {
        String fileStoreUrl = matchFileStoreUrl(fileInfo,fileSize);
        COSClient cosClient = getCOSClient();
        // 如何已经存在就不用再次上传
        if(!cosClient.doesObjectExist(bucketName, fileStoreUrl)) {
            ObjectMetadata metadata = cosClient.getObjectMetadata(bucketName, fileStoreUrl);
            cosClient.putObject(bucketName, fileStoreUrl, is, metadata);
        }
        return fileStoreUrl;
    }

    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException {
        /*if(!FileUploadUtils.checkFileCompleted(sourFilePath, fileMd5))
            throw new IOException("文件MD5校验出错："+fileMd5);*/
        return saveFile( fileInfo, fileSize,new FileInputStream(new File(sourFilePath)));
    }



    @Override
    public boolean checkFile(String fileStoreUrl) {
        COSClient cosClient = getCOSClient();
        return cosClient.doesObjectExist(bucketName, fileStoreUrl);
    }

    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        return fileInfo.getFileMd5();
        /*COSClient cosClient = getCOSClient();
        return cosClient.doesObjectExist(bucketName,
            fileInfo.getFileMd5()) ? fileInfo.getFileMd5() : null;*/
    }


    @Override
    public long getFileSize(String fileStoreUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        ObjectMetadata om = cosClient.getObjectMetadata(bucketName, fileStoreUrl);
        return om.getContentLength();
    }

    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        //TODO 这里应该返回一个相对文件服务器的url，
        //因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
        return fileStoreUrl;
    }

    @Override
    public InputStream loadFileStream(String fileStoreUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        COSObject oobj = cosClient.getObject(bucketName, fileStoreUrl);
        if(oobj==null)
            return null;
        return oobj.getObjectContent();
    }



    @Override
    public File getFile(String fileStoreUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        COSObject oobj = cosClient.getObject(bucketName, fileStoreUrl);
        if(oobj==null)
            return null;
        File file = new File(SystemTempFileUtils.getTempFilePath(fileStoreUrl));
        FileIOOpt.writeInputStreamToFile(oobj.getObjectContent(), file);
        return file;
    }

    @Override
    public boolean deleteFile(String fileStoreUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        cosClient.deleteObject(bucketName, fileStoreUrl);
        return true;
    }

    @Override
    public FileBaseInfo getFileInfo(String fileId) {
        return null;
    }

}
