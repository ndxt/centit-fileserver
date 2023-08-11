package com.centit.fileserver.utils;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileSystemOpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OsFileStore implements FileStore {

    private String fileRoot;

    public OsFileStore(){

    }

    public OsFileStore(String fileRoot){
        setFileRoot(fileRoot);
    }

    public void setFileRoot(String fileRoot){
        if(fileRoot.endsWith("/") || fileRoot.endsWith("\\")) {
            this.fileRoot = fileRoot;
        } else {
            this.fileRoot = fileRoot + File.separatorChar;
        }
    }

    public String calcFilePath(String fileStoreUrl) {
        if (fileStoreUrl.startsWith("/") || fileStoreUrl.indexOf(':')>0) {
            return fileStoreUrl;
        }
        return fileRoot+fileStoreUrl;
    }

    /**
     *
     * @param fileInfo 文件信息
     * @param fileSize long 文件的大小 不再使用
     * @return 文件的实际存储路径
     */
    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        String fileMd5 = fileInfo.getFileMd5();
        String pathname = String.valueOf(fileMd5.charAt(0))
            + File.separatorChar + fileMd5.charAt(1)
            + File.separatorChar + fileMd5.charAt(2);
        FileSystemOpt.createDirect(calcFilePath(pathname));
        return pathname + File.separatorChar + fileMd5  + ".dat";
    }

    @Override
    public String saveFile(FileBaseInfo fileInfo, long fileSize, InputStream is)
        throws IOException {
        String fileStroeUrl = matchFileStoreUrl(fileInfo, fileSize);
        String filePath = calcFilePath(fileStroeUrl);
        FileSystemOpt.createDirect(new File(filePath).getParent());
        FileIOOpt.writeInputStreamToFile(is, filePath);

        /*if(!FileUploadUtils.checkFileCompleted(filePath, fileMd5)){
            FileSystemOpt.deleteFile(filePath);
            throw new IOException("文件MD5校验出错："+fileMd5);
        }*/
        return fileStroeUrl;
    }

    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException {
        String filePath = matchFileStoreUrl(fileInfo, fileSize);
        FileSystemOpt.createDirect(new File(calcFilePath(filePath)).getParent());
        FileSystemOpt.fileCopy(sourFilePath, calcFilePath(filePath));
        return filePath;
    }

    /**
     * 检查文件是否存在，如果存在则实现秒传
     *
     * @param fileStoreUrl 文件存储的位置URL
     * @return true 文件存在 false 文件不存在
     */
    @Override
    public boolean checkFile(String fileStoreUrl) {
        return FileSystemOpt.existFile(calcFilePath(fileStoreUrl));
    }

    @Override
    public long getFileSize(String fileStoreUrl) throws IOException {
        File f = new File(calcFilePath(fileStoreUrl));
        return f.length();
    }

    @Override
    public InputStream loadFileStream(String fileStoreUrl) throws IOException {
        if (FileSystemOpt.existFile(calcFilePath(fileStoreUrl))) {
            return new FileInputStream(new File(calcFilePath(fileStoreUrl)));
        }
        throw new ObjectException(calcFilePath(fileStoreUrl) + "无此文件");
    }

    @Override
    public File getFile(String fileStoreUrl) throws IOException {
        return new File(calcFilePath(fileStoreUrl));
    }

    @Override
    public boolean deleteFile(String fileUrl) throws IOException {
        return FileSystemOpt.deleteFile(calcFilePath(fileUrl));
    }

    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        //TODO 这里应该返回一个相对文件服务器的url，
        //因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
        return fileStoreUrl;
    }
}
