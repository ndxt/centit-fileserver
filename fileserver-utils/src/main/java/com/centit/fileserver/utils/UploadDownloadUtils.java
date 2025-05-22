package com.centit.fileserver.utils;

import com.alibaba.fastjson2.JSONObject;
import com.centit.fileserver.common.FileBaseInfo;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.po.FileInfo;
import com.centit.framework.common.ResponseData;
import com.centit.framework.common.WebOptUtils;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-7-19.
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class UploadDownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadDownloadUtils.class);

    //FIX： 这个地方逻辑好想不正确
    public static JSONObject checkFileRange(FileStore fileStore, FileBaseInfo fileInfo, long size) {
        JSONObject jsonObject;
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), size);
        if (fileStore.checkFile(fileStore.matchFileStoreUrl(fileInfo, size))) {//如果文件已经存在则完成秒传，无需再传
            jsonObject = UploadDownloadUtils.makeRangeCheckJson(size, fileInfo.getFileMd5(), true);
        } else {
            long tempFileSize = -1L;
            if (new File(tempFilePath).exists()) {//先查看临时目录是否存在文件
                tempFileSize = SystemTempFileUtils.checkTempFileSize(tempFilePath);
            }
            jsonObject = UploadDownloadUtils.makeRangeCheckJson(tempFileSize, fileInfo.getFileMd5(), tempFileSize == size);
        }
        return jsonObject;
    }

    public static Pair<String, InputStream> fetchInputStreamFromRequest(HttpServletRequest request, boolean useCommonsReolver) throws IOException {
        String fileName = WebOptUtils.getRequestFirstOneParameter(request, "name", "fileName");
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new ImmutablePair<>(fileName, request.getInputStream());
        }

        MultipartResolver resolver = useCommonsReolver ?
            new CommonsMultipartResolver(request.getSession().getServletContext()) :
            new StandardServletMultipartResolver();
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;
        if (useCommonsReolver) {
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();
                FileItem fi = cMultipartFile.getFileItem();
                if (!fi.isFormField()) {
                    fileName = fi.getName();
                    fis = fi.getInputStream();
                    if (fis != null) {
                        break;
                    }
                }
            }
        } else {
            for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
                MultipartFile cMultipartFile = entry.getValue();
                fileName = cMultipartFile.getResource().getFilename();
                fis = cMultipartFile.getInputStream();
            }
        }
        return new ImmutablePair<>(fileName, fis);
    }

    public static Pair<String, InputStream> fetchInputStreamFromMultipartResolver(HttpServletRequest request) throws IOException {
        return fetchInputStreamFromRequest(request, true);
    }

    //Springboot无法使用CommonsMultipartResolver，换成StandardServletMultipartResolver
    public static Pair<String, InputStream> fetchInputStreamFromStandardResolver(HttpServletRequest request) throws IOException {
        return fetchInputStreamFromRequest(request, false);
    }

    @Deprecated
    public static Pair<String, InputStream> fetchInputStreamFromRequest(HttpServletRequest request)
        throws IOException, FileUploadException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            String fileName = request.getParameter("name");
            if (StringUtils.isBlank(fileName)) {
                fileName = request.getParameter("fileName");
            }
            return new ImmutablePair<>(fileName, request.getInputStream());
        }

        String fileName = null;
        ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> fileItems = servletFileUpload.parseRequest(request);
        InputStream fis = null;
        for (FileItem fi : fileItems) {
            if (!fi.isFormField()) {
                fileName = fi.getName();
                fis = fi.getInputStream();
                if (fis != null) {
                    break;
                }
            }
        }

        if (StringUtils.isBlank(fileName)) {
            fileName = request.getParameter("name");
        }
        if (StringUtils.isBlank(fileName)) {
            fileName = request.getParameter("fileName");
        }

        return new ImmutablePair<>(fileName, fis);
    }

    public static String encodeDownloadFilename(String paramName) {
        try {
            String enCodeName= new String(
                StringEscapeUtils.unescapeHtml4(paramName).getBytes("GBK"), "ISO8859-1");
            if(!(java.nio.charset.Charset.forName("GBK").newEncoder().canEncode(enCodeName))) {
                return paramName;
            }
            return enCodeName;
        } catch (UnsupportedEncodingException e) {
            logger.error("转换文件名 " + paramName + " 报错：" + e.getMessage(), e);
            return paramName;
        }
    }

    public static String encodeDownloadFilename(String paramName, String downloadType) {
        try {
            if ("inline".equals(downloadType)) {
                return new String(
                    StringEscapeUtils.unescapeHtml4(paramName).getBytes("utf-8"), "utf-8");
            } else {
                return encodeDownloadFilename(paramName);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("转换文件名 " + paramName + " 报错：" + e.getMessage(), e);
            return paramName;
        }
    }

    private static void innerDownFileRange(HttpServletResponse response,
                                     InputStream inputStream, FileRangeInfo fr) throws IOException {
        long pos = fr.getRangeStart();
        BufferedInputStream bis = (inputStream instanceof BufferedInputStream)?
            (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, 64 * 1024);
        try (ServletOutputStream out = response.getOutputStream();
             BufferedOutputStream bufferOut = new BufferedOutputStream(out)) {
            if (pos > 0) {
                /*pos =*/ bis.skip(pos);
            }
            byte[] buffer = new byte[64 * 1024];
            long needSize = fr.getPartSize(); //需要传输的字节
            int length;
            while ((needSize > 0) && ((length = bis.read(buffer, 0, buffer.length)) != -1)) {
                long writeLen = Math.min(needSize, length);
                bufferOut.write(buffer, 0, (int) writeLen);
                bufferOut.flush();
                needSize -= writeLen;
            }
            //bufferOut.flush();
            //bufferOut.close();
            //out.close();
        } catch (SocketException e) {
            logger.error("客户端断开链接：" + ObjectException.extortExceptionMessage(e));
        }
    }

    private static void innerDownFileAll(HttpServletResponse response, InputStream inputStream) throws IOException {
        int fileSize = inputStream.available();
        if(fileSize>0) {
            response.setHeader("Content-Length", String.valueOf(fileSize));
        }
        BufferedInputStream bis = (inputStream instanceof BufferedInputStream)?
            (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, 64 * 1024);
        try (ServletOutputStream out = response.getOutputStream();
             BufferedOutputStream bufferOut = new BufferedOutputStream(out)) {
            byte[] buffer = new byte[64 * 1024];
            int length;
            while ( (length = bis.read(buffer, 0, buffer.length)) != -1) {
                bufferOut.write(buffer, 0, length);
                bufferOut.flush();
            }
        } catch (SocketException e) {
            logger.error("客户端断开链接：" + ObjectException.extortExceptionMessage(e));
        }
    }

    /**
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @param inputStream InputStream
     * @param fSize       long
     * @param fileName    fileName
     * @param downloadType  下载方式
     * @param charset    编码
     * @throws IOException IOException
     */
    public static void downFileRange(HttpServletRequest request, HttpServletResponse response,
                                     InputStream inputStream, long fSize, String fileName,
                                     String downloadType, String charset)
        throws IOException {
        // 下载
        //String extName = FileType.getFileExtName(fileName);
        if (StringUtils.isBlank(fileName)) {
            fileName = "attachment.dat";
        }
        //"application/octet-stream"); //application/x-download "multipart/form-data"
        //String isoFileName = this.encodeFilename(proposeFile.getName(), request);
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentType(FileType.getFileMimeType(fileName));
        if (!StringUtils.isBlank(charset)) {
            response.setCharacterEncoding(charset);
        }
        //这个需要设置成真正返回的长度
        //response.setHeader("Content-Length", String.valueOf(fSize));
        response.setHeader("Content-Disposition",
            ("inline".equalsIgnoreCase(downloadType) ? "inline" : "attachment") + "; filename="
                + URLEncoder.encode(fileName, "UTF-8"));
        FileRangeInfo fr = FileRangeInfo.parseRange(request);
        if (fr == null) {
            innerDownFileAll(response, inputStream);
            return;
        }
        int availableSize = inputStream.available();
        if (availableSize > 0) {
            fSize = availableSize;
        }
        if(fSize<0){
            innerDownFileAll(response, inputStream);
            return;
        }

        if (fr.getRangeEnd() <= 0) {
            fr.setRangeEnd(fSize - 1);
        }
        fr.setFileSize(fSize);
        if (fr.getPartSize() < fr.getFileSize()) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        response.setHeader("Content-Length", String.valueOf(fr.getPartSize()));
        // Content-Range: bytes 500-999/1234
        response.setHeader("Content-Range", fr.getResponseRange());
        //logger.debug("Content-Range :" + contentRange);
        innerDownFileRange(response, inputStream, fr);
    }

    private static long checkTempFileSize(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            return 0;
        }
        return f.length();
    }

    /**
     * @param tempFilePath    需要保存的临时文件路径
     * @param token           String
     * @param size            long
     * @param fileInputStream InputStream
     * @param request         HttpServletRequest
     * @return long 0 complete &lt; 0 error &gt; 0 completeSize rangecomplete
     * @throws IOException     IOException
     * @throws ObjectException ObjectException
     */
    public static long uploadRange(String tempFilePath,
                                   InputStream fileInputStream, String token,
                                   long size, HttpServletRequest request)
        throws IOException, ObjectException {

        long tempFileSize = checkTempFileSize(tempFilePath);
        FileRangeInfo range = FileRangeInfo.parseRange(request);
        if (tempFileSize < size || tempFileSize==0) {//文件还没有传输完成
            // 必须要抛出异常或者返回非200响应前台才能捕捉
            if (tempFileSize != range.getRangeStart()) {
                throw new ObjectException(CollectionsOpt.createHashMap(
                    "start", tempFileSize, "fileSize", tempFileSize, "fileMd5", token),
                    FileServerConstant.ERROR_FILE_RANGE_START,
                    "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。"+
                  "Range:"+range.getRangeStart() + " savedSize:"+ tempFileSize + " fileSize:" + size);
                //return -1l;
            }

            // 必须要抛出异常或者返回非200响应前台才能捕捉
            try (FileOutputStream out = new FileOutputStream(tempFilePath, true)) {
                long length = FileIOOpt.writeInputStreamToOutputStream(
                    fileInputStream, out);
                if (length != range.getPartSize()) {
                    throw new ObjectException(CollectionsOpt.createHashMap(
                        "start", tempFileSize, "fileSize", tempFileSize, "fileMd5", token),
                        FileServerConstant.ERROR_FILE_RANGE_START,
                        "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。"+
                            "Range:"+range.getRangeStart() + " uploadSize:"+ range.getPartSize()
                             + "receivedSize" + length + " fileSize:" + size);
                    //return -1l;
                }
            }
            tempFileSize = range.getRangeStart() + range.getPartSize();
        }
        //range.setRangeStart(rangeStart);
        if (tempFileSize == size) {
            //判断是否传输完成
            //fs.saveFile(tempFilePath, token, size);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
            if (StringUtils.equals(fileMd5, token)) {
                //FileSystemOpt.deleteFile(tempFilePath);
                //成功上传到临时路径
                return size;
                //completedStoreFile(fs, token, size, fileInfo.getLeft(), response);
            } else {
                FileSystemOpt.deleteFile(tempFilePath);
                throw new ObjectException(FileServerConstant.ERROR_FILE_MD5_ERROR,
                    "Code: " + FileServerConstant.ERROR_FILE_MD5_ERROR + " 文件MD5计算错误。");
                //return -1;
            }
        }
        return tempFileSize;
    }

    public static JSONObject makeRangeCheckJson(long rangeFileSize, String fileMd5, boolean hasStored) {
        JSONObject json = new JSONObject();
        json.put("start", rangeFileSize);
        if (hasStored) {
            json.put("signal", "secondpass");
            json.put(ResponseData.RES_MSG_FILED, "检查文件上传点, 请调用秒传(secondpass)接口!");
        } else { // 需要调用秒传接口
            json.put("signal", "continue");
            json.put(ResponseData.RES_MSG_FILED, "检查文件上传点, 请调用续传(range)接口!");
        }
        json.put(ResponseData.RES_CODE_FILED, 0);

        json.put(ResponseData.RES_DATA_FILED, CollectionsOpt.createHashMap(
            "fileMd5", fileMd5,
            "fileSize", rangeFileSize));
        return json;
    }

    public static JSONObject makeRangeUploadJson(long rangeFileSize, String fileMd5, String fileName) {
        JSONObject json = new JSONObject();
        json.put("start", rangeFileSize);
        json.put("signal", "continue");
        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, "上传文件片段成功!");
        json.put(ResponseData.RES_DATA_FILED, CollectionsOpt.createHashMap(
            "fileName", fileName,
            "fileMd5", fileMd5,
            "fileSize", rangeFileSize));
        return json;
    }

    public static JSONObject makeRangeUploadCompleteJson(long fileSize,
                                                         Object fileInfo, String message) {
        JSONObject json = new JSONObject();
        json.put("start", fileSize);
        json.put("signal", "complete");
        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, message);//"上传文件成功!"
        json.put(ResponseData.RES_DATA_FILED, fileInfo);
        return json;
    }

    public static JSONObject makeRangeUploadCompleteJson(String fileMd5, long fileSize, String fileName, String fileId, String message) {
        return makeRangeUploadCompleteJson(fileSize,
            CollectionsOpt.createHashMap("fileId", fileId,
                "fileMd5", fileMd5, "fileSize", fileSize,
                "fileName", fileName),
            message);
    }

    public static String encodeFileName(String fileName, String characterEncoding){
        try {
            if (fileName.length() > 150) {
                return new String(fileName.getBytes(characterEncoding), "ISO8859-1");
            }
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            return fileName;
        }
    }

    private static void innerDownloadFile(InputStream downloadFile, String fileName,
                                    HttpServletResponse response, String characterEncoding)
        throws IOException {
        response.setContentType("application/x-msdownload;");
        response.setHeader("Content-disposition", "attachment; filename=" + encodeFileName(fileName, characterEncoding));
        response.setHeader("Content-Length", String.valueOf(downloadFile.available()));
        IOUtils.copy(downloadFile, response.getOutputStream());
    }

    public static void downloadFile(InputStream downloadFile, String fileName,
                                    HttpServletResponse response)
        throws IOException {
        innerDownloadFile(downloadFile, fileName, response, "GBK");
    }

    public static void downloadFile(InputStream downloadFile, String fileName,
                                    HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        innerDownloadFile(downloadFile, fileName, response, request.getCharacterEncoding());
    }

    public static FileInfo createFileBaseInfo(HttpServletRequest request){
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileMd5(WebOptUtils
            .getRequestFirstOneParameter(request, "token", "fileMd5"));
        fileInfo.setFileName(WebOptUtils
            .getRequestFirstOneParameter(request,"name", "fileName"));
        fileInfo.setOptId(request.getParameter("optId"));
        fileInfo.setFileOwner(WebOptUtils.getCurrentUserCode(request));
        String fileUnit = request.getParameter("fileUnit");
        if (StringUtils.isBlank(fileUnit)) {
            fileUnit = WebOptUtils.getCurrentTopUnit(request);
        }
        fileInfo.setFileUnit(fileUnit);
        Long fileSize = NumberBaseOpt.parseLong(
            WebOptUtils.getRequestFirstOneParameter(request, "size", "fileSize"), -1l);
        fileInfo.setFileSize(fileSize);
        return fileInfo;
    }

    public static FileInfo createFileBaseInfo(HttpServletRequest request, String fileMd5, long fileSize){
        FileInfo fileInfo = createFileBaseInfo(request);
        if(StringUtils.isNotBlank(fileMd5)) {
            fileInfo.setFileMd5(fileMd5);
        }
        if(fileSize>0) {
            fileInfo.setFileSize(fileSize);
        }
        return fileInfo;
    }

    public static FileInfo createFileBaseInfo(String fileIdIncludeMd5AndSize){
        Pair<String, Long> md5Size = SystemTempFileUtils.fetchMd5AndSize(fileIdIncludeMd5AndSize);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId (fileIdIncludeMd5AndSize);
        fileInfo.setFileMd5(md5Size.getLeft());
        fileInfo.setFileSize(md5Size.getRight());
        return fileInfo;
    }

}
