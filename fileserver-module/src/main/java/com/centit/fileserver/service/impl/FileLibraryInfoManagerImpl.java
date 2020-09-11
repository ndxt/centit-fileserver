package com.centit.fileserver.service.impl;

import com.centit.fileserver.dao.FileLibraryInfoDao;
import com.centit.fileserver.po.FileLibraryInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.IUnitInfo;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    @Value("${top.enable:false}")
    protected boolean topEnable;
    @Value("${top.unit}")
    protected String topUnit;

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
        String where =" where 1=1 and ((own_user=:userCode) or "
            +"(library_type='O' and own_unit in (:ownunit)) "
            +"or (library_type='I' and library_id in (select library_id from file_library_access where access_usercode=:accessuser)))";
        Map<String,Object> map=new HashMap<>();
        map.put("userCode",userCode);
        map.put("ownunit",getUnits(userCode));
        map.put("accessuser",userCode);
        List<FileLibraryInfo> libraryInfos= fileLibraryInfoDao.listObjectsByFilter(where,map);
        boolean hasPerson= libraryInfos.stream().anyMatch(fileLibraryInfo -> "P".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
            userCode.equals(fileLibraryInfo.getOwnUser()));
        if(!hasPerson){
           libraryInfos.add(getPersonLibraryInfo(userCode));
        }
        for(String unitCode:getUnits(userCode)) {
            boolean hasUnit= libraryInfos.stream().anyMatch(fileLibraryInfo -> "O".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
                unitCode.equals(fileLibraryInfo.getOwnUnit()));
            if (!hasUnit) {
                libraryInfos.add(getUnitLibraryInfo(unitCode, userCode));
            }
        }
        return libraryInfos.stream().sorted(Comparator.comparing(FileLibraryInfo::getLibraryType,Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IUnitInfo> listUnitPathsByUserCode(String userCode) {
        List<IUnitInfo> result= new ArrayList<>(10);
        for(String unit: getUnits(userCode)){
           result.add(CodeRepositoryUtil.getUnitInfoByCode(unit));
        }
        return result;
    }

    @Override
    public void initPersonLibrary(String userCode) {
        List<FileLibraryInfo> fileLibraryInfos=fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUser",userCode,"libraryType","P"));
        if(null==fileLibraryInfos || fileLibraryInfos.size()==0){
            FileLibraryInfo fileLibraryInfo = getPersonLibraryInfo(userCode);
            createFileLibraryInfo(fileLibraryInfo);
        }
    }

    private FileLibraryInfo getPersonLibraryInfo(String userCode) {
        FileLibraryInfo fileLibraryInfo= new FileLibraryInfo();
        fileLibraryInfo.setCreateUser(userCode);
        fileLibraryInfo.setOwnUser(userCode);
        fileLibraryInfo.setLibraryName("我的文件");
        fileLibraryInfo.setLibraryType("P");
        fileLibraryInfo.setIsCreateFolder("T");
        fileLibraryInfo.setIsUpload("T");
        return fileLibraryInfo;
    }

    @Override
    public void initUnitLibrary(String unitCode,String userCode) {
        List<FileLibraryInfo> fileLibraryInfos=fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUnit",unitCode,"libraryType","O"));
        if(null==fileLibraryInfos || fileLibraryInfos.size()==0){
            FileLibraryInfo fileLibraryInfo = getUnitLibraryInfo(unitCode, userCode);
            createFileLibraryInfo(fileLibraryInfo);
        }
    }

    private FileLibraryInfo getUnitLibraryInfo(String unitCode, String userCode) {
        FileLibraryInfo fileLibraryInfo= new FileLibraryInfo();
        fileLibraryInfo.setCreateUser(userCode);
        fileLibraryInfo.setOwnUser(userCode);
        fileLibraryInfo.setOwnUnit(unitCode);
        fileLibraryInfo.setLibraryName(CodeRepositoryUtil.getUnitName(unitCode));
        fileLibraryInfo.setLibraryType("O");
        fileLibraryInfo.setIsCreateFolder("T");
        fileLibraryInfo.setIsUpload("T");
        return fileLibraryInfo;
    }

    @Override
    public String[] getUnits(String userCode) {
        String[] split= StringUtils.split(
            CodeRepositoryUtil.getUnitInfoByCode(CodeRepositoryUtil.getUserInfoByCode(userCode).getPrimaryUnit()).getUnitPath(),SEPARATOR);
        if(topEnable){
            if (!StringBaseOpt.isNvl(topUnit)) {
                boolean isFind=false;
                for (String s : split) {
                    if (s.contains(topUnit)) {
                        isFind = true;
                        break;
                    }
                }
                if(!isFind) {
                    List<String> result = new ArrayList<>(Arrays.asList(split));
                    result.add(topUnit);
                    return result.toArray(new String[0]);
                }
            }
        }
        return split;
    }


    @Override
    public FileLibraryInfo getFileLibraryInfo(String libraryId) {
        FileLibraryInfo fileLibraryInfo= fileLibraryInfoDao.getObjectWithReferences(libraryId);
        if(fileLibraryInfo==null){
            return null;
        }
        if(!StringBaseOpt.isNvl(fileLibraryInfo.getOwnUser())){
           fileLibraryInfo.setOwnName(CodeRepositoryUtil.getUserName(fileLibraryInfo.getOwnUser()));
        }
        return fileLibraryInfo;
    }

    @Override
    public void deleteFileLibraryInfo(String libraryId) {
        fileLibraryInfoDao.deleteObjectById(libraryId);
    }

}

