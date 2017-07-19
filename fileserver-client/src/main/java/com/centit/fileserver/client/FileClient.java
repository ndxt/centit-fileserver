package com.centit.fileserver.client;

import org.apache.http.impl.client.CloseableHttpClient;

import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileStoreInfo;

import java.io.File;

public interface FileClient {

    CloseableHttpClient getHttpClient() throws Exception;

    void releaseHttpClient(CloseableHttpClient httpClient);

    String/*文件下载url */getFileUrl(CloseableHttpClient httpClient, FileAccessLog aacessLog) throws Exception;

    String/*文件下载url */getFileUrl(FileAccessLog aacessLog) throws Exception;

    /**
     * @param fileId
     * @param expireTime 失效期限  按照分钟计算
     * @return
     * @throws Exception
     */
    String/*文件下载url */getFileUrl(String fileId, int expireTime) throws Exception;

    /**
     * @param fileId
     * @param expireTime 失效期限  按照分钟计算
     * @return
     * @throws Exception
     */
    String/*附属文件下载url */getAttachFileUrl(String fileId, int expireTime) throws Exception;

    /**
     * @param httpClient
     * @param fileId
     * @param expireTime 失效期限  按照分钟计算
     * @return
     * @throws Exception
     */
    String/*文件下载url */getFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws Exception;

    /**
     * @param httpClient
     * @param fileId
     * @param expireTime 失效期限  按照分钟计算
     * @return
     * @throws Exception
     */
    String/*附属文件下载url */getAttachFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws Exception;

    FileStoreInfo getFileStoreInfo(CloseableHttpClient httpClient, String fileId) throws Exception;

    boolean updateFileStoreInfo(CloseableHttpClient httpClient, FileStoreInfo fsi) throws Exception;


    FileStoreInfo getFileStoreInfo(String fileId) throws Exception;

    boolean updateFileStoreInfo(FileStoreInfo fsi) throws Exception;


    FileStoreInfo uploadFile(CloseableHttpClient httpClient, FileStoreInfo fsi,File file) throws Exception;

    FileStoreInfo uploadFile(FileStoreInfo fsi,File file) throws Exception;

    long getFileRangeStart(CloseableHttpClient httpClient, String fileMd5,long fileSize) throws Exception;

    long getFileRangeStart(String fileMd5,long fileSize) throws Exception;

    long getFileRangeStart(CloseableHttpClient httpClient, File file) throws Exception;

    long getFileRangeStart(File file) throws Exception;


    FileStoreInfo uploadFileRange(CloseableHttpClient httpClient, FileStoreInfo fsi,
                                         File file,long rangeStart,long rangeSize) throws Exception;

    FileStoreInfo uploadFileRange(FileStoreInfo fsi,
                                         File file,long rangeStart,long rangeSize) throws Exception;


    void downloadFileRange(CloseableHttpClient httpClient,
                                  String fileId, int offset, int lenght, String filePath) throws Exception;

    void downloadFileRange(String fileId, int offset, int lenght,
                                  String filePath) throws Exception;

    void downloadFile(CloseableHttpClient httpClient,
                             String fileId, String filePath) throws Exception;

    void downloadFile(String fileId,
                             String filePath) throws Exception;
}
