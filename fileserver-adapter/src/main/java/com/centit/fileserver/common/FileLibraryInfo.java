package com.centit.fileserver.common;

import com.centit.framework.model.basedata.WorkGroup;
import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorTime;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
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
public class FileLibraryInfo implements Serializable {
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
    @ApiModelProperty(value = "库名称", required = true)
    @Column(name = "library_name")
    @NotBlank
    private String libraryName;
    /**
     * 类别 类别(P 个人、T 团队 team 、 O 组织、I 项目)
     */
    @ApiModelProperty(value = "类别(个人、组织、项目)", required = true)
    @Column(name = "library_type")
    private String libraryType;
    /**
     * 创建人 创建人
     */
    @Column(name = "create_user")
    private String createUser;
    /**
     * 创建时间 创建时间
     */
    @Column(name = "create_time")
    @ValueGenerator(strategy = GeneratorType.FUNCTION, value = "today()")
    private Date createTime;
    /**
     * 所属机构 所属机构
     */
    @ApiModelProperty(value = "所属机构", required = true)
    @Column(name = "own_unit")
    private String ownUnit;
    /**
     * 所属人员 所属人员
     */
    @ApiModelProperty(value = "所属人员", required = true)
    @Column(name = "own_user")
    private String ownUser;
    @Transient
    private String ownName;
    /**
     * 是否可以创建子目录 是否可以创建子目录
     */
    @ApiModelProperty(value = "是否可以创建子目录", required = true)
    @Column(name = "is_create_folder")
    private String isCreateFolder;
    /**
     * 是否可以上传文件 是否可以上传文件
     */
    @ApiModelProperty(value = "是否可以上传文件", required = true)
    @Column(name = "is_upload")
    private String isUpload;
    /**
     * 验证码 验证码
     */
    @Column(name = "auth_code")
    private String authCode;

    @ApiModelProperty(value = "修改人")
    @Column(name = "update_user")
    private String updateUser;
    /**
     * 修改时间 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @Column(name = "update_time")
    @ValueGenerator(strategy = GeneratorType.FUNCTION, occasion = GeneratorTime.UPDATE,
        condition = GeneratorCondition.ALWAYS, value = "today()")
    private Date updateTime;

    @ApiModelProperty(value = "项目库对应权限")
    @OneToMany(mappedBy = "fileLibraryInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "library_id", referencedColumnName = "group_id")
    private List<WorkGroup> workGroups;

    public void copyNotNull(FileLibraryInfo fileLibrary) {
        if (fileLibrary.getLibraryId() != null) {
            libraryId = fileLibrary.getLibraryId();
        }
        if (fileLibrary.getLibraryName() != null) {
            libraryName = fileLibrary.getLibraryName();
        }
        if (fileLibrary.getLibraryType() != null) {
            libraryType = fileLibrary.getLibraryType();
        }
        if (fileLibrary.getCreateUser() != null) {
            createUser = fileLibrary.getCreateUser();
        }
    }

}
