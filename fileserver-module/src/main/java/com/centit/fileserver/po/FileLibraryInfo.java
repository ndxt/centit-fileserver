package com.centit.fileserver.po;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
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
    @Id
    @Column(name = "library_id")
    private String libraryId;

    /**
     * 库名称 库名称
     */
    @Column(name = "library_name")
    private String libraryName;
    /**
     * 类别 类别(个人、组织、项目)
     */
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
    private java.sql.Date createTime;
    /**
     * 所属机构 所属机构
     */
    @Column(name = "own_unit")
    private String ownUnit;
    /**
     * 所属人员 所属人员
     */
    @Column(name = "own_user")
    private String ownUser;
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

    @OneToMany(mappedBy = "fileLibraryInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FileLibraryAccess> fileLibraryAccesss;

    @OneToMany(mappedBy = "fileLibraryInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FileFolderInfo> fileFolderInfos;

    @OneToMany(mappedBy = "fileLibraryInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FileInfo> fileInfos;


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


    public void addFileFolderInfo(FileFolderInfo fileFolderInfo) {
        if (this.fileFolderInfos == null) {
            this.fileFolderInfos = new ArrayList<FileFolderInfo>();
        }
        this.fileFolderInfos.add(fileFolderInfo);
    }

    public void removeFileFolderInfo(FileFolderInfo fileFolderInfo) {
        if (this.fileFolderInfos == null) {
            return;
        }
        this.fileFolderInfos.remove(fileFolderInfo);
    }

    public FileFolderInfo newFileFolderInfo() {
        FileFolderInfo res = new FileFolderInfo();

        res.setLibraryId(this.getLibraryId());

        return res;
    }

    /**
     * 替换子类对象数组，这个函数主要是考虑hibernate中的对象的状态，以避免对象状态不一致的问题
     */
    public void replaceFileFolderInfos(List<FileFolderInfo> fileFolderInfos) {
        List<FileFolderInfo> newObjs = new ArrayList<FileFolderInfo>();
        for (FileFolderInfo p : fileFolderInfos) {
            if (p == null) {
                continue;
            }
            FileFolderInfo newdt = newFileFolderInfo();
            newdt.copyNotNullProperty(p);
            newObjs.add(newdt);
        }
        //delete
        boolean found;
        List<FileFolderInfo> oldObjs = new ArrayList<FileFolderInfo>();
        oldObjs.addAll(getFileFolderInfos());

        for (FileFolderInfo odt : oldObjs) {
            found = false;
            for (FileFolderInfo newdt : newObjs) {
                if (odt.getFolderId().equals(newdt.getFolderId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                removeFileFolderInfo(odt);
            }
        }
        oldObjs.clear();
        //insert or update
        for (FileFolderInfo newdt : newObjs) {
            found = false;
            for (Iterator<FileFolderInfo> it = getFileFolderInfos().iterator();
                 it.hasNext(); ) {
                FileFolderInfo odt = it.next();
                if (odt.getFolderId().equals(newdt.getFolderId())) {
                    odt.copy(newdt);
                    found = true;
                    break;
                }
            }
            if (!found) {
                addFileFolderInfo(newdt);
            }
        }
    }


    public void addFileInfo(FileInfo fileInfo) {
        if (this.fileInfos == null) {
            this.fileInfos = new ArrayList<>();
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

        res.setLibraryId(this.getLibraryId());

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
        this.fileFolderInfos = other.getFileFolderInfos();
        this.fileInfos = other.getFileInfos();
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

        replaceFileFolderInfos(other.getFileFolderInfos());

        replaceFileInfos(other.getFileInfos());

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
        this.fileFolderInfos = new ArrayList<>();
        this.fileInfos = new ArrayList<>();
        return this;
    }
}
