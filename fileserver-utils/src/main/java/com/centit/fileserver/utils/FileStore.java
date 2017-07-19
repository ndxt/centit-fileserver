package com.centit.fileserver.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件的物理存储接口
 * 
 */
public interface FileStore {
	
	/**
	 * 保存文件
	 * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
	 * @return
	 */
	String/*文件存储路径 */saveFile(String sourFilePath)
			throws IOException;
	/**
	 * 保存文件
	 * @param is
	 * @param fileMd5
	 * @param fileSize
	 * @return
	 */
	String/*文件存储路径 */saveFile(InputStream is, String fileMd5, long fileSize)
			throws IOException;
	
	/**
	 * 保存文件
	 * @param sourFilePath 临时文件路径，这个应该是操作系统的路径
	 * @param fileMd5
	 * @param fileSize
	 * @return
	 */
	String/*文件存储路径 */saveFile(String sourFilePath, String fileMd5, long fileSize)
			throws IOException;
	
	/**
	 * 检查文件是否存在，如果存在则实现秒传
	 * @param fileMd5
	 * @param fileSize
	 * @return true 文件存在 false 文件不存在
	 */
	boolean checkFile(String fileMd5, long fileSize);
	
	/**
	 * 获取文件的存储路径 url，通过这个路径 fileStroe可以获得这个文件
	 * 如果不存在返回null checkFile返回为true则这个肯定存在
	 * @param fileMd5
	 * @param fileSize
	 * @return
	 */
	String getFileStoreUrl(String fileMd5, long fileSize);
	
	/**
	 * 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
	 * @return
	 */
	String getFileAccessUrl(String fileStoreUrl);
	
	/**
	 * 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
	 * @param fileMd5
	 * @param fileSize
	 * @return
	 */
	String getFileAccessUrl(String fileMd5, long fileSize);
	
	/**
	 * 获取文件的Access url，如果没有权限限制可以通过这个url 直接访问文件
	 * @return
	 */
	long getFileSize(String fileUrl) throws IOException;
	/**
	 * 获取文件
	 * @param fileUrl saveFile 返回的文件路径
	 * @return
	 */
	InputStream loadFileStream(String fileUrl) throws IOException;
	
	/**
	 * 
	 * @param fileUrl
	 * @return
	 * @throws IOException
	 */
	File getFile(String fileUrl) throws IOException;
	/**
	 * 删除文件 
	 * @return true 删除成功 或者文件本来就不存在  false
	 */
	boolean deleteFile(String fileUrl) throws IOException;
	
	/**
	 * 删除文件
	 * @param fileMd5
	 * @param fileSize
	 * @return
	 * @throws IOException
	 */
	boolean deleteFile(String fileMd5, long fileSize) throws IOException;
}
