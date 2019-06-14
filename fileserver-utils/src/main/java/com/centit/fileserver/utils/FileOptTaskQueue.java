package com.centit.fileserver.utils;

/**
 * 保存后台处理任务的接口
 */
public interface FileOptTaskQueue {

    /**
     * 添加任务到队列
     * @param taskInfo
     * @return
     */
    boolean add(FileOptTaskInfo taskInfo);

    /**
     * 从队列获取任务
     * @return
     */
    FileOptTaskInfo get();
}
