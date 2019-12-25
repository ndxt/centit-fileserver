package com.centit.fileserver.client;

import com.centit.fileserver.client.po.FileInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClientAsFileStore implements FileStore {

    @Setter
    private FileClient fileClient;

    private Logger logger = LoggerFactory.getLogger(ClientAsFileStore.class);

    public ClientAsFileStore(){

    }

    /**
     * 保存文件
     *
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    @Override
    public String saveFile(String sourFilePath) throws IOException {
        try(InputStream is = new FileInputStream(new File(sourFilePath))) {
            return fileClient.storeFile(is);
        }
    }

    /**
     * 保存文件
     *
     * @param is       InputStream
     * @param fileMd5  String
     * @param fileSize long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    @Override
    public String saveFile(InputStream is, String fileMd5, long fileSize) throws IOException {
        return fileClient.storeFile(is);
    }

    /**
     * 保存文件
     *
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileMd5      String
     * @param fileSize     long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    @Override
    public String saveFile(String sourFilePath, String fileMd5, long fileSize) throws IOException {
        return saveFile(sourFilePath);
    }

    /**
     * 保存文件
     *
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileMd5      加密
     * @param fileSize     文件大小
     * @param extName      文件后缀名
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    @Override
    public String saveFile(String sourFilePath, String fileMd5, long fileSize, String extName) throws IOException {
        return saveFile(sourFilePath);
    }

    /**
     * 检查文件是否存在，如果存在则实现秒传
     *
     * @param fileMd5  String
     * @param fileSize long
     * @return true 文件存在 false 文件不存在
     */
    @Override
    public boolean checkFile(String fileMd5, long fileSize) {
        try {
            return fileClient.checkFileExists(fileMd5, fileSize);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * 获取文件的存储路径 url，通过这个路径 fileStroe可以获得这个文件
     * 如果不存在返回null checkFile返回为true则这个肯定存在
     *
     * @param fileMd5  String
     * @param fileSize long
     * @return 如果不存在返回null checkFile返回为true则这个肯定存在
     */
    @Override
    public String getFileStoreUrl(String fileMd5, long fileSize) {
        return null;
    }

    @Override
    public String getFileStoreUrl(String fileMd5, long fileSize, String extName) {
        return null;
    }

    /**
     * @param fileStoreUrl 文件存储的位置URL
     * @return 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     */
    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        return null;
    }

    /**
     * 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     *
     * @param fileMd5  String
     * @param fileSize long
     * @return 文件的Access url
     */
    @Override
    public String getFileAccessUrl(String fileMd5, long fileSize) {
        return null;
    }

    /**
     * @param fileUrl 文件的url
     * @return 文件大小
     * @throws IOException IOException
     */
    @Override
    public long getFileSize(String fileUrl) throws IOException {
        return 0;
    }

    /**
     * 获取文件
     *
     * @param fileUrl saveFile 返回的文件路径
     * @return InputStream
     * @throws IOException IOException
     */
    @Override
    public InputStream loadFileStream(String fileUrl) throws IOException {
        return new FileInputStream(getFile(fileUrl));
    }

    /**
     * 获取文件
     *
     * @param fileMd5  String
     * @param fileSize long
     * @return InputStream
     * @throws IOException IOException
     */
    @Override
    public InputStream loadFileStream(String fileMd5, long fileSize) throws IOException {
        return loadFileStream(fileMd5 +"_" + fileSize+".dat");
    }

    @Override
    public InputStream loadFileStream(String fileMd5, long fileSize, String extName) throws IOException {
        return loadFileStream(fileMd5 +"_" + fileSize+"."+extName);
    }

    /**
     * @param fileId 文件的url md5SizeExt
     * @return File
     * @throws IOException io异常
     */
    @Override
    public File getFile(String fileId) throws IOException {
        if(SystemTempFileUtils.checkMd5AndSize(fileId)){
            return fileClient.getFile(fileId);
        }
        //FileInfo fileInfo = fileClient.getFileInfo(fileUrl);
        String filePath = SystemTempFileUtils.getTempFilePath(fileId);
        fileClient.downloadFile(fileId, filePath);
        return new File(filePath);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件的url
     * @return true 删除成功 或者文件本来就不存在  false
     * @throws IOException IOException
     */
    @Override
    public boolean deleteFile(String fileUrl) throws IOException {
        return false;
    }

    /**
     * 删除文件
     *
     * @param fileMd5  String
     * @param fileSize long
     * @return true 删除成功 或者文件本来就不存在  false
     * @throws IOException io异常
     */
    @Override
    public boolean deleteFile(String fileMd5, long fileSize) throws IOException {
        return false;
    }
}
