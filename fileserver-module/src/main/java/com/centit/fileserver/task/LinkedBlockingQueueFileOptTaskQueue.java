package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileOptTaskQueue;
import com.centit.support.file.FileSystemOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

public class LinkedBlockingQueueFileOptTaskQueue implements FileOptTaskQueue {
    private static final Logger logger = LoggerFactory.getLogger(LinkedBlockingQueueFileOptTaskQueue.class);

    private File taskFile;
    private LinkedBlockingQueue<FileOptTaskInfo> taskQueue;

    public LinkedBlockingQueueFileOptTaskQueue(String taskFileRoot) throws Exception {
        FileSystemOpt.createDirect(taskFileRoot);
        if (taskFileRoot.endsWith(String.valueOf(File.separatorChar))) {
            taskFile = new File(taskFileRoot + "task.dat");
        } else {
            taskFile = new File(taskFileRoot + File.separatorChar +  "task.dat");
        }

        if (taskFile.exists()) {
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(taskFile))) {
                taskQueue = (LinkedBlockingQueue) ois.readObject();
            }
        } else {
            taskQueue = new LinkedBlockingQueue();
        }
    }

    @Override
    public boolean add(FileOptTaskInfo task) {
        taskQueue.offer(task);
        saveTasksToDisk();
        return true;
    }

    @Override
    public FileOptTaskInfo get() {
        FileOptTaskInfo task = taskQueue.poll();
        if (null != task) {
            saveTasksToDisk();
        }
        return task;
    }

    private void saveTasksToDisk() {
        logger.info("持久化任务, 任务总数: " + taskQueue.size());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(taskFile))) {
            oos.writeObject(taskQueue);
        } catch (IOException e) {
            logger.error("持久化文件存储任务失败: " + e.getMessage());
        }
    }
}
