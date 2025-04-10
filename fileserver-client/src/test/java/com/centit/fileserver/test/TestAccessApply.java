package com.centit.fileserver.test;

import com.alibaba.fastjson2.JSON;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileInfo;
import com.centit.framework.appclient.HttpReceiveJSON;
import com.centit.framework.common.ResponseData;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Created by codefan on 17-8-3.
 */
public class TestAccessApply {
    public static void main(String[] args) {
        String jsonStr = "{\"start\":3511,\"signal\":\"complete\",\"code\":0,\"message\":\"\",\"data\":" +
            "{\"fileName\":\"aaa\",\"fileMd5\":\"6e515d60e60f5a5f59e70c47b6060ba3\",\"fileSize\":3511," +
            "\"fileId\":\"fbc4625d47b94531992de5e737df8423\"}}";
        HttpReceiveJSON resJson  = HttpReceiveJSON.dataOfJson(jsonStr);

        FileInfo fileInfo = resJson.getDataAsObject(ResponseData.RES_DATA_FILED, FileInfo.class);

        System.out.println(JSON.toJSONString(fileInfo));
    }

    public static void main2(String[] args) throws Exception {

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
