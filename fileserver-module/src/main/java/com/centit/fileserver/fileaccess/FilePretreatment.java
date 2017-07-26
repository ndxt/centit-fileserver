package com.centit.fileserver.fileaccess;

import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.utils.FileStore;
import com.centit.framework.common.SysParametersUtils;
import com.centit.support.algorithm.ZipCompressor;
import com.centit.support.file.FileEncryptWithAes;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.image.ImageOpt;
import com.centit.support.office.OfficeToPdf;
import com.centit.support.office.Watermark4Pdf;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FilePretreatment {
	
	//private static final Logger logger = LoggerFactory.getLogger(FilePretreatment.class);
	
	//private static BlockingQueue<PretreatInfo> waitingForPretreat = new LinkedBlockingQueue<>();
	//private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

	/**
     * 异步写入日志
     */
	/* 目前先用同步的，同步发现有问题后再用异步的
	 * 
	 * static {
       executor.scheduleWithFixedDelay(
           new Runnable() {
        	   private FileStoreInfoManager fileStoreInfoManager;
        	   
               @Override
               public void run() {
            	   fileStoreInfoManager = 
                           ContextLoaderListener.getCurrentWebApplicationContext().
                           getBean("fileStoreInfoManager",  FileStoreInfoManager.class);
            	   
                   while(true){//!blockingQueue.isEmpty()) {
                       try {
                    	   PretreatInfo pretreatInfo = waitingForPretreat.take();
                    	   FileStoreInfo fileStoreInfo = 
                    			   fileStoreInfoManager.getObjectById(pretreatInfo.getFileId());
                           if(fileStoreInfo==null){
                        	   logger.error("找不到指定的文件信息 " + JSON.toJSONString(pretreatInfo) );
                        	   continue;
                           }
                           FilePretreatment.pretreatment(
                   				fileStoreInfoManager,fileStoreInfo,pretreatInfo);	
                       } catch (Exception e) {
                        
                       }
                   }
                 
               }
           }, 30, 10, TimeUnit.SECONDS);
       //默认执行时间间隔为10秒
	}*/

	/*public static void addPretreatInfo(PretreatInfo pretreatInfo) {
		try {
			waitingForPretreat.put(pretreatInfo);
		} catch (InterruptedException e) {
			logger.error(e.getMessage() + JSON.toJSONString(pretreatInfo));
		}
	}*/
	/**
	 * 将office文件转换为PDF
	 * @param inputFile
	 * @param pdfFile
	 * @return
	 */
	public static boolean office2Pdf(String inputFile, String pdfFile) {
		return OfficeToPdf.office2Pdf(inputFile, pdfFile);
	}
	/**
	 * 给PDF文件添加水印
	 * @param inputFile
	 * @param waterMarkStr
	 * @return
	 */
	public static boolean addWatermarkForPdf(String inputFile , String outputFile,  String waterMarkStr){
		return Watermark4Pdf.addWatermark4Pdf(inputFile, outputFile, waterMarkStr, 0.4f, 45f, 60f);
	}
	/**
	 * 压缩文件
	 * @param inputFile
	 * @return
	 */
	public static boolean zipFile(String inputFile, String fileName ,String zipFilePathName) {
		boolean ziped=false;
		try{
			ZipCompressor.compress(zipFilePathName, fileName, inputFile);
			ziped = true;
		}catch(RuntimeException e){
			
		}
		return ziped;
	}
	/**
	 * 压缩文件并通过密码加密
	 * @param password
	 * @return
	 */
	public static boolean zipFileAndEncrypt(String inputFilePath, String zipFilePath,String password) {
		boolean ziped=false;
		try {
			// Initiate ZipFile object with the path/name of the zip file.
			ZipFile zipFile = new ZipFile(zipFilePath);
			// Build the list of files to be added in the array list
			// Objects of type File have to be added to the ArrayList
			ArrayList<File> filesToAdd = new ArrayList<>();
			filesToAdd.add(new File(inputFilePath));
			// Initiate Zip Parameters which define various properties such
			// as compression method, etc.
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to store compression
			// Set the compression level
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			// Set the encryption flag to true
			// If this is set to false, then the rest of encryption properties are ignored
			parameters.setEncryptFiles(true);
			
			// Set the encryption method to Standard Zip Encryption
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			
			// Set password
			parameters.setPassword(password);			
			// Now add files to the zip file
			// Note: To add a single file, the method addFile can be used
			// Note: If the zip file already exists and if this zip file is a split file
			// then this method throws an exception as Zip Format Specification does not 
			// allow updating split zip files
			zipFile.addFiles(filesToAdd, parameters);
			ziped=true;
		} catch (ZipException e) {
			e.printStackTrace();
		}
		
		return ziped;
	}
	/**
	 * 加密文件：加密算法暂时不可以设定
	 * @param inputFile
	 * @param password
	 * @return
	 */
	public static boolean encryptFile(String inputFile, String diminationFileName,String password) {
		boolean encrypted=false;
		try {
			FileEncryptWithAes.encrypt(inputFile, diminationFileName, password);
			encrypted=true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encrypted;
	}
	/**
	 * 给文件添加缩略图
	 * @param filename
	 * @param thumbWidth
	 * @param thumbHeight
	 * @param quality
	 * @param outFilename
	 * @return
	 */
	public static boolean createImageThumbnail(String filename, int thumbWidth, int thumbHeight, int quality,
			String outFilename){
		boolean created=false;
		try {
			ImageOpt.createThumbnail(filename, thumbWidth, thumbHeight, quality, outFilename);
			created = true;
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		return created;
	}
	
	private static void deleteFileIfTempFile(String filePath ){
		if(filePath!=null && filePath.startsWith(SystemTempFileUtils.getRandomTempFilePath()))
			FileSystemOpt.deleteFile(filePath);
	}
	
	public static FileStoreInfo
		pretreatment( FileStoreInfo fileStoreInfo,PretreatInfo pretreatInfo) throws Exception
	{
		if(! pretreatInfo.needPretreat())
			return fileStoreInfo;
		
		FileStore fs = FileStoreFactory.createDefaultFileStore();
		String sourceFilePath = fs.getFile( fileStoreInfo.getFileStorePath()).getPath();
		
		if(pretreatInfo.getIsIndex()){
			//TODO 调用检索接口
		}
		
		if(pretreatInfo.getAddPdf()){
			String pdfTmpFile = SysParametersUtils.getTempHome()
					+ File.separatorChar + fileStoreInfo.getFileMd5()+"1.pdf";
			boolean createPdf = 
					office2Pdf(sourceFilePath , pdfTmpFile );
			if(createPdf){
				fileStoreInfo.setAttachedType("P");
				if(StringUtils.isNotBlank(pretreatInfo.getWatermark())){
					String pdfTmpFile2 = SysParametersUtils.getTempHome()
							+ File.separatorChar + fileStoreInfo.getFileMd5()+"2.pdf";
					if( addWatermarkForPdf(pdfTmpFile , pdfTmpFile2, pretreatInfo.getWatermark())){
						fileStoreInfo.setAttachedStorePath(fs.saveFile(pdfTmpFile2));
					}else
						throw new Exception("给PDF添加水印出错！"+ fileStoreInfo.getFileMd5());
				}else
					fileStoreInfo.setAttachedStorePath(fs.saveFile(pdfTmpFile));
			}else
				throw new Exception("生产PDF文件出错！"+ fileStoreInfo.getFileMd5());
		}
		
		if(pretreatInfo.getAddThumbnail()){
			String outFilename = SysParametersUtils.getTempHome()
					+ File.separatorChar + fileStoreInfo.getFileMd5()+"1.jpf";
			if(createImageThumbnail(sourceFilePath,
					pretreatInfo.getThumbnailWidth(), pretreatInfo.getThumbnailHeight(), 100,
					outFilename)){
				fileStoreInfo.setAttachedType("T");
				fileStoreInfo.setAttachedStorePath(fs.saveFile(outFilename));
			}else
				throw new Exception("生产缩略图出错！"+ fileStoreInfo.getFileMd5());
		}
		//String oldFileStorePath = fileStoreInfo.getFileStorePath();
		if(!"N".equals(pretreatInfo.getEncryptType())){
			
			String outFilename = SysParametersUtils.getTempHome()
					+ File.separatorChar + fileStoreInfo.getFileMd5()+"1.ent";
			
			if("D".equals(pretreatInfo.getEncryptType())){				
				if(StringUtils.isBlank(pretreatInfo.getEncryptPassword()))
					throw new Exception("设置DES加密时请同时设置密码！"+ fileStoreInfo.getFileMd5());
				if(encryptFile(sourceFilePath,
						outFilename,pretreatInfo.getEncryptPassword())){
					
					File file = new File(outFilename);
					String fileMd5 = FileMD5Maker.makeFileMD5(file);
					long fileSize = file.length();					
					fileStoreInfo.setFileStorePath(
							fs.saveFile(outFilename,fileMd5,fileSize));	
					fileStoreInfo.setFileMd5(fileMd5);
					fileStoreInfo.setFileSize(fileSize);
					fileStoreInfo.setEncryptType("D");
					//fs.deleteFile(oldFileStorePath);
				}else
					throw new Exception("DES加密文件时出错！"+ fileStoreInfo.getFileMd5());
			}else if("Z".equals(pretreatInfo.getEncryptType())){
				if(StringUtils.isBlank(pretreatInfo.getEncryptPassword())){
					
					 if(zipFile(sourceFilePath, fileStoreInfo.getFileName(), outFilename)){
						File file = new File(outFilename);
						String fileMd5 = FileMD5Maker.makeFileMD5(file);
						long fileSize = file.length();					
						fileStoreInfo.setFileStorePath(
								fs.saveFile(outFilename,fileMd5,fileSize));	
						fileStoreInfo.setFileMd5(fileMd5);
						fileStoreInfo.setFileSize(fileSize);						
						fileStoreInfo.setEncryptType("Z");
						fileStoreInfo.setFileName(
								FileType.truncateFileExtName(fileStoreInfo.getFileName())
								+".zip");
						fileStoreInfo.setFileType("zip");
						//fs.deleteFile(oldFileStorePath);
					 }else
						 throw new Exception("Zip压缩文件时出错！"+ fileStoreInfo.getFileMd5());
				}else{
					String entFileDir = SysParametersUtils.getTempHome()
							+ File.separatorChar + fileStoreInfo.getFileMd5();
					FileSystemOpt.createDirect(entFileDir);
					String entFilePath = entFileDir + File.separatorChar + fileStoreInfo.getFileName();
					
					FileSystemOpt.fileCopy(sourceFilePath, entFilePath);
					
					if(zipFileAndEncrypt(entFilePath,outFilename,
							 pretreatInfo.getEncryptPassword())){
						File file = new File(outFilename);
						String fileMd5 = FileMD5Maker.makeFileMD5(file);
						long fileSize = file.length();					
						fileStoreInfo.setFileStorePath(
								fs.saveFile(outFilename,fileMd5,fileSize));	
						fileStoreInfo.setFileMd5(fileMd5);
						fileStoreInfo.setFileSize(fileSize);						
						fileStoreInfo.setEncryptType("Z");
						fileStoreInfo.setFileName(
								FileType.truncateFileExtName(fileStoreInfo.getFileName())
								+".zip");
						fileStoreInfo.setFileType("zip");
						//删除临时文件
						FileSystemOpt.deleteFile(entFilePath);
						FileSystemOpt.deleteDirect(entFileDir);						
						//fs.deleteFile(oldFileStorePath);
					}else{
						FileSystemOpt.deleteFile(entFilePath);
						FileSystemOpt.deleteDirect(entFileDir);
						throw new Exception("zipFileAndEncrypt 压缩文件时出错！"+ fileStoreInfo.getFileMd5());
					}
				}
			}
			//删除临时文件
			FileSystemOpt.deleteFile(outFilename);
		}
		deleteFileIfTempFile(sourceFilePath);
		return fileStoreInfo;
	}

}
