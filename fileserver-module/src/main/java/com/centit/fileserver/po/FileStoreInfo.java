package com.centit.fileserver.po;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "FILE_STORE_INFO")
public class FileStoreInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="FILE_MD5")
    private String fileMd5;

    @Column(name="FILE_SIZE")
    private Long fileSize;

    @Column(name="FILE_STORE_PATH")
    private String fileStorePath;

    // 文件的引用计数
    @Column(name = "FILE_REFERENCE_COUNT")
    private Long fileReferenceCount;


    public FileStoreInfo() {}

    public FileStoreInfo(String fileMd5, Long fileSize, String fileStorePath, Long fileReferenceCount) {
        this.fileMd5 = fileMd5;
        this.fileSize = fileSize;
        this.fileStorePath = fileStorePath;
        this.fileReferenceCount = fileReferenceCount;
    }
}
