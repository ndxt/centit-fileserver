package com.centit.fileserver.task.plugin;

import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisFileTaskQueue implements FileTaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(RedisFileTaskQueue.class);

    private static final String FILE_TASK_INFO_KEY = "file:task:info:list";

    private RedisTemplate redisTemplate;

    public RedisFileTaskQueue() {

    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean add(FileTaskInfo taskInfo) {
        redisTemplate.opsForList().rightPush(FILE_TASK_INFO_KEY, taskInfo);
        logger.info("任务加入Redis队列");
        return true;
    }

    @Override
    public FileTaskInfo get() {
        return (FileTaskInfo) redisTemplate.opsForList().leftPop(FILE_TASK_INFO_KEY);
    }
}
