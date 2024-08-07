package com.centit.fileserver.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileInfoOpt;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.FileOptTaskExecutor;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.common.ResponseData;
import com.centit.search.document.FileDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service("fileInfoOpt")
public class FileInfoOptServerImpl implements FileInfoOpt {
    protected Logger logger = LoggerFactory.getLogger(FileInfoOptServerImpl.class);

    @Value("${fileserver.pretreatment.sync:true}")
    protected boolean pretreatmentAsSync;

    @Autowired
    protected FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    FileOptTaskExecutor fileOptTaskExecutor;

    @Autowired
    FileStore fileStore;

    @Autowired(required = false)
    private ESServerConfig esServerConfig;

    public ESIndexer fetchDocumentIndexer(){
        if(esServerConfig==null)
            return null;
        return IndexerSearcherFactory.obtainIndexer(esServerConfig, FileDocument.class);
    }

    @Override
    public String saveFile(FileBaseInfo fileBaseInfo, long fileSize, InputStream is){
        FileInfo fileInfo = new FileInfo();
        fileInfo.copy(fileBaseInfo);
        Map<String, Object> pretreatInfo = JSONObject.from(fileInfo);// JSON.parseObject(JSON.toJSONString(fileInfo), Map.class);
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
                // TODO 这个是周抄添加的，不知道是什么场景，逻辑意义应该是： 文件名非GBK编码就转换为UTF-8
                if (!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(fileName))) {
                    fileName = new String(fileName.getBytes("iso-8859-1"), "utf-8");
                }
                fileInfo.setFileSize(fileSize);
                fileInfo.setFileName(fileName);
                fileInfo.setFileMd5(fileMd5);
                if(fileInfo.getFileId()==null){
                    fileInfo.setFileId(UuidOpt.getUuidAsString());
                }

                FileInfo dbFile  = fileInfoManager.getDuplicateFile(fileInfo);
                if(dbFile == null) {
                    fileInfoManager.saveNewObject(fileInfo);
                    String fileId = fileInfo.getFileId();
                    try {
                        // 先保存一个 临时文件； 如果文件已经存在是不会保存的
                        fileStoreInfoManager.saveTempFileInfo(fileInfo, renamePath, fileSize);
                        if(pretreatmentAsSync)
                            fileOptTaskExecutor.runOptTask(fileInfo, fileInfo.getFileSize(), pretreatInfo);
                        else
                            fileOptTaskExecutor.addOptTask(fileInfo, fileSize, pretreatInfo);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    return fileId;
                }else {
                    return dbFile.getFileId();
                }
            } else {
                FileSystemOpt.deleteFile(tempFilePath);
                throw new ObjectException(ResponseData.ERROR_OPERATION,
                    "文件上传出错，fileName参数必须传，如果传了token和size参数请检查是否正确，并确认选择的文件！");
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new ObjectException(ResponseData.ERROR_OPERATION, "文件保存失败, "+e.getMessage(), e);
        }
    }

    //请调用另一个实现方法
    @Override
    public String saveFile(String sourFilePath, FileBaseInfo fileInfo, long fileSize) throws IOException{
        return saveFile(fileInfo, fileSize, new FileInputStream(sourFilePath));
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
            return fileStore.checkFile(storeInfo.getFileStorePath());
        }
        return false;
    }

    @Override
    public String matchFileStoreUrl(FileBaseInfo fileInfo, long fileSize) {
        return fileStore.matchFileStoreUrl(fileInfo, fileSize);
    }

    /**
     * 弃用
     * @param fileId  文件存储的位置URL
     * @return String
     */
    @Override
    public String getFileAccessUrl(String fileId) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        return fileStore.getFileAccessUrl(fileStoreInfo.getFileStorePath());
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
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        if("E".equals(fileStoreInfo.getIsTemp())) return null;
        return fileStoreInfo.isTemp() ? new FileInputStream(fileStoreInfo.getFileStorePath())
            : fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
    }

    /**
     * @param fileId 文件的id
     * @return 文件句柄
     * @throws IOException 异常
     */
    @Override
    public File getFile(String fileId) throws IOException {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        if("E".equals(fileStoreInfo.getIsTemp())) return null;
        return fileStoreInfo.isTemp() ? new File(fileStoreInfo.getFileStorePath())
            : fileStore.getFile(fileStoreInfo.getFileStorePath());
    }

    @Override
    public boolean deleteFile(String fileId){
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo != null) {
            fileInfoManager.deleteObjectById(fileId);
            if(esServerConfig != null){
                return fetchDocumentIndexer().deleteDocument(fileId);
            }
            fileStoreInfoManager.decreaseFileReference(fileInfo.getFileMd5());
            return true;
        }
        return false;
    }

    @Override
    public FileBaseInfo getFileInfo(String fileId) {
        return fileInfoManager.getObjectById(fileId);
        /*FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(objectById,fileInfo);
        return fileInfo;*/
    }
}
