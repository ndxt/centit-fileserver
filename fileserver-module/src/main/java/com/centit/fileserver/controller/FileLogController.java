package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.model.adapter.OperationLogWriter;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author zhf
 */
@Controller
@RequestMapping("/log")
@Api(value = "文件日志", tags = "文件日志")
public class FileLogController extends BaseController {

    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件日志")
    @WrapUpResponseBody
    public void createFileLog(String userCode,
                              String method, String fileId,String logDetail){
        OperationLogCenter.log(OperationLog.create().operation("FileServerLog").user(userCode)
        .method(method).tag(fileId).time(DatetimeOpt.currentUtilDate()).content(logDetail));
    }

}
