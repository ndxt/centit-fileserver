package com.centit.fileserver.fileaccess;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.centit.fileserver.utils.FileStore;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AliyunOssStore implements FileStore {
	
	private String endPoint; 
	private String accessKeyId; 
	private String secretAccessKey;
	private String bucketName;
	
	public AliyunOssStore(){
		
	}
	
	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	private String matchFileToStoreUrl(String fileMd5, long fileSize){		
		String pathname = fileMd5.charAt(0)
					+ "/"+ fileMd5.charAt(1)
					+ "/"+ fileMd5.charAt(2);		
		return pathname +"/" + fileMd5 +"_"+fileSize+".dat";
	}

	private String matchFileToStoreUrl(String fileMd5, long fileSize,String extName){
		String pathname = fileMd5.charAt(0)
				+ "/"+ fileMd5.charAt(1)
				+ "/"+ fileMd5.charAt(2);
		return pathname +"/" + fileMd5 +"_"+fileSize+"."+extName;
	}
		
	public String saveFileByMd5(String sourFilePath, String fileMd5, long fileSize)
			throws IOException {
		String filePath =  matchFileToStoreUrl(fileMd5,fileSize);
		
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ossc.putObject(bucketName, filePath, new File(sourFilePath));		
		return filePath;
	}
	
	@Override
	public String saveFile(String sourFilePath) throws IOException {
		File file = new File(sourFilePath);
		String fileMd5 = FileMD5Maker.makeFileMD5(file);
		long fileSize = file.length();
		return saveFileByMd5(sourFilePath, fileMd5, fileSize);
	}

	@Override
	public String saveFile(InputStream is, String fileMd5, long fileSize) throws IOException {
		String fileStroeUrl =  matchFileToStoreUrl(fileMd5,fileSize);		
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ossc.putObject(bucketName, fileStroeUrl, is);		
		return fileStroeUrl;
	}

	@Override
	public String saveFile(String sourFilePath, String fileMd5, long fileSize) throws IOException {
		/*if(!FileUploadUtils.checkFileCompleted(sourFilePath, fileMd5))
			throw new IOException("文件MD5校验出错："+fileMd5);*/
		return saveFileByMd5(sourFilePath, fileMd5, fileSize);
	}

	@Override
	public String saveFile(String sourFilePath, String fileMd5, long fileSize, String extName) throws IOException {
		String filePath =  matchFileToStoreUrl(fileMd5,fileSize,extName);
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ossc.putObject(bucketName, filePath, new File(sourFilePath));
		return filePath;
	}

	@Override
	public boolean checkFile(String fileMd5, long fileSize) {
		String fileStroeUrl =  matchFileToStoreUrl(fileMd5,fileSize);
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		
		return ossc.doesObjectExist(bucketName, fileStroeUrl);
	}

	@Override
	public String getFileStoreUrl(String fileMd5, long fileSize) {
		String fileUrl = matchFileToStoreUrl(fileMd5,fileSize);
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		return ossc.doesObjectExist(bucketName, fileUrl) ? fileUrl : null;
	}

	@Override
	public String getFileStoreUrl(String fileMd5, long fileSize, String extName) {
		String fileUrl = matchFileToStoreUrl(fileMd5,fileSize,extName);
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		return ossc.doesObjectExist(bucketName, fileUrl) ? fileUrl : null;
	}

	@Override
	public long getFileSize(String fileUrl) throws IOException {
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ObjectMetadata om = ossc.getObjectMetadata(bucketName, fileUrl);
		return om.getContentLength();	
	}
	
	@Override
	public String getFileAccessUrl(String fileStoreUrl) {
		//TODO 这里应该返回一个相对文件服务器的url，
		//因为前缀可能通过反向代理有所改变所以，这个前缀应该在客户端的配置文件中设置
		return fileStoreUrl;
	}

	@Override
	public InputStream loadFileStream(String fileUrl) throws IOException {
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		OSSObject oobj = ossc.getObject(bucketName, fileUrl);
		if(oobj==null)
			return null;
		return oobj.getObjectContent();
	}

	@Override
	public InputStream loadFileStream(String fileMd5, long fileSize) throws IOException {
		return  loadFileStream(matchFileToStoreUrl(fileMd5,fileSize));
	}

	@Override
	public File getFile(String fileUrl) throws IOException {
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		OSSObject oobj = ossc.getObject(bucketName, fileUrl);
		if(oobj==null)
			return null;
		File file = new File( SystemTempFileUtils.getRandomTempFilePath());
		FileIOOpt.writeInputStreamToFile( oobj.getObjectContent(), file);		
		return file;
	}

	@Override
	public boolean deleteFile(String fileUrl) throws IOException {
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ossc.deleteObject(bucketName, fileUrl);
		return true;
	}

	@Override
	public boolean deleteFile(String fileMd5, long fileSize) throws IOException {
		String fileUrl =  matchFileToStoreUrl(fileMd5,fileSize);	
		OSSClient ossc = new OSSClient(endPoint,accessKeyId,secretAccessKey);
		ossc.deleteObject(bucketName, fileUrl);
		return true;
	}
	
	@Override
	public String getFileAccessUrl(String fileMd5, long fileSize) {
		return getFileAccessUrl(getFileStoreUrl(fileMd5,  fileSize));
	}
}
