package com.centit.fileserver.common;

import com.centit.fileserver.po.FileInfo;

import java.util.Map;
/**
 * @author codefan@sina.com
 */
public interface FileTaskOpeator {
    /**
     * @return 任务转换器名称
     */
    String getOpeatorName();

    /**
     * 获取文件预处理信息
     * @param fileInfo 文件信息
     * @param fileSize 文件大小
     * @param pretreatInfo 预处理信息
     * @return 文件任务信息 null 表示不匹配不需要处理
     */
    FileTaskInfo attachTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo);


    int runTaskInfo(FileInfo fileInfo, long fileSize, Map<String, Object> pretreatInfo);

    /**
     * @param taskInfo 执行文件转换任务
     */
    void doFileTask(FileTaskInfo taskInfo);
}
