package com.centit.fileserver.demo.localstore;

import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.controller.FileController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/file")
public class FileUpAndDownController extends FileController {
    @Override
    protected void fileUploadCompleteOpt(String fileMd5, long size, JSONObject retJson) {

    }
}
