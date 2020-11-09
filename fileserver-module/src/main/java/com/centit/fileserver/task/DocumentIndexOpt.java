package com.centit.fileserver.task;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.controller.FileLogController;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 全文检索
 */
@Service
public class DocumentIndexOpt implements FileTaskOpeator {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIndexOpt.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired(required = false)
    private ESIndexer esObjectIndexer;

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
    public FileTaskInfo attachTaskInfo(FileBaseInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo) {
        if(BooleanBaseOpt.castObjectToBoolean(pretreatInfo.get("index"),false)
            && canIndex(fileInfo.getFileType())){
            FileTaskInfo indexTaskInfo = new FileTaskInfo(getOpeatorName());
            indexTaskInfo.copy(fileInfo);
            indexTaskInfo.setFileSize(fileSize);
            return indexTaskInfo;
        }
        return null;
    }

    @Override
    public void doFileTask(FileTaskInfo fileOptTaskInfo) {
        if(esObjectIndexer==null){
            return;
        }
        String fileId = fileOptTaskInfo.getFileId();
        long fileSize = fileOptTaskInfo.getFileSize();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(null==fileInfo) {
            return;
        }
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        FileDocument fileDoc = FilePretreatUtils.index(fileInfo, originalTempFilePath);
        esObjectIndexer.mergeDocument(fileDoc);
        logger.info("文件已加入全文检索");
        OperationLogCenter.log(OperationLog.create().operation(FileLogController.LOG_OPERATION_NAME)
            .user("admin").unit(fileInfo.getLibraryId())
            .method("文件已加入全文检索").tag(fileId).time(DatetimeOpt.currentUtilDate()).content(fileInfo.getFileName()).newObject(fileInfo));
        fileInfoManager.updateObject(fileInfo);
    }
}
