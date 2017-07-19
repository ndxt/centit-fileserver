package com.centit.fileserver.fileaccess;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
/**
 *  a：is_index 是否加入全文检索 
    b：encrypt_type 加密方式 N : 没有加密 Z：zipFile D:DES加密 
    c：encrypt_password, 如果加密密码 
    e：add_pdf ； P 添加pdf 副本 
    f：watermark； 如果添加pdf副本 是否需要加水印，水印 文字 
    g：add_Thumbnail ; 添加缩略图 
    h：Thumbnail_width ；缩略图宽度 
    i：Thumbnail_height ；缩略图高度 
 * @author codefan
 *
 */
public class PretreatInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fileId;
	private String fileMd5;
	private Long fileSize;
	private Boolean isIndex;
	private String encryptType;
	private String encryptPassword;
	private Boolean addPdf;
	private String watermark;
	private Boolean addThumbnail;
	private Integer thumbnailWidth;
	private Integer thumbnailHeight;
	
	public PretreatInfo(){
		isIndex = false;
		addPdf = false;
		addThumbnail=false;
	}

	public boolean needPretreat(){
		return this.getIsIndex() || this.getAddPdf()
				|| this.getAddThumbnail() || 
				!"N".equals(this.getEncryptType());
	}
	
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Boolean getIsIndex() {
		return isIndex==null?false:isIndex;
	}

	public void setIsIndex(Boolean isIndex) {
		this.isIndex = isIndex;
	}
	/**
	 *  加密方式 N : 没有加密 Z：zipFile D:DES加密 //A:AES加密 AES 暂未实现
	 * @return
	 */
	public String getEncryptType() {
		return encryptType==null?"N":encryptType;
	}
	/**
	 *  加密方式 N : 没有加密 Z：zipFile D:DES加密  //A:AES加密 AES 暂未实现
	 * @param encryptType
	 */
	public void setEncryptType(String encryptType) {
		this.encryptType = encryptType;
	}

	public String getEncryptPassword() {
		return encryptPassword;
	}

	public void setEncryptPassword(String encryptPassword) {
		this.encryptPassword = encryptPassword;
	}

	public Boolean getAddPdf() {
		
		return addPdf==null?false:addPdf;
	}	

	public void setAddPdf(Boolean addPdf) {
		this.addPdf = addPdf;
	}

	public String getWatermark() {
		return watermark;
	}

	public void setWatermark(String watermark) {
		this.watermark = watermark;
	}

	public Boolean getAddThumbnail() {
		return addThumbnail==null?false:addThumbnail;
	}

	public void setAddThumbnail(Boolean addThumbnail) {
		this.addThumbnail = addThumbnail;
	}

	public Integer getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(Integer thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public Integer getThumbnailHeight() {
		return thumbnailHeight;
	}

	public void setThumbnailHeight(Integer thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public void copyNotNullProperty(PretreatInfo other){
		if(StringUtils.isNotBlank(other.getFileId()))
			this.fileId = other.getFileId();
		if(StringUtils.isNotBlank(other.getFileMd5()))
			this.fileMd5 = other.getFileMd5();
		if(StringUtils.isNotBlank(other.getEncryptType()))
			this.encryptType = other.getFileMd5();
		if(StringUtils.isNotBlank(other.getEncryptPassword()))
			this.encryptPassword = other.getEncryptPassword();
		if(StringUtils.isNotBlank(other.getWatermark()))
			this.watermark = other.getWatermark();
		if(other.getFileSize()!=null)
			this.fileSize = other.getFileSize();
		if(other.getIsIndex()!=null)
			this.isIndex = other.getIsIndex();
		if(other.getAddPdf()!=null)
			this.addPdf = other.getAddPdf();
		if(other.getAddThumbnail()!=null)
			this.addThumbnail = other.getAddThumbnail();
		if(other.getThumbnailWidth()!=null)
			this.thumbnailWidth = other.getThumbnailWidth();
		if(other.getThumbnailHeight()!=null)
			this.thumbnailHeight = other.getThumbnailHeight();
	}
}
