package com.centit.fileserver.utils;

import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OsFileStore implements FileStore {
	
	private String fileRoot;
	
	public OsFileStore(){
		
	}
	
	public OsFileStore(String fileRoot){
		setFileRoot(fileRoot);
	}

	public void setFileRoot(String fileRoot){
		if(fileRoot.endsWith(String.valueOf(File.separatorChar )))
			this.fileRoot = fileRoot;
		else
			this.fileRoot = fileRoot + File.separatorChar;
	}
	public String getFileRoot(){
		/*if(fileRoot==null)
			return SysParametersUtils.getStringValue("fileserver.base.dir");*/
		return fileRoot;
	}
	
	private String matchFileToStoreUrl(String fileMd5, long fileSize){
		String pathname = String.valueOf(fileMd5.charAt(0))
					+ File.separatorChar + fileMd5.charAt(1)
					+ File.separatorChar + fileMd5.charAt(2);
		FileSystemOpt.createDirect(getFileRoot() + pathname);
		return pathname + File.separatorChar + fileMd5 +"_"+fileSize+".dat";
	}
		
	@Override
	public String saveFile(InputStream is, String fileMd5, long fileSize)
			throws IOException {
		String fileStroeUrl =  matchFileToStoreUrl(fileMd5,fileSize);
		String filePath = getFileRoot() + fileStroeUrl;

		FileSystemOpt.createDirect(new File(filePath).getParent());
		FileIOOpt.writeInputStreamToFile(is, filePath);
		
		/*if(!FileUploadUtils.checkFileCompleted(filePath, fileMd5)){
			FileSystemOpt.deleteFile(filePath);
			throw new IOException("文件MD5校验出错："+fileMd5);
		}*/
		return fileStroeUrl;
	}

	public String saveFileByMd5(String sourFilePath, String fileMd5, long fileSize)
			throws IOException {
		String filePath =  matchFileToStoreUrl(fileMd5,fileSize);
		FileSystemOpt.createDirect(new File(getFileRoot() + filePath).getParent());
		FileSystemOpt.fileCopy(sourFilePath,getFileRoot() + filePath);
		return filePath;
	}
	
	@Override
	public String saveFile(String sourFilePath, String fileMd5, long fileSize)
			throws IOException {
		/*if(!FileUploadUtils.checkFileCompleted(sourFilePath, fileMd5))
			throw new IOException("文件MD5校验出错："+fileMd5);*/
		return saveFileByMd5(sourFilePath, fileMd5, fileSize);
	}
	
	@Override
	public String saveFile(String sourFilePath) throws IOException {
		File file = new File(sourFilePath);
		String fileMd5 = FileMD5Maker.makeFileMD5(file);
		long fileSize = file.length();
		return saveFileByMd5(sourFilePath, fileMd5, fileSize);
	}

	@Override
	public boolean checkFile(String fileMd5, long fileSize) {
		String filePath = getFileRoot() + matchFileToStoreUrl(fileMd5,fileSize);
		return FileSystemOpt.existFile(filePath);
	}

	@Override
	public String getFileStoreUrl(String fileMd5, long fileSize) {
		return matchFileToStoreUrl(fileMd5,fileSize);
		//return FileSystemOpt.existFile(getFileRoot() + fileUrl) ? fileUrl : null;
	}
	
	@Override
	public long getFileSize(String fileUrl) throws IOException {
		File f = new File(getFileRoot() + fileUrl);
		return f.length();	
	}

	@Override
	public InputStream loadFileStream(String fileUrl) throws IOException {
		return new FileInputStream(new File(getFileRoot() + fileUrl));
	}

	@Override
	public InputStream loadFileStream(String fileMd5, long fileSize) throws IOException {
		return new FileInputStream(new File(getFileRoot() +
				matchFileToStoreUrl(fileMd5,fileSize)));
	}

	@Override
	public File getFile(String fileUrl) throws IOException {
		return new File(getFileRoot() + fileUrl);
	}
	
	@Override
	public boolean deleteFile(String fileUrl) throws IOException {
		return FileSystemOpt.deleteFile(getFileRoot() + fileUrl);
	}

	@Override
	public boolean deleteFile(String fileMd5, long fileSize) throws IOException {
		return deleteFile( getFileStoreUrl( fileMd5,  fileSize));
	}

	@Override
	public String getFileAccessUrl(String fileStoreUrl) {
		//TODO 这里应该返回一个相对文件服务器的url，
		//因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
		return fileStoreUrl;
	}

	@Override
	public String getFileAccessUrl(String fileMd5, long fileSize) {
		return getFileAccessUrl(getFileStoreUrl(fileMd5,  fileSize));
	}
}
