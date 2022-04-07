package com.centit.fileserver.common;

/**
 * @author zhf
 */
public interface OperateFileLibrary {
    FileLibraryInfo insertFileLibrary(FileLibraryInfo fileLibrary);

    FileLibraryInfo getFileLibrary(String libraryId);

}
