package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileLibraryInfoDao;
import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.support.database.utils.PageDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
/**
 * FileLibraryInfo  Service.
 * create by scaffold 2020-08-18 13:38:13
 * @author codefan@sina.com
 * 文件库信息
*/
@Service
@Transactional(rollbackFor = Exception.class)
public class FileLibraryInfoManagerImpl
	{

	private static final Logger logger = LoggerFactory.getLogger(FileLibraryInfoManager.class);

    @Autowired
	private FileLibraryInfoDao fileLibraryInfoDao ;
    public void updateFileLibraryInfo(FileLibraryInfo fileLibraryInfo){
		fileLibraryInfoDao.updateObject(fileLibraryInfo);
		fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }
    public void createFileLibraryInfo(FileLibraryInfo fileLibraryInfo){
		fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
		fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }
    public List<FileLibraryInfo> listFileLibraryInfo(Map<String, Object> param, PageDesc pageDesc){
		return fileLibraryInfoDao.listObjectsByProperties(param, pageDesc);
    }


    public  FileLibraryInfo getFileLibraryInfo( String libraryId){
		return fileLibraryInfoDao.getObjectById(libraryId);
    }
    public void deleteFileLibraryInfo( String libraryId){
		fileLibraryInfoDao.deleteObjectById(libraryId);
    }

}

