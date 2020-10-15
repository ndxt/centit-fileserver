package com.centit.fileserver.controller;

import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpContentType;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        return this.optLogManager.listOptLog(LOG_OPERATION_NAME, searchColumn, pageDesc.getPageNo(), pageDesc.getPageSize());
    }

}
