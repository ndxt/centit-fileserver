package com.centit.fileserver.utils;

import com.alibaba.fastjson.JSONObject;
import com.centit.framework.core.common.ObjectException;
import com.centit.framework.core.common.ResponseData;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;

/**
 * Created by codefan on 17-7-19.
 */
public class UploadDownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadDownloadUtils.class);
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
        response.setContentType(FileType.getFileMimeType(fileName));
        //"application/octet-stream"); //application/x-download "multipart/form-data"
        //String isoFileName = this.encodeFilename(proposeFile.getName(), request);
        response.setHeader("Accept-Ranges", "bytes");
        //这个需要设置成真正返回的长度
        //response.setHeader("Content-Length", String.valueOf(fSize));
        String s = request.getParameter("downloadType");
        response.setHeader("Content-Disposition",
                ("inline".equalsIgnoreCase(s)?"inline": "attachment")+"; filename="
                        + fileName);
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

            if(pos>0)
                inputStream.skip(pos);
            byte[] buffer = new byte[64 * 1024];
            int needSize = new Long(fr.getPartSize()).intValue(); //需要传输的字节
            int length = 0;
            while (needSize>0 && (length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                int writeLen =  needSize > length ? length: needSize;
                bufferOut.write(buffer, 0, writeLen);
                bufferOut.flush();
                needSize -= writeLen;
            }
            //bufferOut.flush();
            //bufferOut.close();
            //out.close();
        } catch (SocketException e){
            logger.info("客户端断开链接："+e.getMessage());
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
                                    InputStream fileInputStream,String token,
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
                int length = FileIOOpt.writeInputStreamToOutputStream(
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
                FileSystemOpt.deleteFile(tempFilePath);
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

    public static JSONObject makeRangeUploadJson(long rangeFileSize){
        JSONObject json = new JSONObject();
        json.put("start", rangeFileSize);
        json.put(ResponseData.RES_CODE_FILED, 0);
        json.put(ResponseData.RES_MSG_FILED, "上传文件片段成功");
        json.put(ResponseData.RES_DATA_FILED, rangeFileSize);
        return json;
    }
}
