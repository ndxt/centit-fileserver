package com.centit.fileserver.store.plugin;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;

import java.io.File;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }


    private COSClient getCOSClient() {
        COSCredentials cosCredentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cosCredentials, clientConfig);
    }

    private String matchFileToStoreUrl(String fileMd5, long fileSize){
        String pathname = fileMd5.charAt(0)
                    + "/"+ fileMd5.charAt(1)
                    + "/"+ fileMd5.charAt(2);
        return pathname +"/" + fileMd5 +"_"+fileSize+".dat";
    }

    private String matchFileToStoreUrl(String fileMd5, long fileSize,String extName){
        String pathname = fileMd5.charAt(0)
                + "/"+ fileMd5.charAt(1)
                + "/"+ fileMd5.charAt(2);
        return pathname +"/" + fileMd5 +"_"+fileSize+"."+extName;
    }

    public String saveFileByMd5(String sourFilePath, String fileMd5, long fileSize)
            throws IOException {
        String filePath =  matchFileToStoreUrl(fileMd5,fileSize);

        COSClient cosClient = getCOSClient();
        cosClient.putObject(bucketName, filePath, new File(sourFilePath));

        return filePath;
    }

    @Override
    public String saveFile(String sourFilePath) throws IOException {
        File file = new File(sourFilePath);
        String fileMd5 = FileMD5Maker.makeFileMD5(file);
        long fileSize = file.length();
        return saveFileByMd5(sourFilePath, fileMd5, fileSize);
    }

    @Override
    public String saveFile(InputStream is, String fileMd5, long fileSize) throws IOException {
        String fileStroeUrl =  matchFileToStoreUrl(fileMd5,fileSize);
        COSClient cosClient = getCOSClient();
        ObjectMetadata metadata = cosClient.getObjectMetadata(bucketName, fileStroeUrl);

        cosClient.putObject(bucketName, fileStroeUrl, is, metadata);
        return fileStroeUrl;
    }

    @Override
    public String saveFile(String sourFilePath, String fileMd5, long fileSize) throws IOException {
        /*if(!FileUploadUtils.checkFileCompleted(sourFilePath, fileMd5))
            throw new IOException("文件MD5校验出错："+fileMd5);*/
        return saveFileByMd5(sourFilePath, fileMd5, fileSize);
    }

    @Override
    public String saveFile(String sourFilePath, String fileMd5, long fileSize, String extName) throws IOException {
        String filePath =  matchFileToStoreUrl(fileMd5,fileSize,extName);
        COSClient cosClient = getCOSClient();
        cosClient.putObject(bucketName, filePath, new File(sourFilePath));
        return filePath;
    }

    @Override
    public boolean checkFile(String fileMd5, long fileSize) {
        String fileStroeUrl =  matchFileToStoreUrl(fileMd5,fileSize);
        COSClient cosClient = getCOSClient();

        return cosClient.doesObjectExist(bucketName, fileStroeUrl);
    }

    @Override
    public String getFileStoreUrl(String fileMd5, long fileSize) {
        String fileUrl = matchFileToStoreUrl(fileMd5,fileSize);
        COSClient cosClient = getCOSClient();
        return cosClient.doesObjectExist(bucketName, fileUrl) ? fileUrl : null;
    }

    @Override
    public String getFileStoreUrl(String fileMd5, long fileSize, String extName) {
        String fileUrl = matchFileToStoreUrl(fileMd5,fileSize,extName);
        COSClient cosClient = getCOSClient();
        return cosClient.doesObjectExist(bucketName, fileUrl) ? fileUrl : null;
    }

    @Override
    public long getFileSize(String fileUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        ObjectMetadata om = cosClient.getObjectMetadata(bucketName, fileUrl);
        return om.getContentLength();
    }

    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        //TODO 这里应该返回一个相对文件服务器的url，
        //因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
        return fileStoreUrl;
    }

    @Override
    public InputStream loadFileStream(String fileUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        COSObject oobj = cosClient.getObject(bucketName, fileUrl);
        if(oobj==null)
            return null;
        return oobj.getObjectContent();
    }

    @Override
    public InputStream loadFileStream(String fileMd5, long fileSize) throws IOException {
        return  loadFileStream(matchFileToStoreUrl(fileMd5,fileSize));
    }

    @Override
    public InputStream loadFileStream(String fileMd5, long fileSize, String extName) throws IOException {
        return  loadFileStream(matchFileToStoreUrl(fileMd5,fileSize,extName));
    }

    @Override
    public File getFile(String fileUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        COSObject oobj = cosClient.getObject(bucketName, fileUrl);
        if(oobj==null)
            return null;
        File file = new File( SystemTempFileUtils.getRandomTempFilePath());
        FileIOOpt.writeInputStreamToFile( oobj.getObjectContent(), file);
        return file;
    }

    @Override
    public boolean deleteFile(String fileUrl) throws IOException {
        COSClient cosClient = getCOSClient();
        cosClient.deleteObject(bucketName, fileUrl);
        return true;
    }

    @Override
    public boolean deleteFile(String fileMd5, long fileSize) throws IOException {
        String fileUrl =  matchFileToStoreUrl(fileMd5,fileSize);
        COSClient cosClient = getCOSClient();
        cosClient.deleteObject(bucketName, fileUrl);
        return true;
    }

    @Override
    public String getFileAccessUrl(String fileMd5, long fileSize) {
        return getFileAccessUrl(getFileStoreUrl(fileMd5,  fileSize));
    }
}
