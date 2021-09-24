package com.centit.fileserver.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FileLibrary implements Serializable {
    private String libraryId;

    private String libraryName;

    private String libraryType;

    private String createUser;
    private Date createTime;

    public void copyNotNull(FileLibrary fileLibrary){
        if(fileLibrary.getLibraryId()!=null){
            libraryId = fileLibrary.getLibraryId();
        }
        if(fileLibrary.getLibraryName()!=null){
            libraryName = fileLibrary.getLibraryName();
        }
        if(fileLibrary.getLibraryType()!=null){
            libraryType = fileLibrary.getLibraryType();
        }
        if(fileLibrary.getCreateUser()!=null){
            createUser = fileLibrary.getCreateUser();
        }
    }
}
