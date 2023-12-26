package com.centit.fileserver.backup.po;

import com.centit.support.database.orm.GeneratorCondition;
import com.centit.support.database.orm.GeneratorType;
import com.centit.support.database.orm.ValueGenerator;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "FILE_BACKUP_INFO")
public class FileBackupInfo implements Serializable {

    @Id
    @Column(name = "FILE_ID")
    @ValueGenerator(strategy = GeneratorType.UUID22, condition = GeneratorCondition.IFNULL)
    private String backupId;

    @Column(name = "OS_ID")
    private String osId;

    @Column(name = "DEST_PATH")
    private String destPath;

    @Column(name = "BEGIN_TIME")
    private Date beginTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "COMPLETE_TIME")
    private Date completedTime;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "FILE_COUNT")
    private Integer fileCount;

}
