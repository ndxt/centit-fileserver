package com.centit.fileserver.task;

import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileStore;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Consumer;

@Service
public class SaveFileOpt implements Consumer<FileOptTaskInfo> {

    @Resource
    private FileStore fileStore;
    /**
     * Performs this operation on the given argument.
     *
     * @param fileOptTaskInfo the input argument
     */
    @Override
    public void accept(FileOptTaskInfo fileOptTaskInfo) {

    }
}
