package com.centit.fileserver.utils;

import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.common.FileStore;
import com.centit.framework.common.ResponseData;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-7-19.
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class UploadDownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadDownloadUtils.class);

    public static JSONObject checkFileRange(FileStore fileStore, String token, long size) {
        JSONObject jsonObject;
        // 如果文件已经存在则完成秒传，无需再传
        if (fileStore.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
            jsonObject = UploadDownloadUtils.
                makeRangeCheckJson(size, token, true);
        } else {
            long tempFileSize = SystemTempFileUtils.checkTempFileSize(
                SystemTempFileUtils.getTempFilePath(token, size));
            jsonObject = UploadDownloadUtils.
                makeRangeCheckJson(tempFileSize, token, false);
        }
        return jsonObject;
    }

    public static Pair<String, Long> fetchMd5andSize(String md5SizeExt) {
        String fileMd5 =  md5SizeExt.substring(0,32);
        int pos = md5SizeExt.indexOf('.');
        //String extName = md5SizeExt.substring(pos);
        long fileSize = pos<0? NumberBaseOpt.parseLong(md5SizeExt.substring(33),0l)
            : NumberBaseOpt.parseLong(md5SizeExt.substring(33,pos),0l);
        return Pair.of(fileMd5, fileSize);
    }

    public static Pair<String, InputStream> fetchInputStreamFromMultipartResolver(HttpServletRequest request) throws IOException {
        String fileName = request.getParameter("name");
        if(StringUtils.isBlank(fileName))
            fileName = request.getParameter("fileName");
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return new ImmutablePair<>(fileName, request.getInputStream());
        }

        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
