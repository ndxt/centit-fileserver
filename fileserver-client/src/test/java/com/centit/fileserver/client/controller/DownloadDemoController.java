package com.centit.fileserver.client.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.centit.fileserver.client.FileClient;
import com.centit.framework.core.controller.BaseController;

@Controller
@RequestMapping("/demo")
public class DownloadDemoController extends BaseController {

    //private static final Logger logger = LoggerFactory.getLogger(DownLoadController.class);

    @Resource
    protected FileClient fileClient;

    /**
     * 根据文件的id下载文件
     *
     * @param attachId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/download/{attachId}", method = RequestMethod.GET)
    public String downloadFile(@PathVariable("attachId") String attachId, HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        String fileId = attachId;//根据 attachId获得 对应的fileID
        String downloadUrl = fileClient.getFileUrl(fileId, 1440);//一天有效期
        return "redirect:" + downloadUrl;
    }


}