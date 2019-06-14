package com.centit.fileserver.po;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import com.centit.support.file.FileType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name= "FILE_INFO")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name ="FILE_ID")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String fileId;

    @Column(name="FILE_MD5")
    private String fileMd5;

    @Column(name="FILE_NAME")
    private  String fileName;
    /**
     * 这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
     */
    @Column(name="FILE_SHOW_PATH")
    private  String fileShowPath;

//    @Column(name="FILE_STORE_PATH")
//    private String fileStorePath;
    /**
     * 文件的后缀明 ext file name
     */
    @Column(name="FILE_TYPE")
    private String fileType;

    /**
     * C : 正在上传 A:已审核 N : 正常 Z:空文件 F:文件上传失败  D:已删除
     */
    @Column(name="FILE_STATE")
    private String fileState;

    @Column(name="FILE_DESC")
    private String fileDesc;

    @Column(name="INDEX_STATE")
    private String indexState;

    @Column(name="DOWNLOAD_TIMES")
    private Long downloadTimes;

    @Column(name="OS_ID")
    private String osId;

    @Column(name="OPT_ID")
    private String optId;

    @Column(name="OPT_METHOD")
    private String optMethod;

    @Column(name ="OPT_TAG")
    private String optTag;

    @Column(name="CREATED")
    private String created;

    @Column(name="CREATE_TIME")
    private Date createTime;

