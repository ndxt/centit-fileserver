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
    public String getFileRoot(){
        /*if(fileRoot==null)
            return SysParametersUtils.getStringValue("fileserver.base.dir");*/
        return fileRoot;
    }

    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        String fileMd5 = fileInfo.getFileMd5();
        String pathname = String.valueOf(fileMd5.charAt(0))
                    + File.separatorChar + fileMd5.charAt(1)
                    + File.separatorChar + fileMd5.charAt(2);
        FileSystemOpt.createDirect(getFileRoot() + pathname);
        return pathname + File.separatorChar + fileMd5 +"_"+fileSize+".dat";
    }

    @Override
    public String saveFile(FileBaseInfo fileInfo, long fileSize,InputStream is)
            throws IOException {
        String fileStroeUrl =  matchFileStoreUrl(fileInfo, fileSize);
        String filePath = getFileRoot() + fileStroeUrl;
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
        FileSystemOpt.createDirect(new File(getFileRoot() + filePath).getParent());
        FileSystemOpt.fileCopy(sourFilePath,getFileRoot() + filePath);
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
        return FileSystemOpt.existFile(getFileRoot() + fileStoreUrl);
    }

    @Override
    public long getFileSize(String fileStoreUrl) throws IOException {
        File f = new File(getFileRoot() + fileStoreUrl);
        return f.length();
    }

    @Override
    public InputStream loadFileStream(String fileStoreUrl) throws IOException {
        if(FileSystemOpt.existFile(getFileRoot() + fileStoreUrl)){
            return new FileInputStream(new File(getFileRoot() + fileStoreUrl));
        }
        throw new ObjectException(getFileRoot() + fileStoreUrl+"无此文件");
    }

    @Override
    public File getFile(String fileStoreUrl) throws IOException {
        return new File(getFileRoot() + fileStoreUrl);
    }

    @Override
    public boolean deleteFile(String fileUrl) throws IOException {
        return FileSystemOpt.deleteFile(getFileRoot() + fileUrl);
    }

    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        //TODO 这里应该返回一个相对文件服务器的url，
        //因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
        return fileStoreUrl;
    }
}
