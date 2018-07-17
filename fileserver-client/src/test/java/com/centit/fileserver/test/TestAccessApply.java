package com.centit.fileserver.test;

import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Created by codefan on 17-8-3.
 */
public class TestAccessApply {
    public static void main(String[] args) throws Exception {

        FileAccessLog accessLog = new FileAccessLog();
        accessLog.setAccessRight("T");
        accessLog.setAccessTimes(5);
        accessLog.setAccessUsename("管理员");
        accessLog.setAccessUsercode("u0000000");
        accessLog.setFileId("File0000000000000000001");

        try(CloseableHttpClient httpClient = HttpExecutor.createHttpClient()) {
            String jsonStr = HttpExecutor.formPost(HttpExecutorContext.create(httpClient),
                    "http://localhost:8080/fileserver/service/access/apply", accessLog);
            System.out.println(jsonStr);

            jsonStr = HttpExecutor.jsonPost(HttpExecutorContext.create(httpClient),
                    "http://localhost:8080/fileserver/service/access/japply", accessLog);
            System.out.println(jsonStr);
        }
    }
}
