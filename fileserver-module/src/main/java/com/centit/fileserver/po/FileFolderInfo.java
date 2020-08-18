package com.centit.fileserver.po;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    @Id
    @Column(name = "folder_id")
    private String folderId;

    /**
     * 库id 库id
     */
    @Column(name = "library_id")
    private String libraryId;
    /**
     * 上级文件夹 上级文件夹
     */
    @Column(name = "parent_folder")
    private String parentFolder;
    /**
     * 文件夹路径 文件夹路径
     */
    @Column(name = "folder_path")
    private String folderPath;
    /**
     * 是否可以创建子目录 是否可以创建子目录
     */
    @Column(name = "is_create_folder")
    private String isCreateFolder;
    /**
     * 是否可以上传文件 是否可以上传文件
     */
    @Column(name = "is_upload")
    private String isUpload;
    /**
     * 验证码 验证码
     */
    @Column(name = "auth_code")
    private String authCode;
    /**
     * 文件夹名称 文件夹名称
     */
    @Column(name = "folder_name")
    private String folderName;
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
    /**
     * 修改人 修改人
     */
    @Column(name = "update_user")
    private String updateUser;
    /**
     * 修改时间 修改时间
     */
    @Column(name = "update_time")
    private java.sql.Date updateTime;

    @OneToMany(mappedBy = "fileFolderInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FileInfo> fileInfos;


    public void addFileInfo(FileInfo fileInfo) {
        if (this.fileInfos == null) {
            this.fileInfos = new ArrayList<FileInfo>();
        }
        this.fileInfos.add(fileInfo);
    }

    public void removeFileInfo(FileInfo fileInfo) {
        if (this.fileInfos == null) {
            return;
        }
        this.fileInfos.remove(fileInfo);
    }

    public FileInfo newFileInfo() {
        FileInfo res = new FileInfo();

        res.setFolderId(this.getFolderId());

        return res;
    }

    /**
     * 替换子类对象数组，这个函数主要是考虑hibernate中的对象的状态，以避免对象状态不一致的问题
     */
    public void replaceFileInfos(List<FileInfo> fileInfos) {
        List<FileInfo> newObjs = new ArrayList<FileInfo>();
        for (FileInfo p : fileInfos) {
            if (p == null) {
                continue;
            }
            FileInfo newdt = newFileInfo();
            newdt.copyNotNullProperty(p);
            newObjs.add(newdt);
        }
        //delete
        boolean found = false;
        List<FileInfo> oldObjs = new ArrayList<FileInfo>();
        oldObjs.addAll(getFileInfos());

        for (FileInfo odt : oldObjs) {
            found = false;
            for (FileInfo newdt : newObjs) {
                if (odt.getFileId().equals(newdt.getFileId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removeFileInfo(odt);
            }
        }
        oldObjs.clear();
        //insert or update
        for (FileInfo newdt : newObjs) {
            found = false;
            for (Iterator<FileInfo> it = getFileInfos().iterator();
                 it.hasNext(); ) {
                FileInfo odt = it.next();
                if (odt.getFileId().equals(newdt.getFileId())) {
                    odt.copy(newdt);
                    found = true;
                    break;
                }
            }
            if (!found) {
                addFileInfo(newdt);
            }
        }
    }


    public FileFolderInfo copy(FileFolderInfo other) {


        this.setFolderId(other.getFolderId());


        this.libraryId = other.getLibraryId();
        this.parentFolder = other.getParentFolder();
        this.folderPath = other.getFolderPath();
        this.isCreateFolder = other.getIsCreateFolder();
        this.isUpload = other.getIsUpload();
        this.authCode = other.getAuthCode();
        this.folderName = other.getFolderName();
        this.createUser = other.getCreateUser();
        this.createTime = other.getCreateTime();
        this.updateUser = other.getUpdateUser();
        this.updateTime = other.getUpdateTime();

        this.fileInfos = other.getFileInfos();
        return this;
    }

    public FileFolderInfo copyNotNullProperty(FileFolderInfo other) {


        if (other.getFolderId() != null) {
            this.setFolderId(other.getFolderId());
        }

        if (other.getLibraryId() != null) {
            this.libraryId = other.getLibraryId();
        }
        if (other.getParentFolder() != null) {
            this.parentFolder = other.getParentFolder();
        }
        if (other.getFolderPath() != null) {
            this.folderPath = other.getFolderPath();
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
        if (other.getFolderName() != null) {
            this.folderName = other.getFolderName();
        }
        if (other.getCreateUser() != null) {
            this.createUser = other.getCreateUser();
        }
        if (other.getCreateTime() != null) {
            this.createTime = other.getCreateTime();
        }
        if (other.getUpdateUser() != null) {
            this.updateUser = other.getUpdateUser();
        }
        if (other.getUpdateTime() != null) {
            this.updateTime = other.getUpdateTime();
        }

        //this.fileInfos = other.getFileInfos();
        replaceFileInfos(other.getFileInfos());

        return this;
    }

    public FileFolderInfo clearProperties() {

        this.libraryId = null;
        this.parentFolder = null;
        this.folderPath = null;
        this.isCreateFolder = null;
        this.isUpload = null;
        this.authCode = null;
        this.folderName = null;
        this.createUser = null;
        this.createTime = null;
        this.updateUser = null;
        this.updateTime = null;

        this.fileInfos = new ArrayList<FileInfo>();
        return this;
    }
}