//    @Column(name="FILE_SIZE")
//    private Long fileSize;

    //加密算法
    @Column(name="ENCRYPT_TYPE")
    private String encryptType;

    @Column(name="FILE_OWNER")
    private String fileOwner;

    @Column(name="FILE_UNIT")
    private String fileUnit;


    @Column(name="ATTACHED_FILE_MD5")
    private String attachedFileMd5;

    /**
     * 附属文件类别： T：缩略图  P： pdf只读文件
     * 预处理生成新文件，将原文件作为附属文件（缩略图除外）
     */
    @Column(name="ATTACHED_TYPE")
    private String attachedType;


    public FileInfo(){
        indexState = "N";
        encryptType = "N";
        fileState = "N";
        createTime = DatetimeOpt.currentUtilDate();
        downloadTimes = 0l;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
        if(StringUtils.isNoneBlank(fileName) && StringUtils.isBlank(fileType))
            fileType = FileType.getFileExtName(fileName);
    }

    /**
     * 这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
     * @return String
     */
    public String getFileShowPath() {
        return fileShowPath;
    }

    /**
     * 这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
     * @param fileShowPath String
     */
    public void setFileShowPath(String fileShowPath) {
        this.fileShowPath = fileShowPath;
    }

    /**
     * C : 正在上传  A:已审核 N : 正常 Z:空文件 F:文件上传失败  D:已删除
     * @return String
     */
    public String getFileState() {
        return fileState;
    }

    /**
     * C : 正在上传  A:已审核 N : 正常 Z:空文件 F:文件上传失败  D:已删除
     * @param fileState String
     */
    public void setFileState(String fileState) {
        this.fileState = fileState;
    }

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    /**
     * N ：不需要索引 S：等待索引 I：已索引 F:索引失败
     * @return String
     */
    public String getIndexState() {
        return indexState;
    }

    /**
     * N ：不需要索引 S：等待索引 I：已索引 F:索引失败
     * @param indexState String
     */
    public void setIndexState(String indexState) {
        this.indexState = indexState;
    }

    public Long getDownloadTimes() {
        return downloadTimes;
    }

    public void setDownloadTimes(Long downloadTimes) {
        this.downloadTimes = downloadTimes;
    }

    public Long addDownloadTimes() {
        if(this.downloadTimes ==null)
            downloadTimes = 1l;
        else
            downloadTimes +=1;
        return downloadTimes;
    }

    public String getOptId() {
        return optId;
    }

    public void setOptId(String optId) {
        this.optId = optId;
    }

    public String getOptMethod() {
        return optMethod;
    }

    public void setOptMethod(String optMethod) {
        this.optMethod = optMethod;
    }

    public String getOptTag() {
        return optTag;
    }

    public void setOptTag(String optTag) {
        this.optTag = optTag;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * N : 没有加密   Z：zipFile    D:DES加密   A:AES加密
     * @return String
     */
    public String getEncryptType() {
        return encryptType;
    }
    /**
     * N : 没有加密   Z：zipFile    D:DES加密   A:AES加密
     * @param encryptType String
     */
    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    public String getFileOwner() {
        return fileOwner;
    }

    public void setFileOwner(String fileOwner) {
        this.fileOwner = fileOwner;
    }

    public String getFileUnit() {
        return fileUnit;
    }

    public void setFileUnit(String fileUnit) {
        this.fileUnit = fileUnit;
    }

//    public Long getFileSize() {
//        return fileSize;
//    }

//    public void setFileSize(Long fileSize) {
//        this.fileSize = fileSize;
//    }

    public String getAttachedFileMd5() {
        return attachedFileMd5;
    }

    public void setAttachedFileMd5(String attachedFileMd5) {
        this.attachedFileMd5 = attachedFileMd5;
    }

    /**
     * 附属文件类别：N :   没有  T：缩略图  P： pdf只读文件
     * @return String
     */
    public String getAttachedType() {
        return attachedType;
    }
    /**
     * 附属文件类别：N :   没有  T：缩略图  P： pdf只读文件
     * @param attachedType String
     */
    public void setAttachedType(String attachedType) {
        this.attachedType = attachedType;
    }

    public void copy(FileInfo other){
        this.fileName = other.getFileName();

        this.fileShowPath=other.getFileShowPath();
        this.fileType = other.getFileType();

        //C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除

        this.fileState = other.getFileState();

        this.fileDesc = other.getFileDesc();

        //this.indexState = other.getIndexState();

        //this.downloadTimes = other.getDownloadTimes();

        this.osId=other.getOsId();

        this.optId = other.getOptId();

        this.optMethod = other.getOptMethod();

        this.optTag = other.getOptTag();

        this.created = other.getCreated();

        this.createTime = other.getCreateTime();

        //this.fileSize = other.getFileSize();

        //加密算法
        //this.encryptType=other.getEncryptType();

        this.fileOwner=other.getFileOwner();

        this.fileUnit=other.getFileUnit();


        //this.attachedStorePath=other.getAttachedStorePath();

        /**
         * 附属文件类别： T：缩略图  P： pdf只读文件
         */
        //this.attachedType = other.getAttachedType();
    }

    public void copyNotNullProperty(FileInfo other){

        if(StringUtils.isNotBlank(other.getFileName()))
            this.fileName = other.getFileName();

        if(StringUtils.isNotBlank(other.getFileShowPath()))
            this.fileShowPath=other.getFileShowPath();

        if(StringUtils.isNotBlank(other.getFileType()))
            this.fileType = other.getFileType();

        /**
         * C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除
         */
        if(StringUtils.isNotBlank(other.getFileState()))
            this.fileState = other.getFileState();

        if(StringUtils.isNotBlank(other.getFileDesc()))
            this.fileDesc = other.getFileDesc();

        //this.indexState = other.getIndexState();

        //this.downloadTimes = other.getDownloadTimes();
        if(StringUtils.isNotBlank(other.getOsId()))
            this.osId=other.getOsId();
        if(StringUtils.isNotBlank(other.getOptId()))
            this.optId = other.getOptId();
        if(StringUtils.isNotBlank(other.getOptMethod()))
            this.optMethod = other.getOptMethod();
        if(StringUtils.isNotBlank(other.getOptTag()))
            this.optTag = other.getOptTag();
        if(other.getCreated()!=null)
            this.created = other.getCreated();

        if(other.getCreateTime()!=null)
            this.createTime = other.getCreateTime();

        //this.fileSize = other.getFileSize();

        //加密算法
        //this.encryptType=other.getEncryptType();

        if(StringUtils.isNotBlank(other.getFileOwner()))
            this.fileOwner=other.getFileOwner();

        if(StringUtils.isNotBlank(other.getFileUnit()))
            this.fileUnit=other.getFileUnit();

        //this.attachedStorePath=other.getAttachedStorePath();

        /**
         * 附属文件类别： T：缩略图  P： pdf只读文件
         */
        //this.attachedType = other.getAttachedType();
    }

}
