package com.centit.fileserver.client;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileInfoOpt;
import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.common.OperateFileLibrary;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.appclient.HttpReceiveJSON;
import com.centit.framework.appclient.RestfulHttpRequest;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInfoOptClient implements FileInfoOpt, OperateFileLibrary {

    @Setter
    private FileClient fileClient;

    private Logger logger = LoggerFactory.getLogger(FileInfoOptClient.class);

    public FileInfoOptClient() {

    }

    /**
     * 保存文件
     *
     * @param is       InputStream
     * @param fileInfo 文件信息
     * @param fileSize long
     * @return 文件的存储路径
     * @throws IOException io异常
     */
    @Override
    public String saveFile( FileBaseInfo fileInfo, long fileSize,InputStream is) throws IOException {
        FileInfo f = fileClient.uploadFile( (FileInfo)fileInfo, is);
        return f != null ? f.getFileId() : "";
    }

    /**
     * 保存文件
     *
     * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
     * @param fileInfo     文件信息
     * @param fileSize     long
     * @return 文件的存储路径 fileStoreUrl
     * @throws IOException io异常
     */
    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException {
        FileInfo f = fileClient.uploadFile((FileInfo)fileInfo, new File(sourFilePath));
        return f != null ? f.getFileId() : "";
    }

    /**
     * 检查文件是否存在，如果存在则实现秒传
     *
     * @param fileId 文件的ID
     * @return true 文件存在 false 文件不存在
     */
    @Override
    public boolean checkFile(String fileId) {
        return fileClient.getFileSizeByFileId(fileId) > 0;
    }

    /**
     * 获取文件的存储路径 url，通过这个路径 fileStroe可以获得这个文件
     * 如果不存在返回null checkFile返回为true则这个肯定存在
     *
     * @param fileInfo 文件信息
     * @param fileSize long 文件的大小
     * @return 如果不存在返回null checkFile返回为true则这个肯定存在
     */
    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        return fileClient.matchFileStoreUrl((FileInfo)fileInfo, fileSize);
    }

    /**
     * @param fileId 等同于 fileStoreUrl 文件存储的位置URL
     * @return 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
     */
    @Override
    public String getFileAccessUrl(String fileId) {
        try {
            return fileClient.getFileUrl(fileId, 24 * 60);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @param fileId 文件的url
     * @return 文件大小
     * @throws IOException IOException
     */
    @Override
    public long getFileSize(String fileId) throws IOException {
        return fileClient.getFileSizeByFileId(fileId);
    }

    /**
     * 获取文件
     *
     * @param fileId saveFile 返回的 fileId
     * @return InputStream
     * @throws IOException IOException
     */
    @Override
    public InputStream loadFileStream(String fileId) throws IOException {
        return new FileInputStream(getFile(fileId));
    }


    /**
     * @param fileId 文件的url fileId
     * @return File
     * @throws IOException io异常
     */
    @Override
    public File getFile(String fileId) throws IOException {
        String filePath = SystemTempFileUtils.getTempFilePath(fileId);
        File tempFile = new File(filePath);
        if(tempFile.exists() && tempFile.isFile()){
            //判断文件是否已经在缓存中，如果已经存在就不用再次获取
            return tempFile;
        }
        fileClient.downloadFile(fileId, filePath);
        return tempFile;
    }

    /**
     * 删除文件
     *
     * @param fileId 文件的url
     * @return true 删除成功 或者文件本来就不存在  false
     * @throws IOException IOException
     */
    @Override
    public boolean deleteFile(String fileId) throws IOException {
        fileClient.deleteFile(fileId);
        return true;
    }

    @Override
    public FileBaseInfo getFileInfo(String fileId) {
        try {
            return fileClient.getFileInfo(fileId);
        } catch (IOException e) {
            logger.error("获取文件信息失败",e);
            return null;
        }
    }

    @Override
    public FileLibraryInfo insertFileLibrary(FileLibraryInfo fileLibrary) {
        HttpReceiveJSON fileLibraryInfo = HttpReceiveJSON.valueOfJson(fileClient.insertFileLibrary(fileLibrary));
        RestfulHttpRequest.checkHttpReceiveJSON(fileLibraryInfo);
        return fileLibraryInfo.getDataAsObject(FileLibraryInfo.class);
    }

    @Override
    public FileLibraryInfo getFileLibrary(String topUnit, String libraryId) {
        return fileClient.getFileLibrary(topUnit, libraryId);
    }

}
