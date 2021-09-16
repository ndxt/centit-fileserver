package com.centit.fileserver.common;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhf
 */
public interface OperateFileLibrary {
    JSONObject insertFileLibrary(JSONObject fileLibrary);
    JSONObject getFileLibrary(String libraryId);
}
