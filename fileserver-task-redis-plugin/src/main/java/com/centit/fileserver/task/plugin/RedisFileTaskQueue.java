package com.centit.fileserver.task.plugin;

import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskQueue;
import io.lettuce.core.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisFileTaskQueue implements FileTaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(RedisFileTaskQueue.class);

    private static final String FILE_TASK_INFO_KEY = "file:task:info:list";

    private RedisClient redisClient;

    public RedisFileTaskQueue() {

    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public boolean add(FileTaskInfo taskInfo) {
        //redisClient.
        //redisTemplate.opsForList().rightPush(FILE_TASK_INFO_KEY, taskInfo);
        logger.info("任务加入Redis队列");
        return true;
    }

    @Override
    public FileTaskInfo get() {
        return null;
        //return (FileTaskInfo) redisTemplate.opsForList().leftPop(FILE_TASK_INFO_KEY);
    }
}
