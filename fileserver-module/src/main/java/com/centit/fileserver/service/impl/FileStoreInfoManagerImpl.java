package com.centit.fileserver.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.centit.fileserver.dao.FileStoreInfoDao;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.framework.core.dao.DictionaryMapUtils;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.hibernate.dao.DatabaseOptUtils;
import com.centit.framework.hibernate.service.BaseEntityManagerImpl;
import com.centit.support.database.DBType;
import com.centit.support.database.QueryAndNamedParams;
import com.centit.support.database.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Service("fileStoreInfoManager")
@Transactional
public class FileStoreInfoManagerImpl
		extends BaseEntityManagerImpl<FileStoreInfo, String, FileStoreInfoDao>
 	implements FileStoreInfoManager {

	@Resource(name ="fileStoreInfoDao")
	@NotNull
	@Override
	protected void setBaseDao(FileStoreInfoDao baseDao) {
		super.baseDao = baseDao;
	}
	
	@Override
	public String saveNewObject(FileStoreInfo originalFile){

		if(StringUtils.isBlank(originalFile.getOsId()))
			originalFile.setOsId("NOTSET");
		if(StringUtils.isBlank(originalFile.getOptId()))
			originalFile.setOptId("NOTSET");
		return this.baseDao.saveNewObject(originalFile);
		

	}

	@Override
	public String saveNewFile(FileStoreInfo originalFile){
		return saveNewObject(originalFile);
	}

	@Override
	public void deleteFile(FileStoreInfo originalFile){
		
		originalFile.setFileState("D");
		this.baseDao.mergeObject(originalFile);	
	}

	@Override
	public JSONArray listStoredFiles(Map<String, Object> queryParamsMap, PageDesc pageDesc) {
		String queryStatement = 
				"select a.FILE_ID, a.FILE_MD5, a.FILE_NAME, a.FILE_STORE_PATH, a.FILE_TYPE,"
				+ " a.FILE_STATE, a.FILE_DESC, a.INDEX_STATE, a.DOWNLOAD_TIMES, a.OS_ID,"
				+ " a.OPT_ID, a.OPT_METHOD, a.OPT_TAG, a.CREATED, a.CREATE_TIME, FILE_SIZE,"
				+ " a.ENCRYPT_TYPE, a.FILE_OWNER, a.FILE_UNIT, a.ATTACHED_TYPE, a.ATTACHED_STORE_PATH"
				+ " from FILE_STORE_INFO a where 1=1 "
				+ " [ :files | and a.FILE_ID in (:files) ] "
						//:(SPLITFORIN)files 这个地方files如果不是数组而是逗号分隔的就需要添加这个预处理
				+ " [ :(like)fileName | and a.FILE_NAME like :fileName] "
				+ " [ :osId | and a.OS_ID = :osId ]"
				+ " [ :optId | and a.OPT_ID = :optId ]"
				+ " [ :owner | and a.FILE_OWNER = :owner ]"
				+ " [ :unitCode | and a.FILE_UNIT = :unitCode ]"
				+ " [ :beginDate | and a.CREATE_TIME >= :beginDate ]"
				+ " [ :endDate | and a.CREATE_TIME < :endDate ]"
				+ " order by a.CREATE_TIME desc";

		QueryAndNamedParams qap = QueryUtils.translateQuery(queryStatement,queryParamsMap);
		//System.out.println(qap.getQuery());
		JSONArray dataList = DictionaryMapUtils.objectsToJSONArray(
				DatabaseOptUtils.findObjectsAsJSonBySql(baseDao,
				qap.getQuery(), qap.getParams(), pageDesc));
		return dataList;
	}

	/**
	 * 同步保存文件
	 *
	 * @param osId  String
	 * @return  JSONArray
	 */
	@Override
	public JSONArray listOptsByOs(String osId) {
		String queryStatement =
				"select OPT_ID , count(1) as FILE_COUNT " +
						"from FILE_STORE_INFO " +
						"where OS_ID = ? " +
						"group by OPT_ID";
		JSONArray dataList = DatabaseOptUtils.findObjectsAsJSonBySql(
				baseDao,queryStatement,new Object[]{osId},null);
		return dataList;
	}

	@Override
	public JSONArray listFileOwners(String osId, String optId) {
		String queryStatement;
		DBType dbt = DBType.mapDialectToDBType(DatabaseOptUtils.getDialectName());
		if(dbt==DBType.MySql){
			queryStatement = "select ifnull(ifnull(FILE_OWNER,FILE_UNIT),'') as FILE_OWNER, " +
					"count(1) as FILE_COUNT " +
					"from FILE_STORE_INFO " +
					"where OS_ID = ? and OPT_ID = ? " +
					"group by ifnull(ifnull(FILE_OWNER,FILE_UNIT),'') ";
		}else {
			queryStatement = "select nvl(FILE_OWNER,FILE_UNIT) as FILE_OWNER, " +
						"count(1) as FILE_COUNT " +
					"from FILE_STORE_INFO " +
					"where OS_ID = ? and OPT_ID = ? " +
					"group by nvl(FILE_OWNER,FILE_UNIT) ";
		}
		JSONArray dataList = DatabaseOptUtils.findObjectsAsJSonBySql(
				baseDao,queryStatement,new Object[]{osId,optId},null);
		return dataList;
	}

	@Override
	public JSONArray listFilesByOwner(String osId, String optId, String owner) {
		String queryStatement =
				"select a.FILE_ID, a.FILE_MD5, a.FILE_NAME, a.FILE_STORE_PATH, a.FILE_TYPE,"
						+ " a.FILE_STATE, a.FILE_DESC, a.INDEX_STATE, a.DOWNLOAD_TIMES, a.OS_ID,"
						+ " a.OPT_ID, a.OPT_METHOD, a.OPT_TAG, a.CREATED, a.CREATE_TIME, a.FILE_SIZE,"
						+ " a.ENCRYPT_TYPE, a.FILE_OWNER, a.FILE_UNIT, a.ATTACHED_TYPE, a.ATTACHED_STORE_PATH "
						+ " from FILE_STORE_INFO a "
						+ "where a.OS_ID=? and a.OPT_ID = ? " +
							"and (a.FILE_OWNER = ? or a.FILE_UNIT = ?) ";

		JSONArray dataList = DatabaseOptUtils.findObjectsAsJSonBySql(
				baseDao,queryStatement,new Object[]{osId,optId,owner,owner},null);
		return dataList;
	}

}
