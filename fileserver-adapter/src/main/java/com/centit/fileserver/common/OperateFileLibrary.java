package com.centit.fileserver.common;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhf
 */
public interface OperateFileLibrary {
    IFileLibrary insertFileLibrary(IFileLibrary fileLibrary);
    IFileLibrary getFileLibrary(String libraryId);
    IFileLibrary getInstance();
}
