package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.file.FileSystemOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public abstract class FileStoreOpt {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreOpt.class);

    @Autowired
    protected FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    private String fetchOrSaveFile(String tempFilePath, FileInfo file, long fileSize)
    throws IOException {

        String fileStoreUrl = fileStore.matchFileStoreUrl(file, fileSize);
        if(fileStore.checkFile(fileStoreUrl)){
            return fileStoreUrl;
        }
        fileStoreUrl = fileStore.saveFile(tempFilePath, file, fileSize);
        //删除临时文件
        FileSystemOpt.deleteFile(tempFilePath);
        return fileStoreUrl;
    }

    private void transTempFileToStore(FileStoreInfo fileStoreInfo){
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileMd5(fileStoreInfo.getFileMd5());
            fileInfo.setFileSize(fileStoreInfo.getFileSize());

            String fileStoreUrl = fetchOrSaveFile(fileStoreInfo.getFileStorePath(), fileInfo, fileStoreInfo.getFileSize());
            fileStoreInfo.setFileStorePath(fileStoreUrl);
            fileStoreInfo.setIsTemp(false);
            fileStoreInfoManager.updateObject(fileStoreInfo);
        } catch (IOException e){
            logger.error("文件转存失败", e);
            OperationLogCenter.log(OperationLog.create().operation(FileIOUtils.LOG_OPERATION_NAME)
                .user("system").unit("platform")
                .topUnit("system").level(OperationLog.LEVEL_ERROR)
                .correlation("transTempFileToStore")
                .method("文件转储").tag("fixbug")
                .content("文件转存失败,临时文件过期导致文件不可用")
                .oldObject(fileStoreInfo));
            fileStoreInfoManager.markStoreErrorTag(fileStoreInfo);
        }
    }

    public void checkTempFileAndCreateTask(int limitSize){
        List<FileStoreInfo> tempFils = fileStoreInfoManager.listTempFile(limitSize);
        if(tempFils == null || tempFils.isEmpty()) return;
        for(FileStoreInfo tempFile : tempFils) {
            this.transTempFileToStore(tempFile);
        }
    }
    /**
     * 存储文件
     * @param tempFilePath 临时文件路劲
     * @param file FileInfo
     * @param fileSize 文件大小
     */
    protected void save(String tempFilePath, FileInfo file, long fileSize) {
        try {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(file.getFileMd5());
            if (fileStoreInfo == null) { // 按道理永远运行不到这儿
                String fileStorePath = fetchOrSaveFile(tempFilePath, file, fileSize);
                fileStoreInfo =
                    new FileStoreInfo(file.getFileMd5(), fileSize,
                        fileStorePath, 1L,false);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                if(fileStoreInfo.isTemp()){
                    fileStoreInfo.setFileStorePath(
                        fetchOrSaveFile(tempFilePath, file, fileSize));
                    fileStoreInfo.setIsTemp(false);
                }/*else{
                    fileStoreInfo.setFileReferenceCount(fileStoreInfo.getFileReferenceCount() + 1);
                }*/
                fileStoreInfoManager.updateObject(fileStoreInfo);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }
}
