package com.centit.fileserver.common;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhf
 */
public interface OperateFileLibrary {
    FileLibrary insertFileLibrary(FileLibrary fileLibrary);

    FileLibrary getFileLibrary(String libraryId);

}
