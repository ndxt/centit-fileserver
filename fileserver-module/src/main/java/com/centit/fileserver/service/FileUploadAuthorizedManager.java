package com.centit.fileserver.service;


import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.framework.jdbc.service.BaseEntityManager;

public interface FileUploadAuthorizedManager extends BaseEntityManager<FileUploadAuthorized, String> {

    int checkAuthorization(String accessToken);
    FileUploadAuthorized createNewAuthorization(int maxUploadFiles);
    int consumeAuthorization(String accessToken);
}
