package com.centit.fileserver.test;

import com.centit.fileserver.client.FileClientImpl;
import com.centit.fileserver.client.po.FileAccessLog;

/**
 * Created by codefan on 17-8-3.
 */
public class TestAccessApply {
    public static void main(String[] args) throws Exception {
        FileClientImpl fileClient = new FileClientImpl();
        fileClient.init("http://codefanbook:8080/fileserver",
                "u0000000","000000","http://codefanbook:8080/fileserver");
        FileAccessLog accessLog = new FileAccessLog();
        System.out.println(fileClient.getFileUrl(accessLog));
    }
}
