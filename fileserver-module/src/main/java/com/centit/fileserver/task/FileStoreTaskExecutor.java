package com.centit.fileserver.task;

import com.centit.fileserver.fileaccess.FilePretreatment;
import com.centit.fileserver.fileaccess.PretreatInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.FileStoreTaskInfo;
import com.centit.fileserver.utils.FileStoreTaskPool;
import com.centit.fileserver.utils.SystemTempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FileStoreTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreTaskExecutor.class);

    @Autowired
    private FileStore fileStore;

    @Autowired
    private FileStoreTaskPool fileStoreTaskPool;

    @Autowired
    private FileInfoManager fileInfoManager;

    class SaveFileTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    FileStoreTaskInfo taskInfo = fileStoreTaskPool.get();
                    if (null == taskInfo) {
                        Thread.sleep(5000);
                    } else {
                        FileStoreTaskInfo.TaskType taskType = taskInfo.getTaskType();
                        switch (taskType) {
                            case PRETREAT_FILE:
                                pretreatFile(taskInfo);
                                break;
                            case SAVE_FILE:
                                saveFile(taskInfo);
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void saveFile(FileStoreTaskInfo taskInfo) {
            try {
                FileInfo fileInfo = (FileInfo) taskInfo.getTaskInfo();
                String fileMd5 = fileInfo.getFileMd5();
                long fileSize = fileInfo.getFileSize();
                String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
                fileStore.saveFile(tempFilePath, fileMd5, fileSize);
            } catch (Exception e) {
                logger.info("保存文件出错: " + e.getMessage());
            }
        }

        private void pretreatFile(FileStoreTaskInfo taskInfo) {
            PretreatInfo pretreatInfo = (PretreatInfo) taskInfo.getTaskInfo();
            FileInfo fileInfo = fileInfoManager.getObjectById(pretreatInfo.getFileId());
//            FilePretreatment.pretreatment(fileStore, )
        }
    }

    @PostConstruct
    public void doTask() {
        new Thread(new SaveFileTask()).start();
    }
}
