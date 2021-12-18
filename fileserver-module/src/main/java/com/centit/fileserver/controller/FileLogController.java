package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpContentType;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author zhf
 */
@Controller
@RequestMapping("/log")
@Api(value = "文件日志", tags = "文件日志")
public class FileLogController extends BaseController {
    public static final String LOG_OPERATION_NAME = "FileServerLog";
    private final OperationLogWriter optLogManager;
    @Autowired
    private FileLibraryInfoManager fileLibraryInfoManager;

    public FileLogController(OperationLogWriter optLogManager) {
        this.optLogManager = optLogManager;
    }

    @ApiOperation(
        value = "查询文件日志列表",
        notes = "request参数：optTag(文件id),userCode(操作人员)"
    )
    @GetMapping
    @WrapUpResponseBody(contentType = WrapUpContentType.MAP_DICT)
    public List<? extends OperationLog> listFileLog(PageDesc pageDesc, HttpServletRequest request) {
        //TODO 获取当前人员所在库数组 编辑 unitCode in 这个数组
        Map<String, Object> searchColumn = BaseController.collectRequestParameters(request);
        List<FileLibraryInfo> fileLibraryInfos=fileLibraryInfoManager.listFileLibraryInfo(WebOptUtils.getCurrentUserCode(request));
        if(fileLibraryInfos!=null){
            String[] units=new String[fileLibraryInfos.size()];
            for(int i=0;i<fileLibraryInfos.size();i++){
                units[i]=fileLibraryInfos.get(i).getLibraryId();
            }
            searchColumn.put("unitCode_in",units);
        }
        return this.optLogManager.listOptLog(LOG_OPERATION_NAME, searchColumn, pageDesc.getPageNo(), pageDesc.getPageSize());
    }

}
