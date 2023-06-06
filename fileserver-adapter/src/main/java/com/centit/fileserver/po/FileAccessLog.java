package com.centit.fileserver.po;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.database.orm.GeneratorTime;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
@Entity
@Data
@Table(name= "FILE_ACCESS_LOG")
public class FileAccessLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name ="ACCESS_TOKEN")
    @ValueGenerator(strategy = GeneratorType.UUID)
    private String accessToken;

    @Column(name="FILE_ID")
    private String fileId;

    @ValueGenerator(strategy = GeneratorType.FUNCTION, occasion = GeneratorTime.NEW,
        value="today()")
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
        if( accessTimes != null && accessTimes <= 0)
            return false;
        return true;
    }

    public Integer chargeAccessTimes() {
        if(accessTimes != null)
            accessTimes -= 1;
        return accessTimes;
    }
}
