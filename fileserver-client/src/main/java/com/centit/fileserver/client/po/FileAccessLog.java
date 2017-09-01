package com.centit.fileserver.client.po;

import java.io.Serializable;
import java.util.Date;

import com.centit.support.algorithm.DatetimeOpt;

public class FileAccessLog implements Serializable {

    private static final long serialVersionUID = 1L;


    private String accessToken;

    private String fileId;

    private Date authTime;

    private String accessUsercode;

    private String accessUsename;
    /**
     * A： 所有权限  S: 下载源文件  T ：下载附属文件
     */
    private String accessRight;

    private Date tokenExpireTime;

    private Integer accessTimes;

    private Date lastAccessTime;

    private String lastAccessHost;

    public FileAccessLog() {

    }

    public FileAccessLog(String fileId) {
        this.fileId = fileId;
    }

    /**
     * 下载附属文件
     *
     * @param getAttach getAttach
     * @return 布尔值
     */
    public boolean checkValid(boolean getAttach) {
        //如果下载源文件，并且没有权限返回false
        if (!getAttach && "T".equals(accessRight)) {
            return false;
        }
        if (tokenExpireTime == null || accessTimes == null || accessTimes == 0)
            return true;

        return DatetimeOpt.currentUtilDate().before(tokenExpireTime);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public void setAuthTime(Date authTime) {
        this.authTime = authTime;
    }

    public String getAccessUsercode() {
        return accessUsercode;
    }

    public void setAccessUsercode(String accessUsercode) {
        this.accessUsercode = accessUsercode;
    }

    public String getAccessUsename() {
        return accessUsename;
    }

    public void setAccessUsename(String accessUsename) {
        this.accessUsename = accessUsename;
    }

    public String getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(String accessRight) {
        this.accessRight = accessRight;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Date tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public Integer getAccessTimes() {
        return accessTimes;
    }

    public Integer addAccessTimes() {
        if (accessTimes == null)
            accessTimes = 1;
        else
            accessTimes += 1;
        return accessTimes;
    }

    public void setAccessTimes(Integer accessTimes) {
        this.accessTimes = accessTimes;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getLastAccessHost() {
        return lastAccessHost;
    }

    public void setLastAccessHost(String lastAccessHost) {
        this.lastAccessHost = lastAccessHost;
    }
}