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
     * @param is InputStream
     * @param fileInfo 文件信息
     * @param fileSize long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    String saveFile(InputStream is, FileBaseInfo fileInfo, long fileSize)
            throws IOException;

    /**
     * 保存文件
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileInfo 文件信息
     * @param fileSize long
     * @return 文件的存储路径 fileStoreUrl
     * @throws IOException io异常
     */
    String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize)
            throws IOException;

    /**
     * 检查文件是否存在，如果存在则实现秒传
     * @param fileStoreUrl  文件存储的位置URL
     * @return true 文件存在 false 文件不存在
     */
    boolean checkFile(String fileStoreUrl);

    /**
     * 获取文件的存储路径 url，通过这个路径 fileStroe可以获得这个文件
     * 如果不存在返回null checkFile返回为true则这个肯定存在
     * @param fileInfo 文件信息
     * @param fileSize long 文件的大小
     * @return 如果不存在返回null checkFile返回为true则这个肯定存在
     */
    String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize);


    /**
     * @return 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     * @param fileStoreUrl  文件存储的位置URL
     */
    @Deprecated
    String getFileAccessUrl(String fileStoreUrl);


    /**
     * @param fileStoreUrl  文件的url
     * @return 文件大小
     * @throws IOException IOException
     */
    long getFileSize(String fileStoreUrl) throws IOException;
    /**
     * 获取文件
     * @param fileStoreUrl saveFile 返回的文件路径
     * @return InputStream
     * @throws IOException IOException
     */
    InputStream loadFileStream(String fileStoreUrl) throws IOException;

    /**
     *
     * @param fileStoreUrl 或者本地文件的路径
     * @return File 文件句柄
     * @throws IOException io异常
     */
    File getFile(String fileStoreUrl) throws IOException;
    /**
     * 删除文件
     * @param fileStoreUrl 文件的url
     * @throws IOException IOException
     * @return true 删除成功 或者文件本来就不存在  false
     */
    boolean deleteFile(String fileStoreUrl) throws IOException;

}
