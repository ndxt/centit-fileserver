package com.centit.fileserver.service.impl;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.common.OperateFileLibrary;
import com.centit.fileserver.dao.FileLibraryInfoDao;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.UnitInfo;
import com.centit.framework.model.basedata.UserUnit;
import com.centit.framework.model.basedata.WorkGroup;
import com.centit.support.algorithm.CollectionsOpt;
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
@Service("fileLibraryInfoManager")
@Transactional(rollbackFor = Exception.class)
public class FileLibraryInfoManagerImpl extends BaseEntityManagerImpl<FileLibraryInfo, String, FileLibraryInfoDao>
    implements FileLibraryInfoManager, OperateFileLibrary {

    private static final Logger logger = LoggerFactory.getLogger(FileLibraryInfoManager.class);
    private static final char SEPARATOR = '/';
    private static final String WORKGROUP_ROLECODE_MEMBER="组员";
    @Autowired
    private FileLibraryInfoDao fileLibraryInfoDao;
    @Value("${extend.library.enable:false}")
    protected boolean topEnable;
    @Value("${extend.library.unitcode:root}")
    protected String topUnit;

    @Override
    public void updateFileLibrary(FileLibraryInfo fileLibraryInfo) {
        fileLibraryInfoDao.updateObject(fileLibraryInfo);
        fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }

    @Override
    public void createFileLibrary(FileLibraryInfo fileLibraryInfo) {
        if (fileLibraryInfo.getWorkGroups() != null) {
            fileLibraryInfo.getWorkGroups().forEach(e -> {
                if (StringUtils.isBlank(e.getCreator())) {
                    e.setCreator(fileLibraryInfo.getCreateUser());
                }
                if (StringUtils.isBlank(e.getWorkGroupParameter().getRoleCode())) {
                    e.getWorkGroupParameter().setRoleCode(WORKGROUP_ROLECODE_MEMBER);
                }
            });
        }
        fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
        fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }

    @Override
    public List<FileLibraryInfo> listFileLibrary(String topUnit, String userCode) {
        if(StringUtils.isBlank(userCode)){
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("accessuser", userCode);
        StringBuilder sqlBuilder= new StringBuilder("where ( (library_type='P' and own_user=:accessuser) " +
                " or (library_type='T' and library_id in (select group_id from work_group where user_code=:accessuser) )");
        Set<String> units = getUnits(topUnit, userCode);
        if(units != null && units.size() > 0){
            map.put("ownunit", units);
            sqlBuilder.append( " or (library_type='O' and own_unit in (:ownunit) )");
        }
        sqlBuilder.append(")");
            List<FileLibraryInfo> libraryInfos = fileLibraryInfoDao.listObjectsByFilter(sqlBuilder.toString(), map);
        boolean hasPerson = libraryInfos.stream().anyMatch(fileLibraryInfo -> "P".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
            userCode.equals(fileLibraryInfo.getOwnUser()));
        if (!hasPerson) {
            libraryInfos.add(initPersonLibrary(topUnit, userCode));
        }
       /* for (String unitCode : getUnits(topUnit, userCode)) {
            boolean hasUnit = libraryInfos.stream().anyMatch(fileLibraryInfo -> "O".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
                unitCode.equals(fileLibraryInfo.getOwnUnit()));
            if (!hasUnit) {
                libraryInfos.add(getUnitLibraryInfo(topUnit, unitCode, userCode));
            }
        }*/
        return libraryInfos.stream().sorted(Comparator.comparing(FileLibraryInfo::getLibraryType, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public List<UnitInfo> listUnitPathsByUserCode(String topUnit, String userCode) {
        List<UnitInfo> result = new ArrayList<>(10);
        for (String unit : getUnits(topUnit, userCode)) {
            if (CodeRepositoryUtil.getUnitInfoByCode(topUnit, unit) != null) {
                result.add(CodeRepositoryUtil.getUnitInfoByCode(topUnit, unit));
            }
        }
        return result;
    }

    @Override
    public FileLibraryInfo initPersonLibrary(String topUnit, String userCode) {
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUser", userCode, "libraryType", "P"));
        if (null == fileLibraryInfos || fileLibraryInfos.size() == 0) {
            FileLibraryInfo fileLibraryInfo = new FileLibraryInfo();
            fileLibraryInfo.setCreateUser(userCode);
            fileLibraryInfo.setOwnUser(userCode);
            fileLibraryInfo.setLibraryName("我的文件");
            fileLibraryInfo.setLibraryType("P");
            fileLibraryInfo.setIsCreateFolder("T");
            fileLibraryInfo.setIsUpload("T");
            createFileLibrary(fileLibraryInfo);
            return fileLibraryInfo;
        }
        return fileLibraryInfos.get(0);
    }

    @Override
    public void initUnitLibrary(String topUnit, String unitCode, String userCode) {
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUnit", unitCode, "libraryType", "O"));
        if (null == fileLibraryInfos || fileLibraryInfos.size() == 0) {
            FileLibraryInfo fileLibraryInfo = getUnitLibraryInfo(topUnit, unitCode, userCode);
            createFileLibrary(fileLibraryInfo);
        }
    }

    private FileLibraryInfo getUnitLibraryInfo(String topUnit, String unitCode, String userCode) {
        FileLibraryInfo fileLibraryInfo = new FileLibraryInfo();
        fileLibraryInfo.setCreateUser(userCode);
        fileLibraryInfo.setOwnUser(userCode);
        fileLibraryInfo.setOwnUnit(unitCode);
        fileLibraryInfo.setLibraryName(CodeRepositoryUtil.getUnitName(topUnit, unitCode));
        fileLibraryInfo.setLibraryType("O");
        fileLibraryInfo.setIsCreateFolder("T");
        fileLibraryInfo.setIsUpload("T");
        fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
        return fileLibraryInfo;
    }

    @Override
    public Set<String> getUnits(String topUnit, String userCode) {
        if (userCode == null || topUnit == null) {
            return null;
        }
        Set<String> treeSet = new TreeSet<>();
        List<UserUnit> uulist = CodeRepositoryUtil.listUserUnits(topUnit, userCode);
        if (uulist != null && uulist.size() > 0) {
            Iterator<UserUnit> var6 = uulist.iterator();
            while (var6.hasNext()) {
                UserUnit uu = var6.next();
                UnitInfo unitInfo = CodeRepositoryUtil.getUnitInfoByCode(topUnit, uu.getUnitCode());
                if (unitInfo != null) {
                    String[] temp = StringUtils.split(
                        unitInfo.getUnitPath(), SEPARATOR);
                    treeSet.addAll(Arrays.asList(temp));
                }
            }
        }
        if (topEnable) {
            treeSet.add(topUnit);
        }
        return treeSet;
    }


    @Override
    public FileLibraryInfo getFileLibrary(String topUnit, String libraryId) {
        FileLibraryInfo fileLibraryInfo = fileLibraryInfoDao.getObjectWithReferences(libraryId);
        if (fileLibraryInfo == null) {
            return null;
        }
        if (!StringUtils.isBlank(fileLibraryInfo.getOwnUser())) {
            fileLibraryInfo.setOwnName(CodeRepositoryUtil.getUserName(topUnit, fileLibraryInfo.getOwnUser()));
        }
        return fileLibraryInfo;
    }

    @Override
    public void deleteFileLibrary(String libraryId) {
        fileLibraryInfoDao.deleteObjectById(libraryId);
    }

    @Override
    public boolean checkAuth(String topUnit, FileInfo fileInfo, String userCode, String authCode) {
        Set<String> unitPath = this.getUnits(topUnit, userCode);

        if (!"undefined".equals(userCode) && !StringUtils.isBlank(userCode) && !StringUtils.isBlank(fileInfo.getLibraryId())) {
            FileLibraryInfo fileLibraryInfo = this.getFileLibrary(topUnit, fileInfo.getLibraryId());
            switch (fileLibraryInfo.getLibraryType()) {
                //个人
                case "P":
                    if (userCode.equals(fileLibraryInfo.getOwnUser())) {
                        return true;
                    }
                    break;
                //机构
                case "O":
                    for (String s : unitPath) {
                        if (s.contains(fileLibraryInfo.getOwnUnit())) {
                            return true;
                        }
                    }
                    break;
                //项目
                case "I":
                    if (userCode.equals(fileLibraryInfo.getOwnUser())) {
                        return true;
                    }
                    if (fileLibraryInfo.getWorkGroups() != null) {
                        for (WorkGroup workGroup : fileLibraryInfo.getWorkGroups()) {
                            if (userCode.equals(workGroup.getWorkGroupParameter().getUserCode())) {
                                return true;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (!StringUtils.isBlank(authCode) && !"undefined".equals(authCode)) {
            return authCode.equals(fileInfo.getAuthCode());
        }
        return false;
    }

    @Override
    public FileLibraryInfo insertFileLibrary(FileLibraryInfo fileLibrary) {
        /*
        FileLibraryInfo fileLibraryInfo = new FileLibraryInfo();
        fileLibraryInfo.copyNotNull(fileLibrary);*/
        fileLibraryInfoDao.mergeObject(fileLibrary);
        return fileLibrary;
    }

}

