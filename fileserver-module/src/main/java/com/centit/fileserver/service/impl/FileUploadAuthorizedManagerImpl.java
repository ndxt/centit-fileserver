package com.centit.fileserver.service.impl;


import com.centit.fileserver.dao.FileUploadAuthorizedDao;
import com.centit.fileserver.po.FileUploadAuthorized;
import com.centit.fileserver.service.FileUploadAuthorizedManager;
import com.centit.framework.hibernate.service.BaseEntityManagerImpl;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.UuidOpt;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Service
@Transactional
public class FileUploadAuthorizedManagerImpl extends BaseEntityManagerImpl<FileUploadAuthorized, String, FileUploadAuthorizedDao>
 implements FileUploadAuthorizedManager {

	@Resource(name ="fileUploadAuthorizedDao")
	@NotNull
	@Override
	protected void setBaseDao(FileUploadAuthorizedDao baseDao) {
		super.baseDao = baseDao;
	}



	/**
	 *
	 * @param accessToken token
	 * @return >0 可以上传文件否则不可以
	 */
	@Override
	public int checkAuthorization(String accessToken) {
		if(StringUtils.isBlank(accessToken)){
			return -1;
		}

		FileUploadAuthorized authorized = baseDao.getObjectById(accessToken);
		if(authorized==null){
			return -1;
		}
		return authorized.getRestUploadFiles();
	}

	@Override
	@Transactional
	public FileUploadAuthorized createNewAuthorization(int maxUploadFiles) {
		FileUploadAuthorized authorized = new FileUploadAuthorized();
		authorized.setAccessToken(UuidOpt.getUuidAsString32());
		authorized.setMaxUploadFiles(maxUploadFiles);
		authorized.setRestUploadFiles(maxUploadFiles);
		authorized.setCraeteTime(DatetimeOpt.currentUtilDate());
		baseDao.saveNewObject(authorized);
		return authorized;
	}

	@Override
	@Transactional
	public int consumeAuthorization(String accessToken) {
		if(StringUtils.isBlank(accessToken)){
			return -1;
		}

		FileUploadAuthorized authorized = baseDao.getObjectById(accessToken);
		if(authorized==null){
			return -1;
		}

		authorized.setRestUploadFiles( authorized.getRestUploadFiles() - 1);
		authorized.setLastUploadTime(DatetimeOpt.currentUtilDate());
		baseDao.updateObject(authorized);

		return authorized.getRestUploadFiles();
	}
}
