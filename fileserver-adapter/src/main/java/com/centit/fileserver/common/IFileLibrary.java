package com.centit.fileserver.common;

import java.util.List;

public interface IFileLibrary {
    void setLibraryId(String libraryId);

    void setLibraryName(String libraryName);

    void setLibraryType(String libraryType);

    void setCreateUser(String createUser);
    String getLibraryId();
    String getLibraryName();
    String getLibraryType();
    String getCreateUser();

}
