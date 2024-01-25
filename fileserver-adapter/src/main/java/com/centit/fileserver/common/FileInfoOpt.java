package com.centit.fileserver.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件的物理存储接口
 * @author codefan@sina.com
 */
public interface FileInfoOpt {

    /**
     * 保存文件
     * @param is InputStream
     * @param fileInfo 文件信息
     * @param fileSize long
     * @return 文件信息的主键 fileId
     * @throws IOException io异常
     */
    String saveFile(FileBaseInfo fileInfo, long fileSize,InputStream is)
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
     * @param fileId  文件ID
     * @return true 文件存在 false 文件不存在
     */
    boolean checkFile(String fileId);

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
     * @param fileId  文件的ID， 和 fileStore中的 fileStoreUrl 不一样
     */
    @Deprecated
    String getFileAccessUrl(String fileId);


    /**
     * @param fileId  文件的ID， 和 fileStore中的 fileStoreUrl 不一样
     * @return 文件大小
     * @throws IOException IOException
     */
    long getFileSize(String fileId) throws IOException;
    /**
     * 获取文件
     * @param fileId  文件的ID， 和 fileStore中的 fileStoreUrl 不一样
     * @return InputStream
     * @throws IOException IOException
     */
    InputStream loadFileStream(String fileId) throws IOException;

    /**
     *
     * @param fileId  文件的ID， 和 fileStore中的 fileStoreUrl 不一样
     * @return File 文件句柄
     * @throws IOException io异常
     */
    File getFile(String fileId) throws IOException;
    /**
     * 删除文件
     * @param fileId  文件的ID， 和 fileStore中的 fileStoreUrl 不一样
     * @throws IOException IOException
     * @return true 删除成功 或者文件本来就不存在  false
     */
    boolean deleteFile(String fileId) throws IOException;


    FileBaseInfo getFileInfo(String fileId);
}
