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
import com.centit.support.algorithm.*;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/access")
public class AccessManagerController extends BaseController {
    @Resource
    private FileAccessLogManager fileAccessLogManager;
    @Resource
    private FileInfoManager fileInfoManager;
    @Resource
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
    public void accessFile(@Valid FileAccessLog accessLog , HttpServletResponse response) throws Exception{
        JsonResultUtils.writeOriginalObject(
            applyAccess(accessLog), response);
    }

    @RequestMapping(value="/japply", method = RequestMethod.POST )//, headers="content-type=application/json")
    @ResponseBody
    public ResponseSingleData accessFileByJson(@RequestBody FileAccessLog accessLog) throws Exception{
        //return applyAccess(JSON.parseObject(accessLog,FileAccessLog.class));
        return applyAccess(accessLog);
    }

    @RequestMapping(value="/log/{token}", method = RequestMethod.GET)
    public void getAccessLog(@PathVariable("token") String token,
            HttpServletRequest request, HttpServletResponse response){

        FileAccessLog accessLog = fileAccessLogManager.getObjectById(token);
        if(accessLog !=null){
            JsonResultUtils.writeSingleDataJson(accessLog, response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "请求日志不存在："+token, response);
        }
    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public void listAccessLog( PageDesc pageDesc,
            HttpServletRequest request, HttpServletResponse response){

        Map<String, Object> queryParamsMap = convertSearchColumn(request);

        JSONArray listObjects = fileAccessLogManager.listAccessLog(queryParamsMap, pageDesc);
        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, listObjects);
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }

    @RequestMapping(value="/list/{fileId}", method = RequestMethod.GET)
    public void listAccessLogByFileId(@PathVariable("fileId") String fileId, PageDesc pageDesc,
            HttpServletRequest request, HttpServletResponse response){

        Map<String, Object> filterMap = convertSearchColumn(request);
        filterMap.put("fileId", fileId);

        JSONArray listObjects = fileAccessLogManager.listObjectsAsJson(filterMap, pageDesc);
        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, listObjects);
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }

    @RequestMapping(value="/applyUpload", method = RequestMethod.POST)
    public void applyUploadFiles(HttpServletResponse response) throws Exception{
        FileUploadAuthorized authorized = fileUploadAuthorizedManager.createNewAuthorization(1);
        JsonResultUtils.writeSingleDataJson(
                authorized, response);
    }

    @RequestMapping(value="/applyUpload/{maxFiles}", method = RequestMethod.POST)
    public void applyUploadManyFiles( @PathVariable("maxFiles") String maxFiles, HttpServletResponse response) throws Exception{
        int maxUploadFiles = 1;
        if(StringRegularOpt.isNumber(maxFiles)){
            maxUploadFiles = NumberBaseOpt.castObjectToInteger(maxFiles);
        }
        FileUploadAuthorized authorized =
                fileUploadAuthorizedManager.createNewAuthorization(maxUploadFiles);
        JsonResultUtils.writeSingleDataJson(
                authorized, response);
    }

}
