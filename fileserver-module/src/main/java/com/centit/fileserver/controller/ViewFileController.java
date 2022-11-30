package com.centit.fileserver.controller;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.fileserver.utils.FileIOUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.tika.detect.AutoDetectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/"+ViewFileController.URI_FUNC)
@Api(value = "文件按路径预览", tags = "文件按路径预览")
public class ViewFileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ViewFileController.class);
    public static final String URI_FUNC = "view";

    @Autowired
    private FileInfoManager fileInfoManager;

    @Autowired
    private FileStoreInfoManager fileStoreInfoManager;

    @Autowired
    private FileLibraryInfoManager fileLibraryInfoManager;


    @Autowired
    protected FileStore fileStore;

    @Autowired
    protected CreatePdfOpt createPdfOpt;

    public static ImmutableTriple<String, List<String>, String> fetchUnitFilePath(String uri)
        throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int i = 0, n = urips.length;
        while (i < n && !ViewFileController.URI_FUNC.equals(urips[i])) {
            i++;
        }
        i++;
        if (i >= n-1) {
            return null;
        }
        String libCode =URLDecoder.decode(urips[i], "UTF-8");// TOP_UNIT FILE_LIBRARY
        List<String> path = new ArrayList<>();
        for (; i < n - 1; i++){
            path.add(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutableTriple<>(libCode,
            path, URLDecoder.decode(urips[n - 1], "UTF-8"));
    }

    private boolean noAuth(HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo) {
        String userCode = WebOptUtils.getCurrentUserCode(request);
        userCode = StringBaseOpt.isNvl(userCode) ? request.getParameter("userCode") : userCode;
        if (!fileLibraryInfoManager.checkAuth(fileInfo, userCode, request.getParameter("authCode"))) {
            JsonResultUtils.writeErrorMessageJson("用户:" + WebOptUtils.getCurrentUserCode(request)
                + ",所属机构:" + WebOptUtils.getCurrentUnitCode(request) + "没有权限;或者验证码" + request.getParameter("authCode") + "不正确", response);
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/**}", method = RequestMethod.GET)
    @ApiOperation(value = "根据路径预览文件")
    public void previewFile(HttpServletRequest request, HttpServletResponse response) {
        try {
            String uri = request.getRequestURI();
            ImmutableTriple<String, List<String>, String> t = fetchUnitFilePath(uri);
            if (t == null) {
                JsonResultUtils.writeErrorMessageJson("不正确的路径！", response);
                return;
            }
            FileInfo fileInfo = fileInfoManager.getListVersionFileByPath(t.getLeft(),
                t.getMiddle(), t.getRight());

            if ( fileInfo==null || noAuth(request, response, fileInfo)) {
                return;
            }

            boolean canView = false;

            if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(),
                "txt", "html", "csv", "pdf", "xml")) {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
                String charset = null;
                if (StringUtils.equalsAnyIgnoreCase(fileInfo.getFileType(),
                    "txt", "csv")) {
                    charset = new AutoDetectReader(FileIOUtils.getFileStream(fileStore, fileStoreInfo)).getCharset().name();
                }
                UploadDownloadUtils.downFileRange(request, response,
                    FileIOUtils.getFileStream(fileStore, fileStoreInfo),
                    fileStoreInfo.getFileSize(), fileInfo.getFileName(), "inline", charset);
                canView = true;
            } else if (StringUtils.isNotBlank(fileInfo.getAttachedFileMd5())) {
                FileStoreInfo attachedFileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
                if (attachedFileStoreInfo != null && attachedFileStoreInfo.getFileSize() > 0) {
                    UploadDownloadUtils.downFileRange(request, response,
                        FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo),
                        attachedFileStoreInfo.getFileSize(),
                        FileType.truncateFileExtName(fileInfo.getFileName())
                            + "." + fileInfo.getAttachedType(),
                        "inline", null);
                    canView = true;
                } else {
                    canView = FileIOUtils.reGetPdf(fileInfo.getFileId(), request, response, fileInfo,
                        fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
                }
            } else {
                canView = FileIOUtils.reGetPdf(fileInfo.getFileId(), request, response, fileInfo,
                    fileStore, createPdfOpt, fileInfoManager, fileStoreInfoManager);
            }
            if (!canView) {
                FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
                UploadDownloadUtils.downFileRange(request, response,
                    FileIOUtils.getFileStream(fileStore, fileStoreInfo),
                    fileStoreInfo.getFileSize(),
                    fileInfo.getFileName(),
                    "inline", null);
            }
            fileInfoManager.writeDownloadFileLog(fileInfo, WebOptUtils.getCurrentUserCode(request));
        } catch (Exception e) {
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }

}
