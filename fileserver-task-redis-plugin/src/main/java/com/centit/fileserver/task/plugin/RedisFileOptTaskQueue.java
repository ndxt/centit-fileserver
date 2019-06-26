package com.centit.fileserver.task.plugin;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileOptTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisFileOptTaskQueue implements FileOptTaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(RedisFileOptTaskQueue.class);

    private static final String FILE_TASK_INFO_KEY = "file:task:info:list";

    private RedisTemplate redisTemplate;

    public RedisFileOptTaskQueue() {

    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean add(FileOptTaskInfo taskInfo) {
        redisTemplate.opsForList().rightPush(FILE_TASK_INFO_KEY, taskInfo);
        logger.info("任务加入Redis队列");
        return true;
    }

    @Override
    public FileOptTaskInfo get() {
        return (FileOptTaskInfo) redisTemplate.opsForList().leftPop(FILE_TASK_INFO_KEY);
    }
}
