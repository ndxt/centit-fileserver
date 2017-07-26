package com.centit.fileserver.po;

import com.centit.support.algorithm.DatetimeOpt;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
@Entity
@Table(name= "FILE_ACCESS_LOG")
public class FileAccessLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name ="ACCESS_TOKEN")
	@GenericGenerator(name = "fileUuid", strategy = "uuid")
	@GeneratedValue(generator = "fileUuid")
	private String accessToken;
	
	@Column(name="FILE_ID")
	private String fileId;
	
	@Column(name="AUTH_TIME")
	private Date authTime;
	
	@Column(name="ACCESS_USERCODE")
	private String accessUsercode;
	
	@Column(name="ACCESS_USENAME")
	private String accessUsename;
	/**
	 * A： 所有权限  S: 下载源文件  T ：下载附属文件 
	 */
	@Column(name="ACCESS_RIGHT")
	private String accessRight;	
	
	@Column(name="TOKEN_EXPIRE_TIME")
	private Date tokenExpireTime;
	
	@Column(name="ACCESS_TIMES")
	private Integer accessTimes;
	
	@Column(name="LAST_ACCESS_TIME")
	private Date lastAccessTime;
	
	@Column(name="LAST_ACCESS_HOST")
	private String lastAccessHost;

	public FileAccessLog(){
		
	}
	
	/**
	 * 下载附属文件
	 * @param getAttach boolean
	 * @return boolean
	 */
	public boolean checkValid(boolean getAttach){
		//如果下载源文件，并且没有权限返回false
		if( !getAttach && "T".equals(accessRight)){
			return false;
		}
		if(tokenExpireTime!=null &&  DatetimeOpt.currentUtilDate().after(tokenExpireTime))
			return false;
		if( accessTimes != null || accessTimes <= 0)
			return false;
		return true;
	}
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public Date getAuthTime() {
		return authTime;
	}

	public void setAuthTime(Date authTime) {
		this.authTime = authTime;
	}

	public String getAccessUsercode() {
		return accessUsercode;
	}

	public void setAccessUsercode(String accessUsercode) {
		this.accessUsercode = accessUsercode;
	}

	public String getAccessUsename() {
		return accessUsename;
	}

	public void setAccessUsename(String accessUsename) {
		this.accessUsename = accessUsename;
	}

	public String getAccessRight() {
		return accessRight;
	}

	public void setAccessRight(String accessRight) {
		this.accessRight = accessRight;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Date getTokenExpireTime() {
		return tokenExpireTime;
	}

	public void setTokenExpireTime(Date tokenExpireTime) {
		this.tokenExpireTime = tokenExpireTime;
	}

	public Integer getAccessTimes() {
		return accessTimes;
	}

	public Integer chargeAccessTimes() {
		if(accessTimes != null)
			accessTimes -= 1;
		return accessTimes;
	}
	public void setAccessTimes(Integer accessTimes) {
		this.accessTimes = accessTimes;
	}

	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public String getLastAccessHost() {
		return lastAccessHost;
	}

	public void setLastAccessHost(String lastAccessHost) {
		this.lastAccessHost = lastAccessHost;
	}
}