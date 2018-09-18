package com.centit.upload.service;

import com.centit.support.database.utils.QueryUtils;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Test {

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        System.out.println( new Timestamp( cal.getTimeInMillis()).toString());
        System.out.println( new Timestamp( System.currentTimeMillis()).toString());
    }

    public static void testPDF(){
        try{
            File f = new File("D:\\红酒报价单.pdf");
            Map<String,File> files = new HashMap<String,File>();
            files.put("file", f);
            String path="http://localhost:8080/product-uploader/service/formdata?http://localhost:8080/product-uploader/singleUploader.jsp";
            String response = HttpExecutor.formPostWithFileUpload(
                    HttpExecutorContext.create(HttpExecutor.createHttpClient()),
                    path, QueryUtils.createSqlParamsMap("filename","11"),
                    files);


            //GetMethod postMethod=new GetMethod(path);
            /*PostMethod postMethod = new PostMethod(path);
            //postMethod.setRequestHeader('');
            File f = new File("D:\\红酒报价单.pdf");
            Part part1 = new FilePart("file", f);
            Part[] parts = {new StringPart("filename", "11"),part1};
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

            int status = client.executeMethod(postMethod);
            System.out.print(status);
            String response = postMethod.getResponseBodyAsString();*/
            System.out.println(response);
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }

//        File file = new File("D:\\红酒报价单.pdf");
//        SaveFileManager sfm = new SaveFileImpl();
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        map.put("filename","newfile");
//        map.put("address","D:/test/test1");
//        try {
//            sfm.saveFile(file, map);
//            //sfm.saveFiles(new File[] {f},map);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}