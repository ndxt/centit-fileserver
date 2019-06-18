package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * 全文检索
 */
@Service
public class DocumentIndexOpt extends FileOpt implements Consumer<FileOptTaskInfo> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexOpt.class);

    @Resource
    private FileInfoManager fileInfoManager;

    @Resource
    private Indexer documentIndexer;

    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        FileDocument fileDoc = FilePretreatUtils.index(fileInfo, originalTempFilePath);
        documentIndexer.saveNewDocument(fileDoc);
        logger.info("文件已加入全文检索");
    }
}
