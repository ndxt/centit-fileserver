package com.centit.fileserver.store.plugin.test;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.region.Region;

import java.io.File;

public class CosTest {
    public static void main(String[] args) {
        COSClient cosClient = new COSClient(new BasicCOSCredentials("AKID30Qk1EkhzNells9lhzlgV8lJgQfrpr01", "98ZIwTAi8gBEi6d3216StvZGt5kWUmS1"), new ClientConfig(new Region("ap-shanghai")));

        System.out.println(cosClient.putObject("filesave-1259276167", "/TestFile.txt", new File("D:\\TestFile.txt")));
    }
}
