package com.centit.fileserver.demo.fileserverclient;

import com.centit.fileserver.client.FileClient;
import com.centit.framework.core.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DownloadDemoController extends BaseController {

    //private static final Logger logger = LoggerFactory.getLogger(DownLoadController.class);

    @Autowired
    protected FileClient fileClient;

    /**
     * 根据文件的id下载文件
     *
     * @param attachId 文件ID
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return String
     * @throws Exception Exception
     */
    @RequestMapping(value = "/download/{attachId}", method = RequestMethod.GET)
    public String downloadFile(@PathVariable("attachId") String attachId, HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        String fileId = attachId;//根据 attachId获得 对应的fileID
        String downloadUrl = fileClient.getFileUrl(fileId, 1440);//一天有效期
        return "redirect:" + downloadUrl;
    }


}
