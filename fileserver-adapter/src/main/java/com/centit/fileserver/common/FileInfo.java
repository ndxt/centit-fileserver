package com.centit.fileserver.common;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

public class FileInfo implements FileBaseInfo, Serializable {

    private static final long serialVersionUID = 1L;

    private String fileId;

    private String fileMd5;

    private String fileName;

//    private String fileStorePath;

    private String fileType;

    /**
     * C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除
     */
    private String fileState;

    private String fileDesc;

    private String indexState;

    private Long downloadTimes;

    private String osId;

    private String optId;

    private String optMethod;

    private String optTag;

    private String created;

    private Date createTime;

    private Long fileSize;

    //加密算法
    private String encryptType;

    private String fileOwner;

    private String fileUnit;

//    private String attachedStorePath;
    private String attachedFileMd5;

    /**
     * 附属文件类别： T：缩略图  P： pdf只读文件
     */
    private String attachedType;

    private String libraryId;



    public FileInfo() {
        indexState = "N";
        encryptType = "N";
        fileState = "N";
        createTime = DatetimeOpt.currentUtilDate();
        downloadTimes = 0l;
    }

    @Override
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    @Override
    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (StringUtils.isNoneBlank(fileName) && StringUtils.isBlank(fileType))
            fileType = FileType.getFileExtName(fileName);
    }

//    public String getFileStorePath() {
//        return fileStorePath;
//    }
//
//    public void setFileStorePath(String fileStorePath) {
//        this.fileStorePath = fileStorePath;
//    }
    @Override
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除
     *
     * @return String
     */
    public String getFileState() {
        return fileState;
    }

    /**
     * C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除
     * @param fileState 文件状态
     */
    public void setFileState(String fileState) {
        this.fileState = fileState;
    }

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    /**
     * N ：不需要索引 S：等待索引 I：已索引 F:索引失败
     *
     * @return String
     */
    public String getIndexState() {
        return indexState;
    }

    /**
     * N ：不需要索引 S：等待索引 I：已索引 F:索引失败
     *
     * @param indexState indexState
     */
    public void setIndexState(String indexState) {
        this.indexState = indexState;
    }

    public Long getDownloadTimes() {
        return downloadTimes;
    }

    public void setDownloadTimes(Long downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public Long addDownloadTimes() {
        if (this.downloadTimes == null)
            downloadTimes = 1l;
        else
            downloadTimes += 1;
        return downloadTimes;
    }
    @Override
    public String getOptId() {
        return optId;
    }

    public void setOptId(String optId) {
        this.optId = optId;
    }

    public String getOptMethod() {
        return optMethod;
    }

    public void setOptMethod(String optMethod) {
        this.optMethod = optMethod;
    }

    public String getOptTag() {
        return optTag;
    }

    public void setOptTag(String optTag) {
        this.optTag = optTag;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * N : 没有加密   Z：zipFile    D:DES加密
     *
     * @return String
     */
    public String getEncryptType() {
        return encryptType;
    }

    /**
     * N : 没有加密   Z：zipFile    D:DES加密
     *
     * @param String String
     */
    public void setEncryptType(String String) {
        this.encryptType = String;
    }

    @Override
    public String getFileOwner() {
        return fileOwner;
    }

    public void setFileOwner(String fileOwner) {
        this.fileOwner = fileOwner;
    }

    @Override
    public String getFileUnit() {
        return fileUnit;
    }

    @Override
    public String getLibraryId() {
        return this.libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public void setFileUnit(String fileUnit) {
        this.fileUnit = fileUnit;
    }

    @Override
    public long getFileSize() {
        return fileSize == null? -1 : fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

//    public String getAttachedStorePath() {
//        return attachedStorePath;
//    }
//
//    public void setAttachedStorePath(String attachedStorePath) {
//        this.attachedStorePath = attachedStorePath;
//    }


    public String getAttachedFileMd5() {
        return attachedFileMd5;
    }

    public void setAttachedFileMd5(String attachedFileMd5) {
        this.attachedFileMd5 = attachedFileMd5;
    }

    /**
     * 附属文件类别：N :   没有  T：缩略图  P： pdf只读文件
     *
     * @return String
     */
    public String getAttachedType() {
        return attachedType;
    }

    /**
     * 附属文件类别：N :   没有  T：缩略图  P： pdf只读文件
     *
     * @param attachedType 附属文件类别
     */
    public void setAttachedType(String attachedType) {
        this.attachedType = attachedType;
    }


    public static FileInfo fromFileBaseInfo(FileBaseInfo otherFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.fileId = otherFile.getFileId();
        fileInfo.fileMd5 = otherFile.getFileMd5();
        fileInfo.fileType = otherFile.getFileType();
        fileInfo.fileName = otherFile.getFileName();
        fileInfo.osId = otherFile.getOsId();
        fileInfo.optId = otherFile.getOptId();
        fileInfo.fileOwner = otherFile.getFileOwner();
        fileInfo.fileUnit = otherFile.getFileUnit();
        fileInfo.libraryId = otherFile.getLibraryId();
        return fileInfo;
    }
}
