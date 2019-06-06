package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.utils.FileStore;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.ip.po.OsInfo;
import com.centit.framework.ip.service.IntegrationEnvironment;
import com.centit.support.database.utils.PageDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/files")
public class FileManagerController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(FileManagerController.class);

    @Resource
    private FileInfoManager fileInfoManager;

    @Resource
    private IntegrationEnvironment integrationEnvironment;

    @Resource
    protected FileStore fileStore;
    /**
     * 根据文件的id物理删除文件(同时删除文件和数据库记录)
     * @param fileId 文件ID
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{fileId}",method = RequestMethod.DELETE)
    public void delete(@PathVariable("fileId") String fileId, HttpServletResponse response){

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(fileInfo !=null){
            fileInfo.setFileState("D");
            fileInfoManager.updateObject(fileInfo);
            JsonResultUtils.writeSuccessJson(response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "文件不存在："+fileId, response);
        }

    }
    /**
     * 根据文件的id物理删除文件(同时删除文件和数据库记录)
     * @param fileId 文件ID
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/force/{fileId}",method = RequestMethod.DELETE)
    public void deleteForce(@PathVariable("fileId") String fileId, HttpServletResponse response){

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(fileInfo !=null){
            String path= fileInfo.getFileStorePath();

            try {
                fileStore.deleteFile(path);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                JsonResultUtils.writeErrorMessageJson(
                        e.getMessage(), response);
                return;
            }
            fileInfo.setFileState("D");
            fileInfoManager.updateObject(fileInfo);
            JsonResultUtils.writeSuccessJson(response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "文件不存在："+fileId, response);
        }

    }

    /**
     * 根据文件的id获取文件存储信息
     * @param fileId 文件ID
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{fileId}",method = RequestMethod.GET)
    public void getFileStoreInfo(@PathVariable("fileId") String fileId, HttpServletResponse response){

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if(fileInfo !=null){
            JsonResultUtils.writeSingleDataJson(fileInfo, response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "文件不存在："+fileId, response);
        }
    }

    /**
     * 更新文件存储信息
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */

    private void updateFileStoreInfo(FileInfo fileInfo, HttpServletResponse response){
        FileInfo dbFileInfo = fileInfoManager.getObjectById(fileInfo.getFileId());

        if(dbFileInfo !=null){
            dbFileInfo.copyNotNullProperty(fileInfo);
            fileInfoManager.updateObject(dbFileInfo);
            JsonResultUtils.writeSingleDataJson(fileInfo, response);
        }else{
            JsonResultUtils.writeErrorMessageJson(
                    "文件不存在："+fileInfo.getFileId(), response);
        }
    }

    /**
     * 根据文件的id修改文件存储信息，文件春粗信息按照表单的形式传送
     * @param fileId 文件ID
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{fileId}",method = RequestMethod.POST)
    public void postFileStoreInfo(@PathVariable("fileId") String fileId,
            @Valid FileInfo fileInfo, HttpServletResponse response){
        fileInfo.setFileId(fileId);
        updateFileStoreInfo(fileInfo,response);
    }

    /**
     * 根据文件的id修改文件存储信息，文件存储信息按照json的格式传送
     * @param fileId 文件ID
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/j/{fileId}",method = RequestMethod.POST)
    public void jsonpostFileStoreInfo(@PathVariable("fileId") String fileId,
            @RequestBody FileInfo fileInfo, HttpServletResponse response){
        fileInfo.setFileId(fileId);
        updateFileStoreInfo(fileInfo,response);
    }

    /**
     * 根据相关的条件查询文件
     * @param pageDesc 分页对象
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(method = RequestMethod.GET)
    public void listStroedFiles( PageDesc pageDesc,
            HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> queryParamsMap = convertSearchColumn(request);

        JSONArray listObjects = fileInfoManager.listStoredFiles(queryParamsMap, pageDesc);
        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, listObjects);
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }


    /**
     * 获取系统中的所有OS
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/oss",method = RequestMethod.GET)
    public void listOperationSystem(HttpServletRequest request, HttpServletResponse response) {
        List<OsInfo> osinfoList = integrationEnvironment.listOsInfos();
        JsonResultUtils.writeSingleDataJson(osinfoList, response);
    }


    /**
     * 获取系统所有操作
     * @param osId 项目编号
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/optids/{osId}",method = RequestMethod.GET)
    public void listOptsByOs(@PathVariable("osId") String osId,
                             HttpServletResponse response) {
        JSONArray listObjects = fileInfoManager.listOptsByOs(osId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }


    /**
     * 获取系统所有文件属主
     * @param osId 项目编号
     * @param optId 模块编号
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/owner/{osId}/{optId}",method = RequestMethod.GET)
    public void listFileOwners(@PathVariable("osId") String osId,
                             @PathVariable("optId") String optId,
                             HttpServletResponse response) {
        JSONArray listObjects = fileInfoManager.listFileOwners(osId,optId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }

    /**
     * 获取系统所有文件
     * @param osId 项目编号
     * @param optId 模块编号
     * @param owner 所属者
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/files/{osId}/{optId}/{owner}",method = RequestMethod.GET)
    public void listFilesByOwner(@PathVariable("osId") String osId,
                               @PathVariable("optId") String optId,
                                 @PathVariable("owner") String owner,
                               HttpServletResponse response) {

        JSONArray listObjects = fileInfoManager.listFilesByOwner(osId,optId,owner);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }
}
