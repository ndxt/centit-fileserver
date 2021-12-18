package com.centit.fileserver.common;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhf
 */
public interface OperateFileLibrary {
    FileLibraryInfo insertFileLibrary(FileLibraryInfo fileLibrary);

    FileLibraryInfo getFileLibrary(String libraryId);

}
