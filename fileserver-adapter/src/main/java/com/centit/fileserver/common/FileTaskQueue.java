package com.centit.fileserver.common;

/**
 * 保存后台处理任务的接口
 * @author codefan@sina.com
 */
public interface FileTaskQueue {

    /**
     * 添加任务到队列
     * @param taskInfo 文件操作信息
     * @return 是否添加成功
     */
    boolean add(FileTaskInfo taskInfo);

    /**
     * 从队列获取任务
     * @return 文件操作信息
     */
    FileTaskInfo get();
}
