package com.centit.fileserver.client;

import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileStoreInfo;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.IOException;

public interface FileClient {

    CloseableHttpClient getHttpClient() ;

    void releaseHttpClient(CloseableHttpClient httpClient);

    String/*文件下载url */getFileUrl(CloseableHttpClient httpClient, FileAccessLog aacessLog) throws IOException;

    String/*文件下载url */getFileUrl(FileAccessLog aacessLog) throws IOException;

    /**
     * 文件下载url
     * @param fileId 文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 文件下载url
     * @throws IOException IOException
     */
    String getFileUrl(String fileId, int expireTime) throws IOException;

    /**
     * 附属文件下载url
     * @param fileId 文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return String
     * @throws IOException IOException
     */
    String getAttachFileUrl(String fileId, int expireTime) throws IOException;

    /**
     * 文件下载url
     * @param httpClient CloseableHttpClient
     * @param fileId 文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 文件下载url
     * @throws IOException IOException
     */
    String getFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws IOException;

    /**
     * 附属文件下载url
     * @param httpClient CloseableHttpClient
     * @param fileId 文档ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getAttachFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws IOException;

    FileStoreInfo getFileStoreInfo(CloseableHttpClient httpClient, String fileId) throws IOException;

    boolean updateFileStoreInfo(CloseableHttpClient httpClient, FileStoreInfo fsi) throws IOException;


    FileStoreInfo getFileStoreInfo(String fileId) throws IOException;

    boolean updateFileStoreInfo(FileStoreInfo fsi) throws IOException;


    FileStoreInfo uploadFile(CloseableHttpClient httpClient, FileStoreInfo fsi,File file) throws IOException;

    FileStoreInfo uploadFile(FileStoreInfo fsi,File file) throws IOException;

    long getFileRangeStart(CloseableHttpClient httpClient, String fileMd5,long fileSize) throws IOException;

    long getFileRangeStart(String fileMd5,long fileSize) throws IOException;

    long getFileRangeStart(CloseableHttpClient httpClient, File file) throws IOException;

    long getFileRangeStart(File file) throws IOException;


    FileStoreInfo uploadFileRange(CloseableHttpClient httpClient, FileStoreInfo fsi,
                                         File file,long rangeStart,long rangeSize) throws IOException;

    FileStoreInfo uploadFileRange(FileStoreInfo fsi,
                                         File file,long rangeStart,long rangeSize) throws IOException;


    void downloadFileRange(CloseableHttpClient httpClient,
                                  String fileId, int offset, int lenght, String filePath) throws IOException;

    void downloadFileRange(String fileId, int offset, int lenght,
                                  String filePath) throws IOException;

    void downloadFile(CloseableHttpClient httpClient,
                             String fileId, String filePath) throws IOException;

    void downloadFile(String fileId,
                             String filePath) throws IOException;
}
