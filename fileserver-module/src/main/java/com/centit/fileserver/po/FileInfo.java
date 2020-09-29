package com.centit.fileserver.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.centit.framework.core.dao.DictionaryMap;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorTime;
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
@Table(name = "FILE_INFO")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "FILE_ID")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String fileId;

    @Column(name = "FILE_MD5")
    private String fileMd5;

    @Column(name = "FILE_NAME")
    private String fileName;
    /**
     * 这个属性业务系统可以自行解释，在内部文档管理中表现为文件的显示目录
     */
    @Column(name = "FILE_SHOW_PATH")
    private String fileShowPath;

//    @Column(name="FILE_STORE_PATH")
//    private String fileStorePath;
    /**
     * 文件的后缀明 ext file name
     */
    @Column(name = "FILE_TYPE")
    private String fileType;

    /**
     * C : 正在上传 A:已审核 N : 正常 Z:空文件 F:文件上传失败  D:已删除
     */
    @Column(name = "FILE_STATE")
    private String fileState;

    @Column(name = "FILE_DESC")
    private String fileDesc;

    @Column(name = "INDEX_STATE")
    private String indexState;

    @Column(name = "DOWNLOAD_TIMES")
    private Long downloadTimes;

    @Column(name = "OS_ID")
    private String osId;

    @Column(name = "OPT_ID")
    private String optId;

    @Column(name = "OPT_METHOD")
    private String optMethod;

    @Column(name = "OPT_TAG")
    private String optTag;

    @Column(name = "CREATED")
    private String created;

    @Column(name = "CREATE_TIME")
    @ValueGenerator(strategy = GeneratorType.FUNCTION, occasion = GeneratorTime.NEW,
        value = "today()")
    private Date createTime;

//    @Column(name="FILE_SIZE")
//    private Long fileSize;

    //加密算法
    @Column(name = "ENCRYPT_TYPE")
    private String encryptType;

    @Column(name = "FILE_OWNER")
    @DictionaryMap(fieldName="ownerName",value="userCode")
    private String fileOwner;

    @Column(name = "FILE_UNIT")
    private String fileUnit;


    @Column(name = "ATTACHED_FILE_MD5")
    private String attachedFileMd5;

    /**
     * 附属文件类别： T：缩略图  P： pdf只读文件
     * 预处理生成新文件，将原文件作为附属文件（缩略图除外）
     */
    @Column(name = "ATTACHED_TYPE")
    private String attachedType;
    @Column(name = "auth_code")
    @JSONField(serialize = false)
    private String authCode;
    @Column(name = "library_id")
    private String libraryId;
    @Column(name = "parent_folder")
    private String parentFolder;

    public FileInfo() {
        indexState = "N";
        encryptType = "N";
        fileState = "N";
        createTime = DatetimeOpt.currentUtilDate();
        downloadTimes = 0L;
    }
    public String getParentFolder(){
        if(StringUtils.isBlank(this.parentFolder)) {
            return StringUtils.substringAfterLast(this.fileShowPath, "/");
        }
        return this.parentFolder;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (StringUtils.isNoneBlank(fileName) && StringUtils.isBlank(fileType)) {
            fileType = FileType.getFileExtName(fileName);
        }
    }


    public Long addDownloadTimes() {
        if (this.downloadTimes == null) {
            downloadTimes = 1L;
        } else {
            downloadTimes += 1;
        }
        return downloadTimes;
    }


    public void copyNotNullProperty(FileInfo other) {
        if (StringUtils.isNotBlank(other.getFileName())) {
            this.fileName = other.getFileName();
        }

        if (StringUtils.isNotBlank(other.getFileShowPath())) {
            this.fileShowPath = other.getFileShowPath();
        }

        if (StringUtils.isNotBlank(other.getFileType())) {
            this.fileType = other.getFileType();
        }
        /**
         * C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除
         */
        if (StringUtils.isNotBlank(other.getFileState())) {
            this.fileState = other.getFileState();
        }

        if (StringUtils.isNotBlank(other.getFileDesc())) {
            this.fileDesc = other.getFileDesc();
        }
        //this.indexState = other.getIndexState();
        //this.downloadTimes = other.getDownloadTimes();
        if (StringUtils.isNotBlank(other.getOsId())) {
            this.osId = other.getOsId();
        }
        if (StringUtils.isNotBlank(other.getOptId())) {
            this.optId = other.getOptId();
        }
        if (StringUtils.isNotBlank(other.getOptMethod())) {
            this.optMethod = other.getOptMethod();
        }
        if (StringUtils.isNotBlank(other.getOptTag())) {
            this.optTag = other.getOptTag();
        }
        if (other.getCreated() != null) {
            this.created = other.getCreated();
        }
        if (other.getCreateTime() != null) {
            this.createTime = other.getCreateTime();
        }
        //this.fileSize = other.getFileSize();
        //加密算法
        //this.encryptType=other.getEncryptType();
        if (StringUtils.isNotBlank(other.getFileOwner())) {
            this.fileOwner = other.getFileOwner();
        }

        if (StringUtils.isNotBlank(other.getFileUnit())) {
            this.fileUnit = other.getFileUnit();
        }
        if (StringUtils.isNotBlank(other.getParentFolder())) {
            this.parentFolder = other.getParentFolder();
        }
        if (StringUtils.isNotBlank(other.getLibraryId())) {
            this.libraryId = other.getLibraryId();
        }
        //this.attachedStorePath=other.getAttachedStorePath();
        /*
         * 附属文件类别： T：缩略图  P： pdf只读文件
         */
        //this.attachedType = other.getAttachedType();
    }

    public void copy(FileInfo other) {
        this.fileName = other.getFileName();

        this.fileShowPath = other.getFileShowPath();
        this.fileType = other.getFileType();

        //C : 正在上传  N : 正常 Z:空文件 F:文件上传失败  D:已删除

        this.fileState = other.getFileState();

        this.fileDesc = other.getFileDesc();

        //this.indexState = other.getIndexState();

        //this.downloadTimes = other.getDownloadTimes();

        this.osId = other.getOsId();

        this.optId = other.getOptId();

        this.optMethod = other.getOptMethod();

        this.optTag = other.getOptTag();

        this.created = other.getCreated();

        this.createTime = other.getCreateTime();

        //this.fileSize = other.getFileSize();

        //加密算法
        //this.encryptType=other.getEncryptType();

        this.fileOwner = other.getFileOwner();

        this.fileUnit = other.getFileUnit();
        this.authCode = other.getAuthCode();
        this.libraryId = other.libraryId;
        this.parentFolder = other.parentFolder;

        //this.attachedStorePath=other.getAttachedStorePath();

        /**
         * 附属文件类别： T：缩略图  P： pdf只读文件
         */
        //this.attachedType = other.getAttachedType();
    }
}
