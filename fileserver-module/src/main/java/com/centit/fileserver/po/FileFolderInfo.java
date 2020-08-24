package com.centit.fileserver.po;

import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorTime;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * <p>
 * 文件夹信息
 */
@Data
@Entity
@Table(name = "FILE_FOLDER_INFO")
public class FileFolderInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 文件夹id 文件夹id
     */
    @ApiModelProperty(value = "文件夹id,新增时不用传")
    @Id
    @Column(name = "folder_id")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String folderId;

    /**
     * 库id 库id
     */
    @ApiModelProperty(value = "库id", required = true)
    @Column(name = "library_id")
    private String libraryId;
    /**
     * 上级文件夹 上级文件夹
     */
    @ApiModelProperty(value = "上级文件夹")
    @Column(name = "parent_folder")
    @JsonIgnore
    private String parentFolder;
    /**
     * 文件夹路径 文件夹路径
     */
    @ApiModelProperty(value = "文件路径，/上级路径/本级文件夹id", required = true)
    @Column(name = "folder_path")
    @Basic(fetch = FetchType.LAZY)
    private String folderPath;
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
    @ApiModelProperty(value = "验证码")
    @Column(name = "auth_code")
    @JsonIgnore
    private String authCode;
    /**
     * 文件夹名称 文件夹名称
     */
    @ApiModelProperty(value = "文件夹名称", required = true)
    @Column(name = "folder_name")
    @NotNull(message = "文件夹名称不能为空")
    private String folderName;
    /**
     * 创建人 创建人
     */
    @ApiModelProperty(value = "创建人")
    @Column(name = "create_user")
    @JsonIgnore
    private String createUser;
    /**
     * 创建时间 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Column(name = "create_time")
    @ValueGenerator( strategy= GeneratorType.FUNCTION,occasion = GeneratorTime.NEW, value = "today()")
    @JsonIgnore
    private Date createTime;
    /**
     * 修改人 修改人
     */
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


    public String getParentFolder(){
        if(StringUtils.isBlank(this.parentFolder)) {
            return StringUtils.substringAfterLast(this.folderPath, "/");
        }
        return this.parentFolder;
    }

}
