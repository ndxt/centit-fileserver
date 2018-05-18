package com.centit.fileserver.po;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name= "FILE_UPLOAD_AUTHORIZED")
public class FileUploadAuthorized implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name ="ACCESS_TOKEN")
    @GenericGenerator(name = "fileUuid", strategy = "uuid")
    @GeneratedValue(generator = "fileUuid")
    private String accessToken;

    @Column(name="MAX_UPLOAD_FILES")
    private Integer maxUploadFiles;

    @Column(name="REST_UPLOAD_FILES")
    private Integer restUploadFiles;

    @Column(name="CREATE_TIME")
    private Date craeteTime;

    @Column(name="LAST_UPLOAD_TIME")
    private Date lastUploadTime;

    public FileUploadAuthorized(){
        maxUploadFiles = 1;
        restUploadFiles = 1;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getMaxUploadFiles() {
        return maxUploadFiles;
    }

    public void setMaxUploadFiles(Integer maxUploadFiles) {
        this.maxUploadFiles = maxUploadFiles;
    }

    public Integer getRestUploadFiles() {
        return restUploadFiles;
    }

    public void setRestUploadFiles(Integer restUploadFiles) {
        this.restUploadFiles = restUploadFiles;
    }

    public Date getCraeteTime() {
        return craeteTime;
    }

    public void setCraeteTime(Date craeteTime) {
        this.craeteTime = craeteTime;
    }

    public Date getLastUploadTime() {
        return lastUploadTime;
    }

    public void setLastUploadTime(Date lastUploadTime) {
        this.lastUploadTime = lastUploadTime;
    }
}
