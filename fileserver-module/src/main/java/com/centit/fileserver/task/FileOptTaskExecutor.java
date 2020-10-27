package com.centit.fileserver.task;

import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskOpeator;
import com.centit.fileserver.common.FileTaskQueue;
import com.centit.fileserver.po.FileInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOptTaskExecutor {

    //private static final Logger logger = LoggerFactory.getLogger(FileOptTaskExecutor.class);

    private FileTaskQueue fileOptTaskQueue;

    private List<FileTaskOpeator> fileOptList;
    private Map<String, FileTaskOpeator> fileOptMap;


    public FileOptTaskExecutor(){
        fileOptMap = new HashMap<>(20);
        fileOptList = new ArrayList<>(20);
    }

    public void addFileOperator(FileTaskOpeator fileOpt){
        fileOptList.add(fileOpt);
        fileOptMap.put(fileOpt.getOpeatorName(), fileOpt);
    }

    public void setFileOptTaskQueue(FileTaskQueue fileOptTaskQueue) {
        this.fileOptTaskQueue = fileOptTaskQueue;
    }

    public int addOptTask(FileInfo fileInfo, long size, Map<String, Object> pretreatInfo){
        int tasks = 0;
        for(FileTaskOpeator fileOpt : fileOptList){
            FileTaskInfo taskInfo = fileOpt.attachTaskInfo(fileInfo, size, pretreatInfo);
            if(taskInfo!=null){
                fileOptTaskQueue.add(taskInfo);
                tasks++;
            }
        }
        return tasks;
    }

    /*@PostConstruct
    public void doTask() {
        new Thread(new FileOptTask()).start();
    }
    */
    public void doFileOptJob() {
        FileTaskInfo taskInfo = fileOptTaskQueue.get();
        while(taskInfo != null){
            String taskType = taskInfo.getTaskType();
            fileOptMap.get(taskType).doFileTask(taskInfo);
            taskInfo = fileOptTaskQueue.get();
        }
    }

   /* class FileOptTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    FileOptTaskInfo taskInfo = fileOptTaskQueue.get();
                    if (null == taskInfo) {
                        Thread.sleep(5000);
                    } else {
                        int taskType = taskInfo.getTaskType();
                        fileOptList.get(taskType).accept(taskInfo);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }*/
}
