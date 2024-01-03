package com.centit.fileserver.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileFavorite;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileFavoriteManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.framework.model.basedata.OsInfo;
import com.centit.search.document.FileDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.database.utils.PageDesc;
import com.centit.support.json.JSONOpt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/files")
@Api(value = "文件管理", tags = "文件管理")
public class FileManagerController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(FileManagerController.class);

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    @Autowired(required = false)
    private ESServerConfig esServerConfig;

    public ESSearcher fetchDocumentSearcher(){
        if(esServerConfig==null)
            return null;
        return IndexerSearcherFactory.obtainSearcher(esServerConfig, FileDocument.class);
    }

    @Autowired(required = false)
    private FileFavoriteManager fileFavoriteManager;

    public ESIndexer fetchDocumentIndexer(){
        if(esServerConfig==null)
            return null;
        return IndexerSearcherFactory.obtainIndexer(esServerConfig, FileDocument.class);
    }

    /**
     * 根据文件的id物理删除文件(同时删除文件和数据库记录)
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param fileId   文件ID
     */
    @RequestMapping(value = "/{fileId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "根据文件的id逻辑删除文件(同时删除文件和数据库记录)")
    public void delete(@PathVariable("fileId") String fileId,HttpServletRequest request, HttpServletResponse response) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo != null) {
            fileInfoManager.deleteObjectById(fileId);
            if(esServerConfig == null){
                fetchDocumentIndexer().deleteDocument(fileId);
            }
            fileStoreInfoManager.decreaseFileReference(fileInfo.getFileMd5());
            OperationLogCenter.log(OperationLog.create()
                .operation(FileLogController.LOG_OPERATION_NAME)
                .user(WebOptUtils.getCurrentUserCode(request))
                .unit(fileInfo.getLibraryId())
                .topUnit(WebOptUtils.getCurrentTopUnit(request))
                .correlation(WebOptUtils.getCorrelationId(request))
                .loginIp(WebOptUtils.getRequestAddr(request))
                .method("删除文件").tag(fileInfo.getFileMd5())
                .content(fileInfo.getFileName()).oldObject(fileInfo));
            JsonResultUtils.writeSuccessJson(response);
        } else {
            JsonResultUtils.writeErrorMessageJson(
                "文件不存在：" + fileId, response);
        }
    }

    /**
     * 根据文件的id物理删除文件(同时删除文件和数据库记录)
     * @param fileId   文件ID
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/force/{fileId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "根据文件的id物理删除文件(同时删除文件和数据库记录)")
    public void deleteForce(@PathVariable("fileId") String fileId, HttpServletResponse response) {

        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo != null) {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            String path = fileStoreInfo.getFileStorePath();

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
        } else {
            JsonResultUtils.writeErrorMessageJson(
                "文件不存在：" + fileId, response);
        }

    }

    /**
     * 根据文件的id获取文件存储信息
     * @param request HttpServletRequest
     * @param fileId   文件ID
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的id获取文件存储信息")
    public void getFileStoreInfo(@PathVariable("fileId") String fileId, HttpServletRequest request, HttpServletResponse response) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        fileInfo.setFileState("N".equals(fileInfo.getFileState())?"正常":"删除");
        switch (fileInfo.getEncryptType()) {
            case "A":
                fileInfo.setEncryptType("AES加密文件");
                break;
            case "Z":
                fileInfo.setEncryptType("Zip压缩文件");
                break;
            default:
                fileInfo.setEncryptType("未加密");
        }
        fileInfo.setIndexState("I".equals(fileInfo.getIndexState())?"全文检索":"未检索");
        FileStoreInfo fileStoreInfo=fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
        if(fileStoreInfo!=null){
            fileInfo.setFileSize(fileStoreInfo.getFileSize());
        }
        if (fileInfo != null) {
            JsonResultUtils.writeSingleDataJson(fileInfo, response);
        } else {
            JsonResultUtils.writeErrorMessageJson(
                "文件不存在：" + fileId, response);
        }
    }

    @RequestMapping(value = "/size/{fileStoreUrl}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的id获取文件存储信息")
    @WrapUpResponseBody
    public Long getFileSizeByStoreUrl(@PathVariable("fileStoreUrl") String fileStoreUrl) {
        try {
            return fileStore.getFileSize(fileStoreUrl);
        } catch (IOException e) {
            logger.error("获取文件长度异常,文件储存路径"+fileStoreUrl+",错误信息："+e.getMessage() , e);
            return -1L;
        }
    }

    @RequestMapping(value = "/matchFileStoreUrl/{fileSize}", method = RequestMethod.POST)
    @ApiOperation(value = "获取文件存储url")
    @WrapUpResponseBody
    public String matchFileStoreUrl(@PathVariable("fileSize") String fileSize,
                                    @Valid FileInfo fileInfo) {
        return fileStore.matchFileStoreUrl(fileInfo,
            NumberBaseOpt.castObjectToLong(fileSize, 0L));
    }
    /**
     * 更新文件存储信息
     *
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */

    private void updateFileStoreInfo(String fileId, FileInfo fileInfo, HttpServletResponse response) {
        FileInfo dbFileInfo = fileInfoManager.getObjectById(fileId);
        if (dbFileInfo != null) {
            dbFileInfo.copyNotNullProperty(fileInfo);
            dbFileInfo.setFileName(fileInfo.getFileName());
            if (StringUtils.isBlank(fileInfo.getFileId())) {
                dbFileInfo.setFileId(null);
                fileInfoManager.saveNewFile(dbFileInfo);
            } else {
                fileInfoManager.updateObject(fileInfo);
            }
            JsonResultUtils.writeSingleDataJson(fileInfo, response);
        } else {
            JsonResultUtils.writeErrorMessageJson(
                "文件不存在：" + fileInfo.getFileId(), response);
        }
    }

    /**
     * 根据文件的id修改文件存储信息，文件春粗信息按照表单的形式传送
     *
     * @param fileId   文件ID
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{fileId}", method = RequestMethod.POST)
    @ApiOperation(value = "根据文件的id修改文件存储信息，文件信息按照表单的形式传送")
    public void postFileStoreInfo(@PathVariable("fileId") String fileId,
                                  @Valid FileInfo fileInfo, HttpServletResponse response) {
        updateFileStoreInfo(fileId, fileInfo, response);
    }

    /**
     * 根据文件的id修改文件存储信息，文件存储信息按照json的格式传送
     *
     * @param fileId   文件ID
     * @param fileInfo 文件对象
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/j/{fileId}", method = RequestMethod.POST)
    @ApiOperation(value = "根据文件的id修改文件存储信息，文件信息按照json的形式传送")
    public void jsonpostFileStoreInfo(@PathVariable("fileId") String fileId,
                                      @RequestBody FileInfo fileInfo, HttpServletResponse response) {
        updateFileStoreInfo(fileId, fileInfo, response);
    }

    /**
     * 根据相关的条件查询文件
     *
     * @param pageDesc 分页对象
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "根据相关的条件查询文件")
    public void listStoredFiles(PageDesc pageDesc,
                                HttpServletRequest request, HttpServletResponse response) {
        if("".equals(request.getParameter("files"))){
            return;
        }
        Map<String, Object> queryParamsMap = BaseController.collectRequestParameters(request);
        if(queryParamsMap.get("files")!=null){
            queryParamsMap.put("files", Arrays.asList(queryParamsMap.get("files").toString().split(",")));
        }
        JSONArray listObjects = fileInfoManager.listStoredFiles(queryParamsMap, pageDesc);
        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, listObjects);
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }


    /**
     * 获取系统中的所有OS
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/oss", method = RequestMethod.GET)
    @ApiOperation(value = "获取系统中的所有OS")
    public void listOperationSystem(HttpServletRequest request, HttpServletResponse response) {
        List<OsInfo> osinfoList = CodeRepositoryUtil.listOsInfo(
            WebOptUtils.getCurrentTopUnit(request));
        JsonResultUtils.writeSingleDataJson(osinfoList, response);
    }

    /**
     * 获取系统所有操作
     *
     * @param osId     项目编号
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/optids/{osId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取系统所有操作")
    public void listOptsByOs(@PathVariable("osId") String osId,
                             HttpServletResponse response) {
        JSONArray listObjects = fileInfoManager.listOptsByOs(osId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }


    /**
     * 获取系统所有文件属主
     *
     * @param osId     项目编号
     * @param optId    模块编号
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/owner/{osId}/{optId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取系统所有文件属主")
    public void listFileOwners(@PathVariable("osId") String osId,
                               @PathVariable("optId") String optId,
                               HttpServletResponse response) {
        JSONArray listObjects = fileInfoManager.listFileOwners(osId, optId);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }

    /**
     * 获取系统所有文件
     *
     * @param osId     项目编号
     * @param optId    模块编号
     * @param owner    所属者
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/files/{osId}/{optId}/{owner}", method = RequestMethod.GET)
    @ApiOperation(value = "获取系统所有文件")
    public void listFilesByOwner(@PathVariable("osId") String osId,
                                 @PathVariable("optId") String optId,
                                 @PathVariable("owner") String owner,
                                 HttpServletResponse response) {

        JSONArray listObjects = fileInfoManager.listFilesByOwner(osId, optId, owner);
        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }

    @RequestMapping(value = "/authcode/{fileId}", method = RequestMethod.GET)
    @ApiOperation(value = "根据文件的id获取验证码")
    @WrapUpResponseBody
    public Map<String, Object> getAuthCode(@PathVariable("fileId") String fileId, HttpServletRequest request) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (StringUtils.isBlank(fileInfo.getAuthCode())) {
            fileInfo.setAuthCode(StringUtils.substring(UuidOpt.getUuidAsString(), -4));
            fileInfoManager.updateObject(fileInfo);
        }
        OperationLogCenter.log(OperationLog.create()
            .operation(FileLogController.LOG_OPERATION_NAME)
            .user(WebOptUtils.getCurrentUserCode(request))
            .unit(fileInfo.getLibraryId())
            .topUnit(WebOptUtils.getCurrentTopUnit(request))
            .correlation(WebOptUtils.getCorrelationId(request))
            .loginIp(WebOptUtils.getRequestAddr(request))
            .method("分享").tag(fileId)
            .content(fileInfo.getFileName()).newObject(fileInfo));
        return CollectionsOpt.createHashMap("authcode", fileInfo.getAuthCode(),
            "uri", "/checkauth/" + fileId);
    }

    @RequestMapping(value = "/checkauth/{fileId}/{authCode}", method = RequestMethod.GET)
    @ApiOperation(value = "检查验证码")
    @WrapUpResponseBody
    public JSONObject checkAuthCode(@PathVariable("fileId") String fileId, @PathVariable("authCode") String authCode) {
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        if (fileInfo.getAuthCode().equals(authCode)) {
            return fileInfoManager.listStoredFiles(CollectionsOpt.createHashMap("files",fileId),null).getJSONObject(0);
        }
        return JSONOpt.objectToJSONObject("");
    }

    @ApiOperation(value = "全文检索")
    @ApiImplicitParams({@ApiImplicitParam(
        name = "libraryIds", value = "库ids",
        required = true, paramType = "query", dataType = "String", allowMultiple = true
    ), @ApiImplicitParam(
        name = "query", value = "检索关键字",
        required = true, paramType = "query", dataType = "String"
    )})
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @WrapUpResponseBody
    public PageQueryResult<Map<String, Object>> searchObject(String[] libraryIds, String query, HttpServletRequest request, PageDesc pageDesc) {
        if (esServerConfig == null) {
            throw new ObjectException(ObjectException.SYSTEM_CONFIG_ERROR, "没有正确配置Elastic Search");
        }
        Map<String, Object> searchQuery = new HashMap<>(10);
        if (libraryIds != null) {
            searchQuery.put("optId", libraryIds);
        }
        Pair<Long, List<Map<String, Object>>> res =
            fetchDocumentSearcher().search(searchQuery, query, pageDesc.getPageNo(), pageDesc.getPageSize());
        if (res == null) {
            throw new ObjectException("ELK异常");
        }
        pageDesc.setTotalRows(NumberBaseOpt.castObjectToInteger(res.getLeft()));
        return PageQueryResult.createResult(change(res.getRight(), WebOptUtils.getCurrentUserCode(request)), pageDesc);
    }

    private List<Map<String, Object>> change(List<Map<String, Object>> mapList, String userCode) {
        mapList.forEach(e -> {
            e.put("showPath", fileFavoriteManager.getShowPath(e.get("optUrl").toString(), e.get("optId").toString()));
            List<FileFavorite> list = fileFavoriteManager.listFileFavorite(
                CollectionsOpt.createHashMap("fileId", e.get("fileId"), "favoriteUser", userCode), null);
            if (list != null && list.size() > 0) {
                e.put("favoriteId", list.get(0).getFavoriteId());
            } else {
                e.put("favoriteId", "");
            }
        });
        return mapList;
    }
}
