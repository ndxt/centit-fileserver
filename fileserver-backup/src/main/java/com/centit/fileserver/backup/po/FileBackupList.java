package com.centit.fileserver.backup.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "FILE_BACKUP_LIST")
public class FileBackupList implements Serializable{

    @Id
    @Column(name = "BACKUP_ID")
    private String backupId;

    @Id
    @Column(name = "FILE_ID")
    private String fileId;

    /**
     * I: init E: error S: success
     */
    @Column(name = "BACKUP_STATUS")
    private String backupStatus;

}
