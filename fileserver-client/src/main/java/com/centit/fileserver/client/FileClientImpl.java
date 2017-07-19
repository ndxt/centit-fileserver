package com.centit.fileserver.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.client.po.FileAccessLog;
import com.centit.fileserver.client.po.FileStoreInfo;
import com.centit.framework.appclient.AppSession;
import com.centit.framework.core.common.ObjectException;
import com.centit.framework.core.common.ResponseJSON;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.InputStreamResponseHandler;
import com.centit.support.network.Utf8ResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;


public class FileClientImpl implements FileClient {

    /**
     *   #文件服务器
     * @param appServerUrl fileserver.url = http://codefanbook:8180/product-uploader
     * @param userCode  fileserver.userCode =u0000000
     * @param password fileserver.password =000000
     * @param appServerExportUrl  # 文件服务器对外url 这个和 fileserver.url 不一定一样 fileserver.url 是内容文件服务器url
     *                            # fileserver.export.url 是对外的url，由于可能存在 nginx反向代理所以可能不一样
     *                            # fileserver.export.url = http://codefanbook:8180/product-uploader
     */
    public void init(String appServerUrl, String userCode,
                     String password, String appServerExportUrl){
        appSession = new AppSession(
                appServerUrl,false,userCode,password);
        this.fileServerExportUrl = appServerExportUrl;
    }

    private AppSession appSession;

    private String fileServerExportUrl;

    public void setAppSession(AppSession appSession) {
        this.appSession = appSession;
    }

    public void setFileServerExportUrl(String fileServerUrl) {
        this.fileServerExportUrl = fileServerUrl;
    }

    public CloseableHttpClient getHttpClient() throws Exception {
        return appSession.getHttpClient();
    }

    public void releaseHttpClient(CloseableHttpClient httpClient) {
        appSession.releaseHttpClient(httpClient);
    }

    public String/*文件下载url */getFileUrl(CloseableHttpClient httpClient, FileAccessLog aacessLog) throws Exception {
        appSession.checkAccessToken(httpClient);
        String jsonStr = HttpExecutor.jsonPost(httpClient,
                appSession.completeQueryUrl("/service/access/japply"), aacessLog);
        ResponseJSON resJson = ResponseJSON.valueOfJson(jsonStr);

        if (resJson.getCode() != 0) {
            throw new ObjectException(aacessLog, resJson.getMessage());
        }
        FileAccessLog acclog = resJson.getDataAsObject(FileAccessLog.class);
        return StringUtils.equals("T", aacessLog.getAccessRight())
                ? fileServerExportUrl + "/service/download/attach/" + acclog.getAccessToken()
                : fileServerExportUrl + "/service/download/file/" + acclog.getAccessToken();
    }

