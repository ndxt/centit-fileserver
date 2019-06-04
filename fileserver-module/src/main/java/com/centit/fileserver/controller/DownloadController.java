package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.ZipCompressor;
import com.centit.support.file.FileEncryptWithAes;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/download")
public class DownloadController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @Resource
    private FileInfoManager fileInfoManager;
    @Resource
    private FileAccessLogManager fileAccessLogManager;

    @Autowired
    protected FileStore fileStore;

    private static void downFileRange(HttpServletRequest request, HttpServletResponse response,
            InputStream inputStream,long fSize, String fileName)
            throws IOException {
         UploadDownloadUtils.downFileRange(request, response,
                 inputStream, fSize, fileName);
    }

    public static void downloadFile(FileStore fileStore, FileInfo fileInfo, HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        if (null != fileInfo) {

            //对加密的进行特殊处理，ZIP加密的无需处理
            String password = request.getParameter("password");
            if("D".equals(fileInfo.getEncryptType()) && StringUtils.isNotBlank(password) ){
                String tmpFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(),fileInfo.getFileSize() );
                File tmpFile = new File(tmpFilePath);
                try(InputStream downFile = fileStore.loadFileStream(fileInfo.getFileStorePath());
                    OutputStream diminationFile = new FileOutputStream(tmpFile)    ){
                    FileEncryptWithAes.decrypt(downFile, diminationFile, password);
                }catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    JsonResultUtils.writeHttpErrorMessage(
                            FileServerConstant.ERROR_FILE_ENCRYPT,
                            "解码文件失败："+e.getMessage(),
                            response);
                    return;
                }
                try(InputStream inputStream = new FileInputStream(tmpFile)){
                    downFileRange(request, response,
                            inputStream,tmpFile.length(), fileInfo.getFileName());
                }

                FileSystemOpt.deleteFile(tmpFile);
            }else{
                downFileRange(request, response,
                        fileStore.loadFileStream(fileInfo.getFileStorePath()),
                        fileInfo.getFileSize(), fileInfo.getFileName());
            }
        } else {
            JsonResultUtils.writeHttpErrorMessage(
                    FileServerConstant.ERROR_FILE_NOT_EXIST, "找不到该文件", response);
        }
    }


    /**
     * 根据文件的id下载附属文件
     * 这个需要权限 控制 用于内部服务之间文件传输
     * @param fileId 文件ID
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException 异常
     */
    @RequestMapping(value= "/pattach/{fileId}",method=RequestMethod.GET)
    public void downloadAttach(@PathVariable("fileId") String fileId,  HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);

        if (null != fileInfo) {
            String at = fileInfo.getAttachedType();
            if("N".equals(at)){
                JsonResultUtils.writeHttpErrorMessage(
                        FileServerConstant.ERROR_FILE_NOT_EXIST, "该文件没有附属文件", response);
                return ;
            }
            String fileName = fileInfo.getFileName();
            if("P".equals(at)){
                if (fileName.lastIndexOf(".") != -1){
                    fileName = fileName.substring(0,fileName.lastIndexOf("."))+".pdf" ;
                }
            }

            downFileRange(request, response,
                    fileStore.loadFileStream(fileInfo.getAttachedStorePath()),
                    fileStore.getFileSize(fileInfo.getAttachedStorePath()),fileName );
        } else {
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "找不到该文件", response);
        }
    }
    // 文件目录 = 配置目录 + file.getFileStorePath()

    /**
     * 根据文件的id下载文件
     * 这个需要权限 控制 用于内部服务之间文件传输
     * @param fileId 文件ID
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value= "/pfile/{fileId}", method=RequestMethod.GET)
    public void downloadByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);

        downloadFile(fileStore, fileInfo, request, response);
    }

    /**
     * 根据文件的 access_token 下载文件
     * @param token token
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value= "/file/{token}", method=RequestMethod.GET)
    public void downloadByAccessToken(
            @PathVariable("token") String token, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // 根据访问日志的id和授权的token查看是否已经被授权
        FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
        if(fileAccessLog!=null){
            if(fileAccessLog.checkValid(false)){
                FileInfo fileInfo = fileInfoManager.getObjectById(fileAccessLog.getFileId());
                downloadFile(fileStore, fileInfo, request ,response);
                // 记录访问日志
                fileAccessLog.chargeAccessTimes();
                fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
                fileAccessLog.setLastAccessHost(request.getLocalAddr());
                fileAccessLogManager.updateObject(fileAccessLog);
            }else{
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
                        "没有权限访问该文件或者访问授权已过期！", response);
            }
        }else{
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "找不到该文件或者您没有权限访问该文件！", response);
        }
    }

    /**
     * 根据access_token下载附属文件
     * @param token token
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value= "/attach/{token}", method=RequestMethod.GET)
    public void downloadAttachByAccessToken(
            @PathVariable("token") String token, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // 根据访问日志的id和授权的token查看是否已经被授权
        FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
        // 判断权限
        if(fileAccessLog!=null){
            if(fileAccessLog.checkValid(true)){
                downloadAttach( fileAccessLog.getFileId(), request ,response);
                // 记录访问日志
                fileAccessLog.chargeAccessTimes();
                fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
                fileAccessLog.setLastAccessHost(request.getLocalAddr());
                fileAccessLogManager.updateObject(fileAccessLog);
            }else{
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
                        "没有权限访问该文件或者访问授权已过期！", response);
            }
        }else{
            JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "找不到该文件或者您没有权限访问该文件！", response);
        }
    }

    /**
     * 根据文件的 MD5码 下载不受保护的文件，不需要访问文件记录
     * 如果是通过 store 上传的需要指定 extName 扩展名
     * @param md5SizeExt 文件的Md5码和文件的大小 格式为 MD5_SIZE.EXT
     * @param fileName 文件的名称包括扩展名，如果这个不为空， 上面的 md5SizeExt 可以没有 .Ext 扩展名
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @RequestMapping(value= "/unprotected/{md5SizeExt}", method=RequestMethod.GET)
    public void downloadUnprotectedFile(@PathVariable("md5SizeExt") String md5SizeExt,
                                 String fileName,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        //FileStoreInfo stroeInfo = fileStoreInfoManager.getObjectById(md5);
        //downloadFile(stroeInfo,request,response);
        String uri = request.getRequestURI();
        String [] urips = uri.split("/");
        int n=urips.length;
        if(StringUtils.isBlank(fileName)){
            fileName = urips[n-1];
        }
        String fileMd5 =  md5SizeExt.substring(0,32);
        int pos = md5SizeExt.indexOf('.');
        //String extName = md5SizeExt.substring(pos);
        long fileSize = pos<0?NumberBaseOpt.parseLong(md5SizeExt.substring(33),0l)
                            :NumberBaseOpt.parseLong(md5SizeExt.substring(33,pos),0l);

        String filePath = fileStore.getFileStoreUrl(fileMd5, fileSize);
        InputStream inputStream = fileStore.loadFileStream(filePath);
        downFileRange(request,  response,
                inputStream, fileSize,
                fileName);
    }

    private void compressFiles(String zipFilePathName, String[] fileUrls, String[] fileNames, int len) {
        try {
            File zipFile = new File(zipFilePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);

            ZipOutputStream out = ZipCompressor.convertToZipOutputStream(fileOutputStream);
            // new ZipOutputStream(cos);
            String basedir = "";

            for(int i=0; i<len; i++){
                try(InputStream fis = fileStore.loadFileStream(fileUrls[i])) {
                    ZipCompressor.compressFile(fis, fileNames[i], out, basedir);
                }catch (Exception e) {
                    logger.info("获取文件"+ fileUrls[i] +"出错！");
                }
            }
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量下载文件
     * @param fileIds 批量下载文件列表
     * @param fileName 文件名
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException 异常
     */
    @RequestMapping(value= "/batchdownload", method=RequestMethod.GET)
    public void batchDownloadFile(String[] fileIds,
                                        String fileName,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        if(fileIds == null || fileIds.length==0){
            JsonResultUtils.writeMessageJson("请提供文件id列表",response);
            return;
        }

        InputStream inputStream = null;
        long fileSize ;
        // 如果只下载一个文件则 不压缩
        if(fileIds.length == 1){
            FileInfo fileInfo = fileInfoManager.getObjectById(fileIds[0]);
            fileSize = fileInfo.getFileSize();
            String filePath = fileStore.getFileStoreUrl(fileInfo.getFileMd5(), fileSize);
            inputStream = fileStore.loadFileStream(filePath);
        } else {
            StringBuilder fileIdSb = new StringBuilder();
            Arrays.sort(fileIds, String::compareTo);
            for(String fid : fileIds){
                fileIdSb.append(fid);
            }
            // 用所有的fileid（排序）的md5 作为文件名保存在临时目录中
            // 如果临时目录中已经有对应的文件直接下载，如果没有 打包下载
            String fileId = Md5Encoder.encode(fileIdSb.toString());
            String tempFilePath = SystemTempFileUtils.getTempFilePath(fileId, 1024);
            File file = new File(tempFilePath);
            if(! file.exists()) {

                int len = fileIds.length;
                String[] fileUrls = new String[len];
                String[] fileNames = new String[len];
                int j = 0;
                for (int i = 0; i < len; i++) {
                    FileInfo si = fileInfoManager.getObjectById(fileIds[i]);
                    if (si != null) {
                        fileUrls[j] = fileStore.getFileStoreUrl(si.getFileMd5(), si.getFileSize());
                        fileNames[j] = si.getFileName();
                        j++;
                    }
                }
                if (j == 0) {
                    JsonResultUtils.writeMessageJson("请提供文件id列表", response);
                    return;
                }

                compressFiles(tempFilePath, fileUrls, fileNames, j);
            }
            file = new File(tempFilePath);
            fileSize = file.length();
            inputStream = new FileInputStream(file);
        }

        downFileRange(request,  response,
            inputStream, fileSize,
            fileName);
    }
}
