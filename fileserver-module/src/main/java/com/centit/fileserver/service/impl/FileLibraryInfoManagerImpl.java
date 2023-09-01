package com.centit.fileserver.service.impl;

import com.centit.fileserver.common.FileLibraryInfo;
import com.centit.fileserver.common.OperateFileLibrary;
import com.centit.fileserver.dao.FileLibraryInfoDao;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.service.FileLibraryInfoManager;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.filter.RequestThreadLocal;
import com.centit.framework.jdbc.service.BaseEntityManagerImpl;
import com.centit.framework.model.basedata.UnitInfo;
import com.centit.framework.model.basedata.UserUnit;
import com.centit.framework.model.basedata.WorkGroup;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
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
                if (StringBaseOpt.isNvl(e.getCreator())) {
                    e.setCreator(fileLibraryInfo.getCreateUser());
                }
                if (StringBaseOpt.isNvl(e.getWorkGroupParameter().getRoleCode())) {
                    e.getWorkGroupParameter().setRoleCode(WORKGROUP_ROLECODE_MEMBER);
                }
            });
        }
        fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
        fileLibraryInfoDao.saveObjectReferences(fileLibraryInfo);
    }

    @Override
    public List<FileLibraryInfo> listFileLibrary(String userCode) {
        Map<String, Object> map = new HashMap<>();
        String sqlBuilder="";
        if (StringUtils.isNotBlank(userCode) && getUnits(userCode) != null && getUnits(userCode).size() > 0) {
            map.put("ownunit", getUnits(userCode));
            map.put("accessuser", userCode);
            sqlBuilder="where own_unit in (:ownunit) and (library_type='O' or (library_type='P' and own_user=:accessuser) " +
                "or library_id in (select group_id from work_group where user_code=:accessuser and group_id not in (select os_id from f_os_info)))";
        }
        List<FileLibraryInfo> libraryInfos = fileLibraryInfoDao.listObjectsByFilter(sqlBuilder, map);
        boolean hasPerson = libraryInfos.stream().anyMatch(fileLibraryInfo -> "P".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
            userCode.equals(fileLibraryInfo.getOwnUser()));
        if (!hasPerson) {
            libraryInfos.add(getPersonLibraryInfo(userCode));
        }
        for (String unitCode : getUnits(userCode)) {
            boolean hasUnit = libraryInfos.stream().anyMatch(fileLibraryInfo -> "O".equalsIgnoreCase(fileLibraryInfo.getLibraryType()) &&
                unitCode.equals(fileLibraryInfo.getOwnUnit()));
            if (!hasUnit) {
                libraryInfos.add(getUnitLibraryInfo(unitCode, userCode));
            }
        }
        return libraryInfos.stream().sorted(Comparator.comparing(FileLibraryInfo::getLibraryType, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public List<UnitInfo> listUnitPathsByUserCode(String userCode) {
        HttpServletRequest request = RequestThreadLocal.getLocalThreadWrapperRequest();
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        List<UnitInfo> result = new ArrayList<>(10);
        for (String unit : getUnits(userCode)) {
            if (CodeRepositoryUtil.getUnitInfoByCode(topUnit, unit) != null) {
                result.add(CodeRepositoryUtil.getUnitInfoByCode(topUnit, unit));
            }
        }
        return result;
    }

    @Override
    public void initPersonLibrary(String userCode) {
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUser", userCode, "libraryType", "P"));
        if (null == fileLibraryInfos || fileLibraryInfos.size() == 0) {
            FileLibraryInfo fileLibraryInfo = getPersonLibraryInfo(userCode);
            createFileLibrary(fileLibraryInfo);
        }
    }

    private FileLibraryInfo getPersonLibraryInfo(String userCode) {
        FileLibraryInfo fileLibraryInfo = new FileLibraryInfo();
        fileLibraryInfo.setCreateUser(userCode);
        fileLibraryInfo.setOwnUser(userCode);
        fileLibraryInfo.setLibraryName("我的文件");
        fileLibraryInfo.setLibraryType("P");
        fileLibraryInfo.setIsCreateFolder("T");
        fileLibraryInfo.setIsUpload("T");
        fileLibraryInfo.setOwnUnit(WebOptUtils.getCurrentTopUnit(RequestThreadLocal.getLocalThreadWrapperRequest()));
        fileLibraryInfoDao.saveNewObject(fileLibraryInfo);
        return fileLibraryInfo;
    }

    @Override
    public void initUnitLibrary(String unitCode, String userCode) {
        List<FileLibraryInfo> fileLibraryInfos = fileLibraryInfoDao.listObjectsByProperties(
            CollectionsOpt.createHashMap("ownUnit", unitCode, "libraryType", "O"));
        if (null == fileLibraryInfos || fileLibraryInfos.size() == 0) {
            FileLibraryInfo fileLibraryInfo = getUnitLibraryInfo(unitCode, userCode);
            createFileLibrary(fileLibraryInfo);
        }
    }

    private FileLibraryInfo getUnitLibraryInfo(String unitCode, String userCode) {
        HttpServletRequest request = RequestThreadLocal.getLocalThreadWrapperRequest();
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
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
    public Set<String> getUnits(String userCode) {
        if (userCode == null) {
            return null;
        }
        HttpServletRequest request = RequestThreadLocal.getLocalThreadWrapperRequest();
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
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
    public FileLibraryInfo getFileLibrary(String libraryId) {
        FileLibraryInfo fileLibraryInfo = fileLibraryInfoDao.getObjectWithReferences(libraryId);
        if (fileLibraryInfo == null) {
            return null;
        }
        if (!StringBaseOpt.isNvl(fileLibraryInfo.getOwnUser())) {
            HttpServletRequest request = RequestThreadLocal.getLocalThreadWrapperRequest();
            String topUnit = WebOptUtils.getCurrentTopUnit(request);
            fileLibraryInfo.setOwnName(CodeRepositoryUtil.getUserName(topUnit, fileLibraryInfo.getOwnUser()));
        }
        return fileLibraryInfo;
    }

    @Override
    public void deleteFileLibrary(String libraryId) {
        fileLibraryInfoDao.deleteObjectById(libraryId);
    }

    @Override
    public boolean checkAuth(FileInfo fileInfo, String userCode, String authCode) {
        Set<String> unitPath = this.getUnits(userCode);

        if (!"undefined".equals(userCode) && !StringBaseOpt.isNvl(userCode) && !StringBaseOpt.isNvl(fileInfo.getLibraryId())) {
            FileLibraryInfo fileLibraryInfo = this.getFileLibrary(fileInfo.getLibraryId());
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
        if (!StringBaseOpt.isNvl(authCode) && !"undefined".equals(authCode)) {
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

