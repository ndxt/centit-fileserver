package com.centit.fileserver.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * create by scaffold 2020-08-18 13:38:15
 *
 * @author codefan@sina.com
 * <p>
 * 项目库授权信息
 */
@Data
@Entity
@Table(name = "FILE_LIBRARY_ACCESS")
public class FileLibraryAccess implements java.io.Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 授权id 授权id
     */
    @Id
    @Column(name = "access_id")
    private String accessId;

    /**
     * 库id 库id
     */
    @Column(name = "library_id")
    private String libraryId;
    /**
     * 被授权人员 被授权人员
     */
    @Column(name = "access_usercode")
    private String accessUsercode;
    /**
     * 创建人 创建人
     */
    @Column(name = "create_user")
    private String createUser;
    /**
     * 创建时间 创建时间
     */
    @Column(name = "create_time")
    private java.sql.Date createTime;


    public FileLibraryAccess copy(FileLibraryAccess other) {
        this.setAccessId(other.getAccessId());
        this.libraryId = other.getLibraryId();
        this.accessUsercode = other.getAccessUsercode();
        this.createUser = other.getCreateUser();
        this.createTime = other.getCreateTime();
        return this;
    }

    public FileLibraryAccess copyNotNullProperty(FileLibraryAccess other) {
        if (other.getAccessId() != null) {
            this.setAccessId(other.getAccessId());
        }

        if (other.getLibraryId() != null) {
            this.libraryId = other.getLibraryId();
        }
        if (other.getAccessUsercode() != null) {
            this.accessUsercode = other.getAccessUsercode();
        }
        if (other.getCreateUser() != null) {
            this.createUser = other.getCreateUser();
        }
        if (other.getCreateTime() != null) {
            this.createTime = other.getCreateTime();
        }

        return this;
    }

    public FileLibraryAccess clearProperties() {

        this.libraryId = null;
        this.accessUsercode = null;
        this.createUser = null;
        this.createTime = null;

        return this;
    }
}
