package com.centit.fileserver.po;

import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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
    @ApiModelProperty(value = "授权id，新增时不传")
    @Id
    @Column(name = "access_id")
    @ValueGenerator(strategy = GeneratorType.UUID, condition = GeneratorCondition.IFNULL)
    private String accessId;

    /**
     * 库id 库id
     */
    @ApiModelProperty(value = "库id",required = true)
    @Column(name = "library_id")
    private String libraryId;
    /**
     * 被授权人员 被授权人员
     */
    @ApiModelProperty(value = "被授权人员",required = true)
    @Column(name = "access_usercode")
    private String accessUsercode;
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

}
