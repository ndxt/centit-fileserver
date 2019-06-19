package com.centit.fileserver.task.plugin;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileOptTaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisFileOptTaskQueue implements FileOptTaskQueue {

    private static final Logger logger = LoggerFactory.getLogger(RedisFileOptTaskQueue.class);

    private static final String FILE_TASK_INFO_KEY = "file:task:info:list";

    private RedisTemplate redisTemplate;


    public RedisFileOptTaskQueue(String host, int port, int dbIndex) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(dbIndex);
        JedisConnectionFactory factory = new JedisConnectionFactory(configuration);
        redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.afterPropertiesSet();
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
