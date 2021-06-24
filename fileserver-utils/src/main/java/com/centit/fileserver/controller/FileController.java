package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.utils.FileRangeInfo;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public abstract class FileController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    protected FileStore fileStore;

    /**
     * 文件上传后的处理工作，如果需要对文件处理或者返回特定的数据给前段可以在这个方法中做
     *
     * @param fileMd5 文件的md5 和 size可以确定文件的位置
     * @param size    文件大小
     * @param retJson 返回前段的json对象，可以在这个方法中修改
     */
    protected abstract void fileUploadCompleteOpt(String fileMd5, long size,
                                                  JSONObject retJson);

    /**
     * 这个方法可能需要根据环境重载;
     *
     * @param request 客户端请求
     * @return 文件名和 文件流
     * @throws IOException io 异常
     */
    protected Pair<String, InputStream> fetchInputStreamFromRequest(HttpServletRequest request) throws IOException {
        return UploadDownloadUtils.fetchInputStreamFromMultipartResolver(request);
    }

    /**
     * 判断文件是否存在，如果文件已经存在可以实现秒传
     *
     * @param request  token, size     大小
     * @param response HttpServletResponse
     */
    @ApiOperation(value = "检查文件是否存在")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400,
        allowedHeaders = "*", methods = RequestMethod.GET)
    @RequestMapping(value = "/exists", method = RequestMethod.GET)
    public void checkFileExists(HttpServletRequest request, HttpServletResponse response) {
        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(request);
        JsonResultUtils.writeOriginalObject(
            fileStore.checkFile(fileStore.matchFileStoreUrl(fileInfo, fileInfo.getFileSize())), response);
    }


    /**
     * 获取文件 断点位置，前端根据断点位置续传
     *
     * @param request HttpServletRequest : token, size  大小
     * @return 文件信息
     */
    @ApiOperation(value = "检查续传点，如果signal为continue请续传，如果为secondpass表示文件已存在需要调用秒传接口")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.GET)
    @RequestMapping(value = "/range", method = {RequestMethod.GET})
    @WrapUpResponseBody
    public JSONObject checkFileRange(HttpServletRequest request) {
        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(request);
        return UploadDownloadUtils.checkFileRange(fileStore, fileInfo, fileInfo.getFileSize());
    }

    private void completedFileStore(String fileMd5, long size,
                                    String fileName, HttpServletResponse response) {
        String fileId = fileMd5 + "_" + String.valueOf(size) + "." +
            FileType.getFileExtName(fileName);
        // 返回响应
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("src", "file/download/" + fileId + "?fileName=" + fileName);
        fileInfo.put("fileId", fileId);
        fileInfo.put("fileMd5", fileMd5);
        fileInfo.put("fileName", fileName);
        fileInfo.put("fileSize", size);
        JSONObject retJson = UploadDownloadUtils.makeRangeUploadCompleteJson(
            size, fileInfo);
        fileUploadCompleteOpt(fileMd5, size, retJson);
        JsonResultUtils.writeOriginalJson(retJson.toString(), response);
    }

    /**
     * 完成秒传，如果文件不存在会返回失败
     * @param request  HttpServletRequest token, size
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件秒传接口，在作为仅仅存储文件使用时，这个其实是没有必要的，可以不用调用")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/secondpass", method = RequestMethod.POST)
    public void secondPass(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        request.setCharacterEncoding("utf8");

        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(request);
        if (fileStore.checkFile(fileStore.matchFileStoreUrl(fileInfo, fileInfo.getFileSize()))) {// 如果文件已经存在则完成秒传，无需再传。
            completedFileStore(fileInfo.getFileMd5(), fileInfo.getFileSize(), fileInfo.getFileName(), response);
            return;
        } else {
            String tempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileInfo.getFileSize());
            long tempFileSize = SystemTempFileUtils.checkTempFileSize(tempFilePath);
            if (tempFileSize == fileInfo.getFileSize()) {
                fileStore.saveFile(tempFilePath, fileInfo, fileInfo.getFileSize());
                completedFileStore(fileInfo.getFileMd5(), fileInfo.getFileSize(), fileInfo.getFileName(), response);
                return;
            }
        }
        JsonResultUtils.writeHttpErrorMessage(
            FileServerConstant.ERROR_FILE_NOT_EXIST,
            "文件不存在无法实现秒传，MD5：" + fileInfo.getFileMd5(), response);
    }

    /**
     * 续传文件（range） 如果文件已经传输完成 对文件进行保存
     * @param request  HttpServletRequest: token, size
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "断点续传接口")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/range", method = {RequestMethod.POST})
    public void uploadRange(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        request.setCharacterEncoding("utf8");
        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(request);
        Pair<String, InputStream> fileStreamInfo = fetchInputStreamFromRequest(request);
        String fileName = StringUtils.isBlank(fileInfo.getFileName())? fileStreamInfo.getLeft()
            :fileInfo.getFileName();
        if(!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(fileName))) {
            fileName = new String(fileName.getBytes("iso-8859-1"), "utf-8");
        }
//        fileName= URLDecoder.decode(fileName,"utf-8");
        if (fileStore.checkFile(fileStore.matchFileStoreUrl(fileInfo, fileInfo.getFileSize()))) {// 如果文件已经存在则完成秒传，无需再传。
            completedFileStore(fileInfo.getFileMd5(), fileInfo.getFileSize(),
                fileName, response);
            return;
        }

        FileRangeInfo range = FileRangeInfo.parseRange(request);
        FileSystemOpt.createDirect(SystemTempFileUtils.getTempDirectory());
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileInfo.getFileSize());
        long tempFileSize = SystemTempFileUtils.checkTempFileSize(tempFilePath);
        if (tempFileSize < fileInfo.getFileSize()) {//文件还没有传输完成
            // 必须要抛出异常或者返回非200响应前台才能捕捉
            if (tempFileSize != range.getRangeStart()) {
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_RANGE_START,
                    "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。", response);
                return;
            }

            // 必须要抛出异常或者返回非200响应前台才能捕捉
            try (FileOutputStream out = new FileOutputStream(
                new File(tempFilePath), true)) {
                long length = FileIOOpt.writeInputStreamToOutputStream(fileStreamInfo.getRight(), out);
                if (length != range.getPartSize()) {
                    JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_RANGE_START,
                        "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。", response);
                    return;
                }
            }
            tempFileSize = range.getRangeStart() + range.getPartSize();
        }
        //range.setRangeStart(rangeStart);
        if (tempFileSize == fileInfo.getFileSize()) {
            //判断是否传输完成
            fileStore.saveFile(tempFilePath, fileInfo, fileInfo.getFileSize());
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
            if (StringUtils.equals(fileMd5, fileInfo.getFileMd5())) {
                completedFileStore(fileInfo.getFileMd5(), fileInfo.getFileSize(),
                    fileName, response);
            } else {
                JsonResultUtils.writeHttpErrorMessage(FileServerConstant.ERROR_FILE_MD5_ERROR,
                    "Code: " + FileServerConstant.ERROR_FILE_MD5_ERROR + " 文件MD5计算错误。", response);
            }
            FileSystemOpt.deleteFile(tempFilePath);
            return;
        }
        JSONObject json = UploadDownloadUtils.makeRangeUploadJson(tempFileSize, fileInfo.getFileMd5(),
            fileInfo.getFileMd5() + "_" + fileInfo.getFileSize());
        JsonResultUtils.writeOriginalJson(json.toString(), response);
    }

    /**
     * 上传整个文件适用于IE8
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件整体上传结构，适用于IE8")
    @CrossOrigin(origins = "*", allowCredentials = "true", maxAge = 86400, methods = RequestMethod.POST)
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadFile(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        request.setCharacterEncoding("utf8");
        Pair<String, InputStream> fileStreamInfo = fetchInputStreamFromRequest(request);

        String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();

        try {
            int fileSize = FileIOOpt.writeInputStreamToFile(fileStreamInfo.getRight(), tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
            FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(request, fileMd5, fileSize);

            fileStore.saveFile(tempFilePath, fileInfo, fileSize);
            completedFileStore(fileMd5, fileSize, fileStreamInfo.getLeft(), response);
            FileSystemOpt.deleteFile(tempFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JsonResultUtils.writeErrorMessageJson(
                ObjectException.extortExceptionMessage(e), response);
        }
    }

    /**
     * 根据文件的 MD5码 下载不受保护的文件，不需要访问文件记录
     * 如果是通过 store 上传的需要指定 extName 扩展名
     *
     * @param md5SizeExt 文件的Md5码和文件的大小 格式为 MD5_SIZE.EXT
     * @param fileName   文件的名称包括扩展名，如果这个不为空， 上面的 md5SizeExt 可以没有 .Ext 扩展名
     * @param request    HttpServletRequest
     * @param response   HttpServletResponse
     * @throws IOException IOException
     */
    @ApiOperation(value = "文件下载，此接口支持文件断点续传")
    @ApiImplicitParam(
        name = "md5SizeExt", value = "文件的Md5码_文件的大小.文件格式 MD5_SIZE.EXT，",
        required = true, paramType = "path", dataType = "String"
    )
    @RequestMapping(value = "/download/{md5SizeExt}", method = RequestMethod.GET)
    public void downloadUnprotectedFile(@PathVariable("md5SizeExt") String md5SizeExt,
                                        String fileName,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        String[] urips = uri.split("/");
        int n = urips.length;
        if (StringUtils.isBlank(fileName)) {
            fileName = urips[n - 1];
        }

        Pair<String, Long> md5Size = SystemTempFileUtils.fetchMd5AndSize(md5SizeExt);
        FileBaseInfo fileInfo = UploadDownloadUtils.createFileBaseInfo(
            request, md5Size.getLeft(), md5Size.getRight());
        InputStream inputStream = fileStore.loadFileStream(
            fileStore.matchFileStoreUrl(fileInfo, md5Size.getRight()));
        UploadDownloadUtils.downFileRange(request, response,
            inputStream, md5Size.getRight(), UploadDownloadUtils.encodeDownloadFilename(fileName), request.getParameter("downloadType"), null);
    }


}
