package com.centit.fileserver.po;

import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorTime;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * create by scaffold 2020-08-18 13:38:13
 *
 * @author codefan@sina.com
 * <p>
 * 文件库信息
 */
@Data
@Entity
@Table(name = "FILE_LIBRARY_INFO")
public class FileLibraryInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 库id 库id
     */
    @ApiModelProperty(value = "库id，新增时不传")
    @Id
    @Column(name = "library_id")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String libraryId;

    /**
     * 库名称 库名称
     */
    @ApiModelProperty(value = "库名称",required = true)
    @Column(name = "library_name")
    private String libraryName;
    /**
     * 类别 类别(个人、组织、项目)
     */
    @ApiModelProperty(value = "类别(个人、组织、项目)",required = true)
    @Column(name = "library_type")
    private String libraryType;
    /**
     * 创建人 创建人
     */
    @Column(name = "create_user")
    @JsonIgnore
    private String createUser;
    /**
     * 创建时间 创建时间
     */
    @Column(name = "create_time")
    @ValueGenerator( strategy= GeneratorType.FUNCTION, value = "today()")
    @JsonIgnore
    private Date createTime;
    /**
     * 所属机构 所属机构
     */
    @ApiModelProperty(value = "所属机构",required = true)
    @Column(name = "own_unit")
    private String ownUnit;
    /**
     * 所属人员 所属人员
     */
    @ApiModelProperty(value = "所属人员",required = true)
    @Column(name = "own_user")
    private String ownUser;
    /**
     * 是否可以创建子目录 是否可以创建子目录
     */
    @ApiModelProperty(value = "是否可以创建子目录",required = true)
    @Column(name = "is_create_folder")
    private String isCreateFolder;
    /**
     * 是否可以上传文件 是否可以上传文件
     */
    @ApiModelProperty(value = "是否可以上传文件",required = true)
    @Column(name = "is_upload")
    private String isUpload;
    /**
     * 验证码 验证码
     */
    @Column(name = "auth_code")
    @JsonIgnore
    private String authCode;

    @ApiModelProperty(value = "修改人")
    @Column(name = "update_user")
    @JsonIgnore
    private String updateUser;
    /**
     * 修改时间 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @Column(name = "update_time")
    @ValueGenerator(strategy = GeneratorType.FUNCTION, occasion = GeneratorTime.UPDATE,
        condition = GeneratorCondition.ALWAYS, value="today()" )
    @JsonIgnore
    private Date updateTime;

    @ApiModelProperty(value = "项目库对应权限")
    @OneToMany(mappedBy = "fileLibraryInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FileLibraryAccess> fileLibraryAccesss;


    public void addFileLibraryAccess(FileLibraryAccess fileLibraryAccess) {
        if (this.fileLibraryAccesss == null) {
            this.fileLibraryAccesss = new ArrayList<>();
        }
        this.fileLibraryAccesss.add(fileLibraryAccess);
    }

    public void removeFileLibraryAccess(FileLibraryAccess fileLibraryAccess) {
        if (this.fileLibraryAccesss == null) {
            return;
        }
        this.fileLibraryAccesss.remove(fileLibraryAccess);
    }

    public FileLibraryAccess newFileLibraryAccess() {
        FileLibraryAccess res = new FileLibraryAccess();

        res.setLibraryId(this.getLibraryId());

        return res;
    }

    /**
     * 替换子类对象数组，这个函数主要是考虑hibernate中的对象的状态，以避免对象状态不一致的问题
     */
    public void replaceFileLibraryAccesses(List<FileLibraryAccess> fileLibraryAccesses) {
        List<FileLibraryAccess> newObjs = new ArrayList<FileLibraryAccess>();
        for (FileLibraryAccess p : fileLibraryAccesses) {
            if (p == null) {
                continue;
            }
            FileLibraryAccess newdt = newFileLibraryAccess();
            newdt.copyNotNullProperty(p);
            newObjs.add(newdt);
        }
        //delete
        boolean found;
        List<FileLibraryAccess> oldObjs = new ArrayList<>();
        oldObjs.addAll(getFileLibraryAccesss());

        for (FileLibraryAccess odt : oldObjs) {
            found = false;
            for (FileLibraryAccess newdt : newObjs) {
                if (odt.getAccessId().equals(newdt.getAccessId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removeFileLibraryAccess(odt);
            }
        }
        oldObjs.clear();
        //insert or update
        for (FileLibraryAccess newdt : newObjs) {
            found = false;
            for (FileLibraryAccess odt : getFileLibraryAccesss()) {
                if (odt.getAccessId().equals(newdt.getAccessId())) {
                    odt.copy(newdt);
                    found = true;
                    break;
                }
            }
            if (!found) {
                addFileLibraryAccess(newdt);
            }
        }
    }

    public FileLibraryInfo copy(FileLibraryInfo other) {


        this.setLibraryId(other.getLibraryId());


        this.libraryName = other.getLibraryName();
        this.libraryType = other.getLibraryType();
        this.createUser = other.getCreateUser();
        this.createTime = other.getCreateTime();
        this.ownUnit = other.getOwnUnit();
        this.ownUser = other.getOwnUser();
        this.isCreateFolder = other.getIsCreateFolder();
        this.isUpload = other.getIsUpload();
        this.authCode = other.getAuthCode();

        this.fileLibraryAccesss = other.getFileLibraryAccesss();
        return this;
    }

    public FileLibraryInfo copyNotNullProperty(FileLibraryInfo other) {


        if (other.getLibraryId() != null) {
            this.setLibraryId(other.getLibraryId());
        }

        if (other.getLibraryName() != null) {
            this.libraryName = other.getLibraryName();
        }
        if (other.getLibraryType() != null) {
            this.libraryType = other.getLibraryType();
        }
        if (other.getCreateUser() != null) {
            this.createUser = other.getCreateUser();
        }
        if (other.getCreateTime() != null) {
            this.createTime = other.getCreateTime();
        }
        if (other.getOwnUnit() != null) {
            this.ownUnit = other.getOwnUnit();
        }
        if (other.getOwnUser() != null) {
            this.ownUser = other.getOwnUser();
        }
        if (other.getIsCreateFolder() != null) {
            this.isCreateFolder = other.getIsCreateFolder();
        }
        if (other.getIsUpload() != null) {
            this.isUpload = other.getIsUpload();
        }
        if (other.getAuthCode() != null) {
            this.authCode = other.getAuthCode();
        }

        replaceFileLibraryAccesses(other.getFileLibraryAccesss());

        return this;
    }

    public FileLibraryInfo clearProperties() {

        this.libraryName = null;
        this.libraryType = null;
        this.createUser = null;
        this.createTime = null;
        this.ownUnit = null;
        this.ownUser = null;
        this.isCreateFolder = null;
        this.isUpload = null;
        this.authCode = null;

        this.fileLibraryAccesss = new ArrayList<>();
        return this;
    }
}
