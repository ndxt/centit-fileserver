package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileLibraryInfoDao;
import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.IUnitInfo;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileLibraryInfo  Service.
 * create by scaffold 2020-08-18 13:38:13
 *
 * @author codefan@sina.com
 * 文件库信息
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FileLibraryInfoManagerImpl extends BaseEntityManagerImpl<FileLibraryInfo, String, FileLibraryInfoDao>
    implements FileLibraryInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(FileLibraryInfoManager.class);
    private static final char SEPARATOR = '/';
    @Autowired
    private FileLibraryInfoDao fileLibraryInfoDao;

    @Override
    public void updateFileLibraryInfo(FileLibraryInfo fileLibraryInfo) {
        fileLibraryInfoDao.updateObject(fileLibraryInfo);
        fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }

    @Override
    public void createFileLibraryInfo(FileLibraryInfo fileLibraryInfo) {
        fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
        fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }

    @Override
    public List<FileLibraryInfo> listFileLibraryInfo(String userCode) {
        String where =" where 1=1 and ((library_type='P' and own_user=:userCode) or "
            +"(library_type='O' and own_unit in (:ownunit)) "
            +"or (library_type='I' and library_id in (select library_id from file_library_access where access_usercode=:accessuser)))";
        Map<String,Object> map=new HashMap<>();
        map.put("userCode",userCode);
        map.put("ownunit",getUnits(userCode));
        map.put("accessuser",userCode);
        return fileLibraryInfoDao.listObjectsByFilter(where,map);
    }

    @Override
    public List<IUnitInfo> listUnitPathsByUserCode(String userCode) {
        List<IUnitInfo> result= new ArrayList<>(10);
        for(String unit: getUnits(userCode)){
           result.add(CodeRepositoryUtil.getUnitInfoByCode(unit));
        }
        return result;
    }

    private String[] getUnits(String userCode) {
        return StringUtils.split(
            CodeRepositoryUtil.getUnitInfoByCode(CodeRepositoryUtil.getUserInfoByCode(userCode).getPrimaryUnit()).getUnitPath(),SEPARATOR);
    }


    @Override
    public FileLibraryInfo getFileLibraryInfo(String libraryId) {
        return fileLibraryInfoDao.getObjectById(libraryId);
    }

    @Override
    public void deleteFileLibraryInfo(String libraryId) {
        fileLibraryInfoDao.deleteObjectById(libraryId);
    }

}

