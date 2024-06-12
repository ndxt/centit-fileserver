package com.centit.fileserver.task.plugin;

import com.alibaba.fastjson2.JSON;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.common.FileTaskQueue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.StringUtils;
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
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> commands = connection.sync();
        commands.rpush(FILE_TASK_INFO_KEY, JSON.toJSONString(taskInfo));
        connection.close();
        //redisTemplate.opsForList().rightPush(FILE_TASK_INFO_KEY, taskInfo);
        logger.info("任务加入Redis队列");
        return true;
    }

    @Override
    public FileTaskInfo get() {
        //return (FileTaskInfo) redisTemplate.opsForList().leftPop(FILE_TASK_INFO_KEY);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> commands = connection.sync();
        String sjson = commands.lpop(FILE_TASK_INFO_KEY);
        connection.close();
        if(StringUtils.isBlank(sjson))
            return null;
        return JSON.parseObject(sjson, FileTaskInfo.class);
    }
}
