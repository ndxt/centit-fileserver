package com.centit.fileserver.store.plugin.test;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class CosTest {
    private static String bucketName = "filesave-1259276167";
    private static String key = "TestFile.txt";

    public static void main(String[] args) {
        testDownloadUrl();
    }

    public static void testPut() {
        COSClient cosClient = getCosClient();

        PutObjectResult result = cosClient.putObject(bucketName, key, new File("D:\\TestFile.txt"));
        System.out.println(result.getExpirationTime());
    }

    public static void testDelete() {

        COSClient cosClient = getCosClient();
        cosClient.deleteObject(bucketName, key);
    }

    public static void testDownloadUrl() {

        // 生成预签名链接
        // HTTP方法: PUT用于上传; GET用于下载; DELETE用于删除
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key, HttpMethodName.GET);
        // 设置签名在半小时后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 30L * 60L * 1000L);
        request.setExpiration(expirationDate);
        COSClient cosClient = getCosClient();
        URL downloadUrl = cosClient.generatePresignedUrl(request);
        System.out.println(downloadUrl);
        // http://filesave-1259276167.cos.ap-shanghai.myqcloud.com/TestFile.txt?sign=q-sign-algorithm%3Dsha1%26q-ak%3DAKID30Qk1EkhzNells9lhzlgV8lJgQfrpr01%26q-sign-time%3D1558950548%3B1558952347%26q-key-time%3D1558950548%3B1558952347%26q-header-list%3D%26q-url-param-list%3D%26q-signature%3D19dd69768e3f71272366eafd389551ba88294b3d
    }

    public static COSClient getCosClient() {
        String accessKey = "AKID30Qk1EkhzNells9lhzlgV8lJgQfrpr01";
        String secretKey = "98ZIwTAi8gBEi6d3216StvZGt5kWUmS1";
        BasicCOSCredentials basicCOSCredentials = new BasicCOSCredentials(accessKey, secretKey);
        String regionName = "ap-shanghai";
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        return new COSClient(basicCOSCredentials, clientConfig);
    }
}
