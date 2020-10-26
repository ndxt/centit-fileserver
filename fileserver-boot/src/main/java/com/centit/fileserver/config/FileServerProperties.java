package com.centit.fileserver.config;

import com.centit.search.service.ESServerConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(FileServerProperties.PREFIX)
@Data
public class FileServerProperties {
    public static final String PREFIX = "fileserver";

    private FileStoreConfig fileStore;
    private SmsConfig sms;
    private NotifyConfig notify;
    private ESServerConfig elasticSearch;
    private boolean fulltextIndexEnable;

    @Data
    public static class FileStoreConfig {
        private String storeType;
        private OsConfig os;
        private OssConfig oss;
        private CosConfig cos;

        @Data
        public static class OsConfig {
            private String baseDir;
        }

        @Data
        public static class OssConfig {
            private String endPoint;
            private String accessKeyId;
            private String secretAccessKey;
            private String bucketName;
        }

        @Data
        public static class CosConfig {
            private String region;
            private String appId;
            private String secretId;
            private String secretKey;
            private String bucketName;
        }
    }

    @Data
    public static class SmsConfig {
        private String sendUrl;
    }

    @Data
    public static class NotifyConfig {
        private String type;
    }
}
