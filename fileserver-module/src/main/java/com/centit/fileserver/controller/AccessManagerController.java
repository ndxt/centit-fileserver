package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileUploadAuthorizedManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.common.ResponseSingleData;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/access")
@Api(value = "文件授权日志", tags = "文件授权日志")
public class AccessManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(AccessManagerController.class);

    @Autowired
    private FileAccessLogManager fileAccessLogManager;
    @Autowired
    private FileInfoManager fileInfoManager;
    @Autowired
    private FileUploadAuthorizedManager fileUploadAuthorizedManager;

    private ResponseSingleData applyAccess(FileAccessLog accessLog) {
        String fileId = accessLog.getFileId();
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(fileInfo==null){
            return new ResponseSingleData(
                    FileServerConstant.ERROR_FILE_NOT_EXIST,
                    "文件不存："+fileId);
        }
        if(StringUtils.isBlank(accessLog.getAccessRight())) {
            accessLog.setAccessRight("A");
        }
        //accessLog.setFileId(fileId);
        accessLog.setAccessToken( UuidOpt.getUuidAsString32());
        accessLog.setAuthTime(DatetimeOpt.currentUtilDate());
        fileInfo.addDownloadTimes();
        fileAccessLogManager.saveNewAccessLog(accessLog);
        fileInfoManager.updateObject(fileInfo);
        return ResponseSingleData.makeResponseData(accessLog);
    }

    @RequestMapping(value="/apply", method = RequestMethod.POST)
    @ApiOperation(value = "新增文件授权")
    public void accessFile(@Valid FileAccessLog accessLog , HttpServletResponse response) throws Exception{
        JsonResultUtils.writeOriginalObject(
            applyAccess(accessLog), response);
    }



    @RequestMapping(value="/log/{token}", method = RequestMethod.GET)
    @ApiOperation(value = "查询单个授权")
    public void getAccessLog(@PathVariable("token") String token,
             HttpServletResponse response){

        FileAccessLog accessLog = fileAccessLogManager.getObjectById(token);
        if(accessLog !=null){
            JsonResultUtils.writeSingleDataJson(accessLog, response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "请求日志不存在："+token, response);
        }
    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    @ApiOperation(value = "查询所有授权列表")
    public void listAccessLog( PageDesc pageDesc,
            HttpServletRequest request, HttpServletResponse response){

        Map<String, Object> queryParamsMap = BaseController.collectRequestParameters(request);

        JSONArray listObjects = fileAccessLogManager.listAccessLog(queryParamsMap, pageDesc);
        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, listObjects);
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }

    @RequestMapping(value="/applyUpload", method = RequestMethod.POST)
    @ApiOperation(value = "新增文件上传授权")
    public void applyUploadFiles(HttpServletResponse response) throws Exception{
        FileUploadAuthorized authorized = fileUploadAuthorizedManager.createNewAuthorization(1);
        JsonResultUtils.writeSingleDataJson(
                authorized, response);
    }
}
