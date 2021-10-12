package com.centit.fileserver.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 客户端想要操作FileStore 须通过FileStoreContext 对文件进行保存，删除操作
 */
public class FileStoreContext {
    private FileStore fileStore;

    //设置具体的FileStore实现
    public FileStoreContext(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    //设置具体的FileStore实现
    public void setFileStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public String saveFile(FileBaseInfo fileInfo, long fileSize,InputStream is) throws IOException {
        return fileStore.saveFile(fileInfo, fileSize,is);
    }


    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException {
        return   fileStore.saveFile(fileInfo, fileSize,new FileInputStream(sourFilePath));
    }


    public boolean checkFile(String fileStoreUrl) {
        return fileStore.checkFile(fileStoreUrl);
    }


    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        return fileStore.matchFileStoreUrl(fileInfo,fileSize);
    }


    public String getFileAccessUrl(String fileStoreUrl) {
        return fileStore.getFileAccessUrl(fileStoreUrl);
    }


    public long getFileSize(String fileStoreUrl) throws IOException {
        return fileStore.getFileSize(fileStoreUrl);
    }


    public InputStream loadFileStream(String fileStoreUrl) throws IOException {
        return fileStore.loadFileStream(fileStoreUrl);
    }


    public File getFile(String fileStoreUrl) throws IOException {
        return fileStore.getFile(fileStoreUrl);
    }


    public boolean deleteFile(String fileStoreUrl) throws IOException {
        return fileStore.deleteFile(fileStoreUrl);
    }

    public FileInfo getFileInfo(String fileId){
        return fileStore.getFileInfo(fileId);
    };
}
