package com.centit.fileserver.utils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRangeInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final long FILE_BLOCK_SIZE = 10*1024*1024L;


    private    long rangeStart;
    private long rangeEnd;
    private long fileSize;


    //static final Pattern RANGE_PATTERN = Pattern.compile("bytes \\d+-\\d+/\\d+");
    /**
     * 获取Range参数
     * @param req HttpServletRequest
     * @return FileRangeInfo
     */
    public static FileRangeInfo parseRange(HttpServletRequest req){
        return parseRange(req.getHeader("content-range"));
    }

    public static FileRangeInfo parseRange(String range){
        if(range==null)
            return null;

        Matcher m = Pattern.compile("\\d+").matcher(range);
        if (m.find()) {
            long from = Long.parseLong(m.group(0));
            if(m.find()){
                long to = Long.parseLong(m.group(0));
                if(m.find()){
                    long size = Long.parseLong(m.group(0));
                    if(size>0 && to>=size)
                        to=size-1;
                    return new FileRangeInfo(from, to, size);
                }
                return new FileRangeInfo(from, to, -1);
            }
            return new FileRangeInfo(from,-1, -1);
        }
        return null;//new FileRangeInfo(0, -1, -1);
    }

    public FileRangeInfo(){
        this(0,-1,-1);
    }

    public FileRangeInfo(long from, long to, long size){
        this.rangeStart = from;
        this.rangeEnd = to;
        this.fileSize = size;
    }

    @Override
    public String toString(){
        return  (rangeStart<0?"": String.valueOf(rangeStart))
                + "-" + (rangeEnd<=0?"": String.valueOf(rangeEnd))
                + "/" + (fileSize<=0?"": String.valueOf(fileSize));
    }

    public String getResponseRange(){
        return  "bytes " + (rangeStart<0?"": String.valueOf(rangeStart))
                + "-" + (rangeEnd<=0?"": String.valueOf(rangeEnd))
                + "/" + (fileSize<=0?"": String.valueOf(fileSize));
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public long getRangeEnd() {
        return rangeEnd;
        /*rangeStart+FILE_BLOCK_SIZE>fileSize?
                    fileSize:
                    rangeStart+FILE_BLOCK_SIZE;*/
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getPartSize() {
        return rangeEnd - rangeStart + 1;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setRangeEnd(long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
}