    public String/*文件下载url */getFileUrl(FileAccessLog aacessLog) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        String url = getFileUrl(httpClient, aacessLog);
        releaseHttpClient(httpClient);
        return url;
    }

    public String getAttachFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws Exception {
        FileAccessLog aacessLog = new FileAccessLog();
        aacessLog.setFileId(fileId);
        aacessLog.setAccessRight("T");
        if (expireTime > 0)
            aacessLog.setTokenExpireTime(
                    DatetimeOpt.addMinutes(DatetimeOpt.currentUtilDate(), expireTime));
        return getFileUrl(httpClient, aacessLog);

    }

    public String getFileUrl(CloseableHttpClient httpClient, String fileId, int expireTime) throws Exception {
        FileAccessLog aacessLog = new FileAccessLog();
        aacessLog.setFileId(fileId);
        aacessLog.setAccessRight("A");
        if (expireTime > 0)
            aacessLog.setTokenExpireTime(
                    DatetimeOpt.addMinutes(DatetimeOpt.currentUtilDate(), expireTime));
        return getFileUrl(httpClient, aacessLog);
    }

    public String getAttachFileUrl(String fileId, int expireTime) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        String url = getAttachFileUrl(httpClient, fileId, expireTime);
        releaseHttpClient(httpClient);
        return url;
    }

    public String getFileUrl(String fileId, int expireTime) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        String url = getFileUrl(httpClient, fileId, expireTime);
        releaseHttpClient(httpClient);
        return url;
    }


    public FileStoreInfo getFileStoreInfo(CloseableHttpClient httpClient, String fileId) throws Exception {
        appSession.checkAccessToken(httpClient);
        String jsonStr = HttpExecutor.simpleGet(httpClient,
                appSession.completeQueryUrl("/service/files/" + fileId));
        ResponseJSON resJson = ResponseJSON.valueOfJson(jsonStr);

        if (resJson.getCode() != 0) {
            throw new ObjectException(fileId, resJson.getMessage());
        }
        FileStoreInfo fileStoreInfo = resJson.getDataAsObject(FileStoreInfo.class);
        return fileStoreInfo;
    }

    public boolean updateFileStoreInfo(CloseableHttpClient httpClient, FileStoreInfo fsi) throws Exception {
        appSession.checkAccessToken(httpClient);
        String jsonStr = HttpExecutor.jsonPost(httpClient,
                appSession.completeQueryUrl("/service/files/j/" + fsi.getFileId()), fsi);
        ResponseJSON resJson = ResponseJSON.valueOfJson(jsonStr);
        if (resJson == null) {
            throw new ObjectException(fsi, "请求失败！");
        }
        return resJson.getCode() == 0;
    }

    public boolean updateFileStoreInfo(FileStoreInfo fsi) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        boolean upres = updateFileStoreInfo(httpClient, fsi);
        releaseHttpClient(httpClient);
        return upres;
    }


    public FileStoreInfo uploadFile(CloseableHttpClient httpClient, FileStoreInfo fsi,File file) throws Exception {
        appSession.checkAccessToken(httpClient);

        String jsonStr = HttpExecutor.fileUpload(httpClient,
                appSession.completeQueryUrl("/service/upload/file"),
                JSON.parseObject(JSON.toJSONString(fsi) ),
                file);

        ResponseJSON resJson = null;
        try {
            resJson = ResponseJSON.valueOfJson(jsonStr);
        }catch(Exception e){
            throw new ObjectException(jsonStr, "解析返回json串失败");
        }
        if (resJson == null) {
            throw new ObjectException(jsonStr, "请求失败！");
        }
        return resJson.getDataAsObject(FileStoreInfo.class);
    }

    public FileStoreInfo uploadFile(FileStoreInfo fsi,File file) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        FileStoreInfo upres = uploadFile(httpClient, fsi,file);
        releaseHttpClient(httpClient);
        return upres;
    }

    public long getFileRangeStart(CloseableHttpClient httpClient, String fileMd5,long fileSize) throws Exception{
        String uri =  appSession.completeQueryUrl(
                "/service/upload/range?token="+fileMd5+"&size="+String.valueOf(fileSize));
        String jsonStr = HttpExecutor.simpleGet(httpClient,uri);
        long rangeStart = -1;

        try {
            JSONObject obj = JSON.parseObject(jsonStr);
            rangeStart = obj.getLong("start");
        }catch(Exception e){
            throw new ObjectException(jsonStr, "解析返回json串失败");
        }

        return rangeStart;
    }

    public long getFileRangeStart(String fileMd5,long fileSize) throws Exception{
        CloseableHttpClient httpClient = getHttpClient();
        long rs = getFileRangeStart(httpClient,fileMd5,fileSize);
        releaseHttpClient(httpClient);
        return rs;
    }

    public long getFileRangeStart(CloseableHttpClient httpClient, File file) throws Exception{
        String fileMd5 = FileMD5Maker.makeFileMD5(file);
        long fileSize = file.length();
        return getFileRangeStart(httpClient,fileMd5,fileSize);
    }

    public long getFileRangeStart(File file) throws Exception{
        CloseableHttpClient httpClient = getHttpClient();
        long rs = getFileRangeStart(httpClient,file);
        releaseHttpClient(httpClient);
        return rs;
    }

    public FileStoreInfo uploadFileRange(CloseableHttpClient httpClient, FileStoreInfo fsi,
                                         File file,long rangeStart,long rangeSize) throws Exception{

        String fileMd5 = FileMD5Maker.makeFileMD5(file);
        long fileSize = file.length();

        String uri =  appSession.completeQueryUrl(
                "/service/upload/range?token="+fileMd5+"&size="+String.valueOf(fileSize));
        List<NameValuePair> params = HttpExecutor.makeRequectParams(fsi, "");

        String paramsUrl1 = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        HttpPost httpPost = new HttpPost(HttpExecutor.appendParamToUrl(uri, paramsUrl1));
        httpPost.setHeader("Content-Type","application/octet-stream");
        httpPost.addHeader("method","post");
        httpPost.addHeader("type","file");
        //HttpExecutor.multiPartApplicationFormHead);
        //"multipart/form-data; charset=UTF-8; boundary=------1cC9oE7dN8eT1fI0aT2n4------");
        long rangeEnd = rangeStart+rangeSize-1;
        int postFileSize= (int) rangeSize;
        if(rangeEnd>=fileSize) {
            rangeEnd = fileSize - 1;
            postFileSize = (int)(fileSize - rangeStart);
        }
        httpPost.addHeader("content-range",
                String.valueOf(rangeStart)+"-"+String.valueOf(rangeEnd)+"/"+String.valueOf(fileSize));
        //httpPost.addHeader("type","file");
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) (rangeSize));
        FileInputStream fin = new FileInputStream(file);
        int gotlen=0;
        int read1;
        byte[] bytes = new byte[10240];
        if(rangeStart>0)
            fin.skip(rangeStart);
        while(gotlen < postFileSize) {
            read1 = fin.read(bytes);
            if(read1 + gotlen < postFileSize) {
                bos.write(bytes, 0, read1);
            }else{
                read1 = postFileSize-gotlen;
                if(read1>0)
                    bos.write(bytes, 0, read1);
                break;
            }
            gotlen += read1;
        }
        //File tempfile = new File("/D/Projects/RunData/file_home/temp/tem.data");
        //FileIOOpt.writeInputStreamToFile(new ByteArrayInputStream(bos.toByteArray()),tempfile);
        ByteArrayEntity entity = new ByteArrayEntity(bos.toByteArray());
        httpPost.setEntity(entity);

        String jsonStr = HttpExecutor.httpExecute(httpClient, (HttpContext)null, (HttpRequestBase)httpPost);
        ResponseJSON resJson = null;
        try {
            resJson = ResponseJSON.valueOfJson(jsonStr);
        }catch(Exception e){
            throw new ObjectException(jsonStr, "解析返回json串失败");
        }
        if (resJson == null) {
            throw new ObjectException(jsonStr, "请求失败！");
        }
        return resJson.getDataAsObject(FileStoreInfo.class);

    }

    public FileStoreInfo uploadFileRange(FileStoreInfo fsi,File file,long rangeStart,long rangeSize) throws Exception{
        CloseableHttpClient httpClient = getHttpClient();
        FileStoreInfo upres = uploadFileRange(httpClient, fsi,file,rangeStart,rangeSize);
        releaseHttpClient(httpClient);
        return upres;
    }

    public FileStoreInfo getFileStoreInfo(String fileId) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        FileStoreInfo fileStoreInfo = getFileStoreInfo(httpClient, fileId);
        releaseHttpClient(httpClient);
        return fileStoreInfo;
    }

    public void downloadFileRange(CloseableHttpClient httpClient,
                                  String fileId, int offset, int lenght, String filePath) throws Exception {
        //CloseableHttpClient httpClient = appSession.getHttpClient();
        appSession.checkAccessToken(httpClient);

        HttpGet httpGet = new HttpGet(
                appSession.completeQueryUrl("/service/download/pfile/" + fileId));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (offset > -1 && lenght > 0) {
                httpGet.setHeader("Range", "bytes=" + offset + "-" + String.valueOf(offset + lenght - 1));
            }
            Header[] contentTypeHeader = response.getHeaders("Content-Type");
            if (contentTypeHeader == null || contentTypeHeader.length < 1 ||
                    StringUtils.indexOf(
                            contentTypeHeader[0].getValue(), "text/") >= 0
                    ) {
                String responseContent = Utf8ResponseHandler.INSTANCE
                        .handleResponse(response);
                throw new RuntimeException(responseContent);
            }
            try (InputStream inputStream = InputStreamResponseHandler.INSTANCE
                    .handleResponse(response)) {
                FileSystemOpt.createFile(inputStream, filePath);
            }
        }
        //appSession.releaseHttpClient(httpClient);
    }


    public void downloadFileRange(String fileId, int offset, int lenght, String filePath) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        downloadFileRange(httpClient, fileId, offset, lenght, filePath);
        releaseHttpClient(httpClient);
    }

    @Override
    public void downloadFile(CloseableHttpClient httpClient, String fileId, String filePath) throws Exception {
        downloadFileRange(httpClient, fileId, -1, -1, filePath);
    }

    @Override
    public void downloadFile(String fileId, String filePath) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        downloadFileRange(httpClient, fileId, -1, -1, filePath);
        releaseHttpClient(httpClient);
    }

}
