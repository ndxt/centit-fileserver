package com.centit.fileserver.po;

import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * create by scaffold 2020-08-18 13:38:14
 *
 * @author codefan@sina.com
 * <p>
 * 文件收藏
 */
@Data
@Entity
@Table(name = "FILE_FAVORITE")
public class FileFavorite implements java.io.Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 收藏id 收藏id
     */
    @ApiModelProperty(value = "收藏id,新增时不用传")
    @Id
    @Column(name = "favorite_id")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String favoriteId;

    /**
     * 文件ID 文件ID
     */
    @ApiModelProperty(value = "文件ID", required = true)
    @Column(name = "file_id")
    private String fileId;
    /**
     * 收藏人 收藏人
     */
    @Column(name = "favorite_user")
    @JsonIgnore
    private String favoriteUser;
    /**
     * 收藏时间 收藏时间
     */
    @Column(name = "favorite_time")
    @ValueGenerator(strategy = GeneratorType.FUNCTION, value = "today()")
    @JsonIgnore
    private Date favoriteTime;
    @Transient
    private String fileName;
    @Transient
    private String fileType;
    @Transient
    private Long fileSize;
    @Transient
    private String uploadUser;
    @Transient
    private String libraryId;
    @Transient
    private String parentFolder;
    @Transient
    private String showPath;
}
