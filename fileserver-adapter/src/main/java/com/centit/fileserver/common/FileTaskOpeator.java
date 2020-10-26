package com.centit.fileserver.common;

import com.centit.fileserver.po.FileBaseInfo;

import java.util.Map;

public interface FileTaskOpeator {
    String getOpeatorName();

    /**
     * 获取文件预处理信息
     * @param fileInfo 文件信息
     * @param pretreatInfo 预处理信息
     * @return 文件任务信息 null 表示不匹配不需要处理
     */
    FileTaskInfo attachTaskInfo(FileBaseInfo fileInfo, Map<String, Object> pretreatInfo);

    /**
     * @param taskInfo 任务西悉尼
     */
    void doFileTask(FileTaskInfo taskInfo);
}
