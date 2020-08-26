package com.centit.fileserver.po;

import java.util.Date;

/**
 * Created by codefan on 17-1-17.
 */
public class FileShowInfo {
    /**
     * 文件版本数量
     */
    private int versions;
    /**
     * p:个人文件  d:机构文件
     */
    private String catalogType;
    private String fileShowPath;
    /**
     * 文件类别，f：文件，d:目录
     */
    private String fileType;
    private String fileName;
    /**
     * 如果文件最新版本的访问入口，可以通过这个直接访问
     */
    private String accessToken;

    private boolean fileEncrypt;

    private long fileSize;

    private Date createTime;//, CREATE_TIME

    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }

    private String favoriteId;
    public int getVersions() {
        return versions;
    }

    public void setVersions(int versions) {
        this.versions = versions;
    }

    public String getCatalogType() {
        return catalogType;
    }

    public void setCatalogType(String catalogType) {
        this.catalogType = catalogType;
    }

    public String getFileShowPath() {
        return fileShowPath;
    }

    public void setFileShowPath(String fileShowPath) {
        this.fileShowPath = fileShowPath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isEncrypt() {
        return fileEncrypt;
    }

    public void setEncrypt(boolean encrypt) {
        fileEncrypt = encrypt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
