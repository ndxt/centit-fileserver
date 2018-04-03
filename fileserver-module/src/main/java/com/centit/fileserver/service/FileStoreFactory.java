package com.centit.fileserver.service;

import com.centit.fileserver.utils.FileStore;

public interface FileStoreFactory {
	FileStore createDefaultFileStore();

	FileStore createFileStore(String fileStoreType);
}
