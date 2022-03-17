package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 本地文件控制器
 */
@Controller
@RequestMapping("/local")
@Api(value = "本地文件控制器", tags = "本地文件控制器")
public class LocalFileController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(LocalFileController.class);
    private static final int URI_START_PARAM = 5;
    @Autowired
    private LocalFileManager localFileManager;

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    protected FileStore fileStore;

    /**
     * 获取当前用户的文件类别
     * 个人文件，和本人所属的部门
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/catalog", method = RequestMethod.GET)
    @ApiOperation(value = "获取当前用户的文件类别")
    public void getFileCatalog(HttpServletRequest request, HttpServletResponse response) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        JsonResultUtils.writeSingleDataJson(
            localFileManager.listUserUnit(topUnit, userCode), response);
    }

    /**
     * 返回当前访问目录，顶级目录时返回null
     * fixme-zouwy URL如果没有项目名或service时 长度 != URI_START_PARAM = 5
     * @param uri 文件url
     * @return 当前路径
     * @throws UnsupportedEncodingException 编码异常
     */
    public static String fetchUserShowPath(String uri) throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"));
        for (int i = URI_START_PARAM + 1; i < n; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(
                URLDecoder.decode(urips[i], "UTF-8"));
        }
        return sb.toString();
    }

    public static ImmutablePair<String, String> fetchUserFilePath(String uri)
        throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 1) {
            return null;
        }
        if (n == URI_START_PARAM + 1) {
            return new ImmutablePair<>("", URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"));
        }

        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"));
        for (int i = URI_START_PARAM + 1; i < n - 1; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutablePair<>(sb.toString(), URLDecoder.decode(urips[n - 1], "UTF-8"));
    }

    public static ImmutablePair<String, String> fetchUnitShowPath(String uri)
        throws UnsupportedEncodingException {
        //URLDecoder.decode(uri,"UTF-8");
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 1) {
            return null;
        }
        if (n == URI_START_PARAM + 1) {
            return new ImmutablePair<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"), "");
        }
        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        for (int i = URI_START_PARAM + 2; i < n; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutablePair<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"), sb.toString());
    }

    public static ImmutableTriple<String, String, String> fetchUnitFilePath(String uri)
        throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 2) {
            return null;
        }
        if (n == URI_START_PARAM + 2) {
            return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
                "", URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        }
        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        for (int i = URI_START_PARAM + 2; i < n - 1; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
            sb.toString(), URLDecoder.decode(urips[n - 1], "UTF-8"));
    }

    /**
     * 获取个人文件列表  {showPath}
     * PathVariable("showPath") String showPath
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/userdir/**", method = RequestMethod.GET)
    @ApiOperation(value = "获取个人文件列表")
    public void listUserFiles(HttpServletRequest request,
                              HttpServletResponse response) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        String uri = request.getRequestURI();
        try {
            JsonResultUtils.writeSingleDataJson(
                localFileManager.listUserFiles(userCode, fetchUserShowPath(uri)),
                response);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson("url 解析出错:" + e.getMessage(), response);
        }
    }

    /**
     * 获取机构文件列表 {unitCode}/{showPath}
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/unitdir/**", method = RequestMethod.GET)
    @ApiOperation(value = "获取机构文件列表")
    public void listUnitFiles(HttpServletRequest request,
                              HttpServletResponse response) {
        try {
            String uri = request.getRequestURI();
            ImmutablePair<String, String> p = fetchUnitShowPath(uri);
            if (p == null) {
                JsonResultUtils.writeErrorMessageJson("不正确的路径！", response);
                return;
            }

            JsonResultUtils.writeSingleDataJson(
                localFileManager.listUnitFiles(p.getLeft(), p.getRight()),
                response);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson("url 解析出错:" + e.getMessage(), response);
        }
    }

    /**
     * 获取个人文件版本信息 {showPath}/{fileName}
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/userfile/**", method = RequestMethod.GET)
    @ApiOperation(value = "获取个人文件版本信息")
    public void listUserFileVersion(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userCode = WebOptUtils.getCurrentUserCode(request);
            String uri = request.getRequestURI();
            ImmutablePair<String, String> p = fetchUserFilePath(uri);
            if (p == null) {
                JsonResultUtils.writeErrorMessageJson("不正确的路径！", response);
                return;
            }
            JsonResultUtils.writeSingleDataJson(
                localFileManager.listUserFileVersions(userCode, p.getLeft(), p.getRight()),
                response);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson("url 解析出错:" + e.getMessage(), response);
        }
    }

    /**
     * 获取机构文件版本信息 {unitCode}/{showPath}/{fileName}
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/unitfile/**", method = RequestMethod.GET)
    @ApiOperation(value = "获取机构文件版本信息")
    public void listUnitFileVersion(HttpServletRequest request,
                                    HttpServletResponse response) {
        try {
            String uri = request.getRequestURI();
            ImmutableTriple<String, String, String> t = fetchUnitFilePath(uri);
            if (t == null) {
                JsonResultUtils.writeErrorMessageJson("不正确的路径！", response);
                return;
            }
            JsonResultUtils.writeSingleDataJson(
                localFileManager.listUnitFileVersions(t.getLeft(), t.getMiddle(), t.getRight()),
                response);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson("url 解析出错:" + e.getMessage(), response);
        }
    }




}
