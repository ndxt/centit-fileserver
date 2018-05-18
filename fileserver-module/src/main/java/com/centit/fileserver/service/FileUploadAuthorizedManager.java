package com.centit.fileserver.service;


import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.framework.hibernate.service.BaseEntityManager;

public interface FileUploadAuthorizedManager extends BaseEntityManager<FileUploadAuthorized, String> {

	int checkAuthorization(String accessToken);
	FileUploadAuthorized createNewAuthorization(int maxUploadFiles);
	int consumeAuthorization(String accessToken);
}