//        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> map = multiRequest.getFileMap();
        InputStream fis = null;

        for (Map.Entry<String, MultipartFile> entry : map.entrySet()) {
            CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();
            FileItem fi = cMultipartFile.getFileItem();
            if (! fi.isFormField())  {
                fileName = fi.getName();
                fis = fi.getInputStream();
                if(fis!=null)
                    break;
            }
        }
        return  new ImmutablePair<>(fileName, fis);
    }

    @Deprecated
    public static Pair<String, InputStream> fetchInputStreamFromRequest(HttpServletRequest request)
            throws IOException, FileUploadException {

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            String fileName = request.getParameter("name");
            if(StringUtils.isBlank(fileName)) {
                fileName = request.getParameter("fileName");
            }
            return new ImmutablePair<>(fileName, request.getInputStream());
        }

        String fileName = null;
        ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> fileItems =  servletFileUpload.parseRequest(request);
        InputStream fis = null;
        for (FileItem fi : fileItems) {
            if (! fi.isFormField())  {
                fileName = fi.getName();
                fis = fi.getInputStream();
                if(fis!=null)
                    break;
            }
        }

        if(StringUtils.isBlank(fileName)) {
            fileName = request.getParameter("name");
        }
        if(StringUtils.isBlank(fileName)) {
            fileName = request.getParameter("fileName");
        }

        return  new ImmutablePair<>(fileName, fis);
    }

    public static String encodeDownloadFilename(String paramName) {
        try {
            return new String(
                    StringEscapeUtils.unescapeHtml4(paramName).getBytes("GBK"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            logger.error("转换文件名 " + paramName + " 报错："+e.getMessage(),e);
            return paramName;
        }
    }

    /**
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param inputStream InputStream
     * @param fSize long
     * @param fileName fileName
     * @throws IOException IOException
     */
    public static void downFileRange(HttpServletRequest request, HttpServletResponse response,
                                      InputStream inputStream, long fSize, String fileName)
            throws IOException {
        // 下载
        //String extName = FileType.getFileExtName(fileName);
        if(StringUtils.isBlank(fileName)){
            fileName = "attachment.dat";
        }
        response.setContentType(FileType.getFileMimeType(fileName));
        //"application/octet-stream"); //application/x-download "multipart/form-data"
        //String isoFileName = this.encodeFilename(proposeFile.getName(), request);
        response.setHeader("Accept-Ranges", "bytes");
        //这个需要设置成真正返回的长度
        //response.setHeader("Content-Length", String.valueOf(fSize));
        String s = request.getParameter("downloadType");
        response.setHeader("Content-Disposition",
                ("inline".equalsIgnoreCase(s)?"inline": "attachment")+"; filename="
                        + UploadDownloadUtils.encodeDownloadFilename(fileName));
        long pos = 0;

        FileRangeInfo fr = FileRangeInfo.parseRange(request.getHeader("Range"));

        if(fr == null){
            fr = new FileRangeInfo(0,fSize - 1,fSize);
        }else{
            if(fr.getRangeEnd()<=0)
                fr.setRangeEnd(fSize - 1);
            fr.setFileSize(fSize);
            pos = fr.getRangeStart();
            if(fr.getPartSize() < fr.getFileSize()) //206
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }

        response.setHeader("Content-Length", String.valueOf(fr.getPartSize()));
        // Content-Range: bytes 500-999/1234
        response.setHeader("Content-Range", fr.getResponseRange());
        //logger.debug("Content-Range :" + contentRange);
        try(ServletOutputStream out = response.getOutputStream();
            BufferedOutputStream bufferOut = new BufferedOutputStream(out)){

            if(pos>0) {
                inputStream.skip(pos);
            }
            byte[] buffer = new byte[64 * 1024];
            int needSize = new Long(fr.getPartSize()).intValue(); //需要传输的字节
            int length = 0;
            while ((needSize > 0) && ((length = inputStream.read(buffer, 0, buffer.length)) != -1)) {
                int writeLen =  needSize > length ? length: needSize;
                bufferOut.write(buffer, 0, writeLen);
                bufferOut.flush();
                needSize -= writeLen;
            }
            //bufferOut.flush();
            //bufferOut.close();
            //out.close();
        } catch (SocketException e){
            logger.info("客户端断开链接："+e.getMessage(), e);
        }
    }

    private static long checkTempFileSize(String filePath){
        File f = new File(filePath);
        if(!f.exists()) return 0;
        return f.length();
    }

    /**
     * @param tempFilePath 需要保存的临时文件路径
     * @param token String
     * @param size long
     * @param fileInputStream InputStream
     * @param request HttpServletRequest
     * @return long 0 complete &lt; 0 error &gt; 0 completeSize rangecomplete
     * @throws IOException IOException
     * @throws ObjectException ObjectException
     */
    public static long uploadRange(String tempFilePath,
                                    InputStream fileInputStream, String token,
                                    long size, HttpServletRequest request )
            throws IOException, ObjectException {

        long tempFileSize = checkTempFileSize(tempFilePath);
        FileRangeInfo range = FileRangeInfo.parseRange(request);
        if (tempFileSize < size) {//文件还没有传输完成
            // 必须要抛出异常或者返回非200响应前台才能捕捉
            if (tempFileSize != range.getRangeStart()) {
                throw new ObjectException(FileServerConstant.ERROR_FILE_RANGE_START,
                        "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。" );
                //return -1l;
            }

            // 必须要抛出异常或者返回非200响应前台才能捕捉
            try (FileOutputStream out = new FileOutputStream(
                    new File(tempFilePath), true)) {
                long length = FileIOOpt.writeInputStreamToOutputStream(
                        fileInputStream, out);
                if (length != range.getPartSize()) {
                    throw new ObjectException(FileServerConstant.ERROR_FILE_RANGE_START,
                            "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。");
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
                return 0;
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

    public static JSONObject makeRangeCheckJson(long rangeFileSize, String fileMd5, boolean hasStored){
        JSONObject json = new JSONObject();
        json.put("start", rangeFileSize);
        if(hasStored) {
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

    public static JSONObject makeRangeUploadJson(long rangeFileSize, String fileMd5, String fileName){
        JSONObject json = new JSONObject();
        json.put("start", rangeFileSize);
        json.put("signal", "continue");
        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, "上传文件片段成功!");
        json.put(ResponseData.RES_DATA_FILED, CollectionsOpt.createHashMap(
            "fileName",fileName ,
            "fileMd5", fileMd5,
            "fileSize", rangeFileSize));
        return json;
    }

    public static JSONObject makeRangeUploadCompleteJson(long fileSize,
                                                         Object fileInfo){
        JSONObject json = new JSONObject();
        json.put("start", fileSize);
        json.put("signal", "complete");
        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, "上传文件成功!");
        json.put(ResponseData.RES_DATA_FILED, fileInfo);
        return json;
    }

    public static JSONObject makeRangeUploadCompleteJson(String fileMd5, long fileSize, String fileName, String fileId){
        return makeRangeUploadCompleteJson(fileSize,
            CollectionsOpt.createHashMap("fileId", fileId,
                "fileMd5", fileMd5, "fileSize", fileSize,
                "fileName", fileName));
    }

    public static void downloadFile(InputStream downloadFile, String downloadName, HttpServletResponse response)
        throws IOException {
        downloadName = new String(downloadName.getBytes("GBK"), "ISO8859-1");
        response.setContentType("application/x-msdownload;");
        response.setHeader("Content-disposition", "attachment; filename=" + downloadName);
        response.setHeader("Content-Length", String.valueOf(downloadFile.available()));
        IOUtils.copy(downloadFile, response.getOutputStream());
    }

}
