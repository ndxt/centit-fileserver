package com.centit.fileserver.store.plugin;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.support.file.FileIOOpt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AliyunOssStore implements FileStore {

    private String endPoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;

    public AliyunOssStore(){

    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public String saveFile( FileBaseInfo fileInfo, long fileSize,InputStream is) throws IOException {
        String fileStroreUrl =  matchFileStoreUrl(fileInfo, fileSize);
        OSSClient ossc = new OSSClient(endPoint, accessKeyId, secretAccessKey);
        if(!ossc.doesObjectExist(bucketName, fileStroreUrl)) {
            ossc.putObject(bucketName, fileStroreUrl, is);
        }
        return fileStroreUrl;
    }


    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException {
        String fileStroreUrl =  matchFileStoreUrl(fileInfo, fileSize);
        OSSClient ossc = new OSSClient(endPoint, accessKeyId, secretAccessKey);
        if(!ossc.doesObjectExist(bucketName, fileStroreUrl)) {
            ossc.putObject(bucketName, fileStroreUrl, new File(sourFilePath));
        }
        return fileStroreUrl;
    }

    @Override
    public boolean checkFile(String fileStoreUrl) {
        OSSClient ossc = new OSSClient(endPoint, accessKeyId, secretAccessKey);
        return ossc.doesObjectExist(bucketName, fileStoreUrl);
    }

    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        return fileInfo.getFileMd5();
    }


    @Override
    public long getFileSize(String fileStoreUrl) throws IOException {
        OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
        ObjectMetadata om = ossc.getObjectMetadata(bucketName, fileStoreUrl);
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
        OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
        OSSObject oobj = ossc.getObject(bucketName, fileStoreUrl);
        if(oobj==null)
            return null;
        return oobj.getObjectContent();
    }


    @Override
    public File getFile(String fileStoreUrl) throws IOException {
        OSSClient ossc = new OSSClient(endPoint, accessKeyId, secretAccessKey);
        OSSObject oobj = ossc.getObject(bucketName, fileStoreUrl);
        if(oobj==null)
            return null;
        File file = new File(SystemTempFileUtils.getTempFilePath(fileStoreUrl));
        FileIOOpt.writeInputStreamToFile(oobj.getObjectContent(), file);
        return file;
    }

    @Override
    public boolean deleteFile(String fileStoreUrl) throws IOException {
        OSSClient ossc = new OSSClient(endPoint, accessKeyId, secretAccessKey);
        ossc.deleteObject(bucketName, fileStoreUrl);
        return true;
    }

    @Override
    public FileBaseInfo getFileInfo(String fileId) {
        return null;
    }

}
