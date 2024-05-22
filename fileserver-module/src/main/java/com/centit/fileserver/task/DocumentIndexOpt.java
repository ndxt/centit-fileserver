package com.centit.fileserver.task;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.search.document.FileDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Map;

/**
 * 全文检索
 */
@Service
@Transactional
public class DocumentIndexOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired(required = false)
    private ESServerConfig esServerConfig;

    @Autowired
    protected FileStore fileStore;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    public ESIndexer fetchDocumentIndexer(){
        if(esServerConfig==null)
            return null;
        return IndexerSearcherFactory.obtainIndexer(esServerConfig, FileDocument.class);
    }

    public DocumentIndexOpt(){
        this.esServerConfig = null;
    }
    /**
     * @return 任务转换器名称
     */
    @Override
    public String getOpeatorName() {
        return "index";
    }

    private static boolean canIndex(String fileType) {
        return StringUtils.equalsAnyIgnoreCase(fileType,
            "txt", "csv", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx","pdf", "html", "xml");
    }

    /**
     * 获取文件预处理信息
     * @param fileInfo     文件信息
     * @param fileSize     文件大小
     * @param pretreatInfo 预处理信息
     * @return 文件任务信息 null 表示不匹配不需要处理
     */
    @Override
    public FileTaskInfo attachTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if(BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("index"),false)
            && canIndex(fileInfo.getFileType())){
            FileTaskInfo indexTaskInfo = new FileTaskInfo(getOpeatorName());
            indexTaskInfo.copy(fileInfo);
            indexTaskInfo.setFileSize(fileSize);
            return indexTaskInfo;
        }
        return null;
    }

    public void doFileIndex(FileInfo fileInfo, long fileSize){
        try {
            String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
            FileDocument fileDoc;
            if (FileSystemOpt.existFile(originalTempFilePath)) {
                fileDoc = FilePretreatUtils.index(fileInfo, new File(originalTempFilePath));
            } else {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
                fileDoc = FilePretreatUtils.index(fileInfo, fileStore.getFile(fileStoreInfo.getFileStorePath()));
            }
            fetchDocumentIndexer().mergeDocument(fileDoc);
            logger.info("文件已加入全文检索");
            fileInfoManager.updateObject(fileInfo);
        } catch (Exception e) {
            logger.error("找不到被索引的文件，" + e.getMessage(), e);
        }
    }

    @Override
    public int runTaskInfo(FileInfo  fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if(esServerConfig==null){
            return 0;
        }
        if(BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("index"),false)
            && canIndex(fileInfo.getFileType())){
            doFileIndex(fileInfo, fileSize);
            return 1;
        }
        return 0;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        if(esServerConfig==null){
            return;
        }
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(null==fileInfo) {
            return;
        }
        doFileIndex(fileInfo, fileSize);
    }
}
