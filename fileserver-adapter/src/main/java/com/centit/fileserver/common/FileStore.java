package com.centit.fileserver.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件的物理存储接口
 *
 */
public interface FileStore {

    /**
     * 保存文件
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    String saveFile(String sourFilePath)
            throws IOException;
    /**
     * 保存文件
     * @param is InputStream
     * @param fileMd5 String
     * @param fileSize long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    String saveFile(InputStream is, String fileMd5, long fileSize)
            throws IOException;

    /**
     * 保存文件
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileMd5 String
     * @param fileSize long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    String saveFile(String sourFilePath, String fileMd5, long fileSize)
            throws IOException;

    /**
     * 保存文件
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileMd5 加密
     * @param fileSize 文件大小
     * @param extName 文件后缀名
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    String saveFile(String sourFilePath, String fileMd5, long fileSize,String extName)
            throws IOException;

    /**
     * 检查文件是否存在，如果存在则实现秒传
     * @param fileMd5 String
     * @param fileSize long
     * @return true 文件存在 false 文件不存在
     */
    boolean checkFile(String fileMd5, long fileSize);

    /**
     * 获取文件的存储路径 url，通过这个路径 fileStroe可以获得这个文件
     * 如果不存在返回null checkFile返回为true则这个肯定存在
     * @param fileMd5 String
     * @param fileSize long
     * @return 如果不存在返回null checkFile返回为true则这个肯定存在
     */
    String getFileStoreUrl(String fileMd5, long fileSize);

    String getFileStoreUrl(String fileMd5, long fileSize,String extName);

    /**
     * @return 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     * @param fileStoreUrl  文件存储的位置URL
     */
    String getFileAccessUrl(String fileStoreUrl);

    /**
     * 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     * @param fileMd5 String
     * @param fileSize long
     * @return 文件的Access url
     */
    String getFileAccessUrl(String fileMd5, long fileSize);

    /**
     * @param fileUrl  文件的url
     * @return 文件大小
     * @throws IOException IOException
     */
    long getFileSize(String fileUrl) throws IOException;
    /**
     * 获取文件
     * @param fileUrl saveFile 返回的文件路径
     * @return InputStream
     * @throws IOException IOException
     */
    InputStream loadFileStream(String fileUrl) throws IOException;

    /**
     * 获取文件
     * @param fileMd5 String
     * @param fileSize long
     * @return InputStream
     * @throws IOException IOException
     */
    InputStream loadFileStream(String fileMd5, long fileSize) throws IOException;

    InputStream loadFileStream(String fileMd5, long fileSize,String extName) throws IOException;

    /**
     *
     * @param fileId 文件的id 或者 md5_size 或者本地文件的路径
     * @return File
     * @throws IOException io异常
     */
    File getFile(String fileId) throws IOException;
    /**
     * 删除文件
     * @param fileUrl 文件的url
     * @throws IOException IOException
     * @return true 删除成功 或者文件本来就不存在  false
     */
    boolean deleteFile(String fileUrl) throws IOException;

    /**
     * 删除文件
     * @param fileMd5 String
     * @param fileSize long
     * @throws IOException io异常
     * @return true 删除成功 或者文件本来就不存在  false
     */
    boolean deleteFile(String fileMd5, long fileSize) throws IOException;
}
