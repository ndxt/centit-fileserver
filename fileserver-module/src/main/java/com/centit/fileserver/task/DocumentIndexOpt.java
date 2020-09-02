package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Indexer;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 全文检索
 */
@Service
public class DocumentIndexOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired(required = false)
    private ESIndexer esObjectIndexer;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        if(esObjectIndexer==null){
            return;
        }
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        FileDocument fileDoc = FilePretreatUtils.index(fileInfo, originalTempFilePath);
        esObjectIndexer.mergeDocument(fileDoc);
        logger.info("文件已加入全文检索");
        OperationLogCenter.log(OperationLog.create().operation("FileServerLog")
            .method("文件已加入全文检索").tag(fileId).time(DatetimeOpt.currentUtilDate()).content(fileInfo.getFileName()).newObject(fileInfo));
    }
}
