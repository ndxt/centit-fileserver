package com.centit.fileserver.demo.localstore;

import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ObjectException;
import com.centit.framework.common.ResponseData;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/download")

public class DownloadFileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileController.class);
    /**
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param inputStream InputStream
     * @param fSize long
     * @param fileName String
     * @throws IOException IOException
     */
    private static void downFileRange(HttpServletRequest request, HttpServletResponse response,
                                      InputStream inputStream, long fSize, String fileName)
            throws IOException {
        UploadDownloadUtils.downFileRange(request, response,
                inputStream, fSize,UploadDownloadUtils.encodeDownloadFilename(fileName));
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
    @RequestMapping(value= "/download/{md5SizeExt}", method=RequestMethod.GET)
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
        long fileSize = pos<0? NumberBaseOpt.parseLong(md5SizeExt.substring(33),0l)
                : NumberBaseOpt.parseLong(md5SizeExt.substring(33,pos),0l);
        FileStore fs = FileStoreFactory.createDefaultFileStore();
        String filePath = fs.getFileStoreUrl(fileMd5, fileSize);
        InputStream inputStream = fs.loadFileStream(filePath);
        downFileRange(request,  response,
                inputStream, fileSize,
                fileName);
    }

}