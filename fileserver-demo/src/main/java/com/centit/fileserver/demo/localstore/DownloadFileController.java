package com.centit.fileserver.demo.localstore;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.NumberBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/download")

public class DownloadFileController extends BaseController {

    @Autowired
    protected FileStore fileStore;

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

        String filePath = fileStore.getFileStoreUrl(fileMd5, fileSize);
        InputStream inputStream = fileStore.loadFileStream(filePath);
        downFileRange(request,  response,
                inputStream, fileSize,
                fileName);
    }

}
