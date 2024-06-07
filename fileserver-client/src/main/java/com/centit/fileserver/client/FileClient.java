package com.centit.fileserver.client;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileInfo;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileClient {

    CloseableHttpClient allocHttpClient();

    void releaseHttpClient(CloseableHttpClient httpClient);

    /*
     * 请求下载文件
     */
    String/*文件下载url */getFileUrl(CloseableHttpClient httpClient, FileAccessLog aacessLog) throws IOException;

    String/*文件下载url */getFileUrl(FileAccessLog aacessLog) throws IOException;

    /*
        请求上传文件
     */
    String applyUploadFiles(CloseableHttpClient httpClient, int maxUploadFiles) throws IOException;

    String applyUploadFiles(int maxUploadFiles) throws IOException;

    default String applyUploadFile() throws IOException {
        return applyUploadFiles(1);
    }

    /**
     * 文件下载url
     *
     * @param fileId     文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 文件下载url
     * @throws IOException IOException
     */
    String getFileUrl(String fileId, int expireTime) throws IOException;

    /**
     * 附属文件下载url
     *
     * @param fileId     文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return String
     * @throws IOException IOException
     */
    String getAttachFileUrl(String fileId, int expireTime) throws IOException;

    /**
     * 文件下载url
     *
     * @param httpClient CloseableHttpClient
     * @param fileId     文件ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 文件下载url
     * @throws IOException IOException
     */
    String getFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws IOException;

    /**
     * 附属文件下载url
     *
     * @param httpClient CloseableHttpClient
     * @param fileId     文档ID
     * @param expireTime 失效期限  按照分钟计算
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getAttachFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws IOException;

    /**
     * 附属文件下载url 不限制时间，限制下载次数
     *
     * @param httpClient   CloseableHttpClient
     * @param fileId       文档ID
     * @param downloadTime 下载次数
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getAttachFileUrlLimitTimes(CloseableHttpClient httpClient, String fileId, int downloadTime) throws IOException;

    /**
     * 文件下载url 不限制时间，限制下载次数
     *
     * @param httpClient   CloseableHttpClient
     * @param fileId       文档ID
     * @param downloadTime 下载次数
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getFileUrlLimitTimes(CloseableHttpClient httpClient, String fileId, int downloadTime) throws IOException;

    /**
     * 附属文件下载url 不限制时间，限制下载次数
     *
     * @param fileId       文档ID
     * @param downloadTime 下载次数
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getAttachFileUrlLimitTimes(String fileId, int downloadTime) throws IOException;

    /**
     * 文件下载url 不限制时间，限制下载次数
     *
     * @param fileId       文档ID
     * @param downloadTime 下载次数
     * @return 附属文件下载url
     * @throws IOException IOException
     */
    String getFileUrlLimitTimes(String fileId, int downloadTime) throws IOException;

    FileInfo getFileInfo(CloseableHttpClient httpClient, String fileId) throws IOException;

    boolean updateFileInfo(CloseableHttpClient httpClient, FileInfo fi) throws IOException;


    FileInfo getFileInfo(String fileId) throws IOException;

    boolean updateFileInfo(FileInfo fi) throws IOException;


    FileInfo uploadFile(CloseableHttpClient httpClient, FileInfo fi, File file) throws IOException;

    FileInfo uploadFile(FileInfo fi, File file) throws IOException;

    FileInfo uploadFile(CloseableHttpClient httpClient, FileInfo fi, InputStream inputStream) throws IOException;

    FileInfo uploadFile(FileInfo fi, InputStream inputStream) throws IOException;

    long getFileRangeStart(CloseableHttpClient httpClient, String fileMd5, long fileSize) throws IOException;

    long getFileRangeStart(String fileMd5, long fileSize) throws IOException;

    long getFileRangeStart(CloseableHttpClient httpClient, File file) throws IOException;

    long getFileRangeStart(File file) throws IOException;


    FileInfo uploadFileRange(CloseableHttpClient httpClient, FileInfo fi,
                             File file, long rangeStart, long rangeSize) throws IOException;

    FileInfo uploadFileRange(FileInfo fi,
                             File file, long rangeStart, long rangeSize) throws IOException;


    void downloadFileRange(CloseableHttpClient httpClient,
                           String fileId, int offset, int lenght, String filePath) throws IOException;

    void downloadFileRange(String fileId, int offset, int lenght,
                           String filePath) throws IOException;

    void downloadFile(CloseableHttpClient httpClient,
                      String fileId, String filePath) throws IOException;

    void downloadFile(String fileId,
                      String filePath) throws IOException;

    String storeFile(InputStream file) throws IOException;

    String matchFileStoreUrl(FileInfo fi, long fileSize);

    long getFileSizeByStoreUrl(String fileStoreUrl);

    long getFileSizeByFileId(String fileId);

    void deleteFile(String fileId);

    String insertFileLibrary(FileLibraryInfo fileLibrary);

    FileLibraryInfo getFileLibrary(String topUnit, String libraryId);


}
