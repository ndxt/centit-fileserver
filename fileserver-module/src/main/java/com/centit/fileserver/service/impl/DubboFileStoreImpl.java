package com.centit.fileserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.FileOptTaskExecutor;
import com.centit.fileserver.utils.OsFileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service("dubboFileStore")
public class DubboFileStoreImpl implements FileStore {
    protected Logger logger = LoggerFactory.getLogger(DubboFileStoreImpl.class);


    @Autowired
    protected FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    FileOptTaskExecutor fileOptTaskExecutor;

    @Autowired
    OsFileStore osFileStore;

    @Autowired(required = false)
    protected ESIndexer documentIndexer;

    @Override
    public String saveFile(FileBaseInfo fileBaseInfo, long fileSize,InputStream is){
        FileInfo fileInfo = new FileInfo();
        fileInfo.copy(fileBaseInfo);
        Map<String, Object> pretreatInfo = JSON.parseObject(JSON.toJSONString(fileInfo), Map.class);
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
        try {
            // 整体上传清除 残留文件
            if (FileSystemOpt.existFile(tempFilePath)) {// 临时文件已存在
                FileSystemOpt.deleteFile(tempFilePath);
            }
            fileSize = FileIOOpt.writeInputStreamToFile(is, tempFilePath);
            File tempFile = new File(tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(tempFile);
            boolean isValid = fileSize != 0;
            String renamePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
            tempFile.renameTo(new File(renamePath));
            if (isValid && !StringUtils.isBlank(fileInfo.getFileName())) {
                fileInfo.setFileMd5(fileMd5);
                String fileName = fileInfo.getFileName();
                if (!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(fileName))) {
                    fileName = new String(fileName.getBytes("iso-8859-1"), "utf-8");
                }
                fileInfo.setFileName(fileName);
                fileInfo.setFileMd5(fileMd5);
                FileInfo dbFile  = fileInfoManager.getDuplicateFile(fileInfo);
                if(dbFile == null) {
                    fileInfoManager.saveNewObject(fileInfo);
                    String fileId = fileInfo.getFileId();
                    try {
                        // 先保存一个 临时文件； 如果文件已经存在是不会保存的
                        fileStoreInfoManager.saveTempFileInfo(fileInfo,SystemTempFileUtils.getTempFilePath(fileMd5, fileSize), fileSize);
                        fileOptTaskExecutor.addOptTask(fileInfo, fileSize, pretreatInfo);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    JSONObject jsonObject = UploadDownloadUtils.makeRangeUploadCompleteJson(fileMd5, fileSize, fileInfo.getFileName(), fileId);
                    return jsonObject.getJSONObject("data").getString("fileId");
                }else {
                    JSONObject jsonObject = UploadDownloadUtils.makeRangeUploadCompleteJson(fileMd5, fileSize, fileInfo.getFileName(), dbFile.getFileId());
                    return jsonObject.getJSONObject("data").getString("fileId");
                }
            } else {
                FileSystemOpt.deleteFile(tempFilePath);
                return "文件上传出错，fileName参数必须传，如果传了token和size参数请检查是否正确，并确认选择的文件！";
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return "文件保存失败！";
    }

    //请调用另一个实现方法
    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize){
        throw new ObjectException("This function is not been implemented. Please call another implementation method saveFile() ") ;
    }

    @Override
    public boolean checkFile(String fileId) {
        if(StringUtils.isNotBlank(fileId)){
            FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
            if(fileInfo==null) {
                return false;
            }
            FileStoreInfo storeInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            if(storeInfo==null) return false;
            return osFileStore.checkFile(storeInfo.getFileStorePath());
        }
        return false;
    }

    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        String fileMd5 = fileInfo.getFileMd5();
        String pathname = String.valueOf(fileMd5.charAt(0))
            + File.separatorChar + fileMd5.charAt(1)
            + File.separatorChar + fileMd5.charAt(2);
        FileSystemOpt.createDirect(osFileStore.getFileRoot() + pathname);
        return pathname + File.separatorChar + fileMd5 +"_"+fileSize+".dat";
    }

    /**
     * 弃用
     * @param fileStoreUrl  文件存储的位置URL
     * @return
     */
    @Override
    public String getFileAccessUrl(String fileStoreUrl) {
        return null;
    }

    @Override
    public long getFileSize(String fileId){
        try {
            FileBaseInfo fi = this.getFileInfo(fileId);
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fi.getFileMd5());
            return fileStoreInfo != null ? fileStoreInfo.getFileSize() : -1L;
        } catch (Exception e) {
            return -2;
        }
    }

    @Override
    public InputStream loadFileStream(String fileId) throws IOException {
        throw new ObjectException("This function is not been implemented , Please call method getFile()") ;
    }

    /**
     * @param fileId
     * @return
     * @throws IOException
     */
    @Override
    public File getFile(String fileId) throws IOException {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        File file = fileStoreInfo.getIsTemp() ? new File(fileStoreInfo.getFileStorePath()) : osFileStore.getFile(fileStoreInfo.getFileStorePath());
        return file;
    }

    @Override
    public boolean deleteFile(String fileId){
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo != null) {
            fileInfoManager.deleteObjectById(fileId);
            if(documentIndexer != null){
                return documentIndexer.deleteDocument(fileId);
            }
            return true;
        }
        return false;
    }

    @Override
    public FileBaseInfo getFileInfo(String fileId) {
        FileInfo objectById = fileInfoManager.getObjectById(fileId);
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(objectById,fileInfo);
        return fileInfo;
    }
}
