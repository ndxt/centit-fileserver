package com.centit.fileserver.common;

/**
 * 保存后台处理任务的接口
 */
public interface FileOptTaskQueue {

    /**
     * 添加任务到队列
     * @param taskInfo 文件操作信息
     * @return 是否添加成功
     */
    boolean add(FileOptTaskInfo taskInfo);

    /**
     * 从队列获取任务
     * @return 文件操作信息
     */
    FileOptTaskInfo get();
}
