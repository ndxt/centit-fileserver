package com.centit.fileserver.store.plugin.test;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.centit.support.file.FileIOOpt;

import java.io.File;
import java.io.IOException;

public class OssTest {

    public static void main(String[] args) throws IOException {
        OSSClient ossc = new OSSClient("oss-cn-shanghai.aliyuncs.com",
                //"5adzw3sGtcPeQMjm","LldMVUMuemMoJ7YEVBxZ3pXsWxY63T");
        "LTAI9QEcfHf7VrSE","fYHyfoehaUS48tv53XEh3tihHx4hsN");

        OSSObject oobj = ossc.getObject("centit-pub", "image2016-9-22 14-13-24.png");

        FileIOOpt.writeInputStreamToFile(oobj.getObjectContent(),
                new File("D:\\temp\\oos.png"));

        //ossc.putObject(bucketName, key, file, metadata)
    }

}
