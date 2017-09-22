package com.centit.fileserver.fileaccess;

import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.common.SysParametersUtils;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.utils.TikaTextExtractor;
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
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FilePretreatment {
	
	private static final Logger logger = LoggerFactory.getLogger(FilePretreatment.class);
	
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
                        	logger.error(e.getMessage(), e);
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
	 * @param inputFile office文件
	 * @param pdfFile PDF文件
	 * @return 布尔值
	 */
	public static boolean office2Pdf(String inputFile, String pdfFile) {
		return OfficeToPdf.office2Pdf(inputFile, pdfFile);
	}

	public static boolean office2Pdf(String suffix, String inputFile, String pdfFile) {
		return OfficeToPdf.office2Pdf(suffix, inputFile, pdfFile);
	}

	/**
	 *  * 给PDF文件添加水印
	 * @param inputFile 处理前的文件
	 * @param outputFile 处理后的文件
	 * @param waterMarkStr 水印
	 * @return 布尔值
	 */
	public static boolean addWatermarkForPdf(String inputFile , String outputFile,  String waterMarkStr){
		return Watermark4Pdf.addWatermark4Pdf(inputFile, outputFile, waterMarkStr, 0.4f, 45f, 60f);
	}

	/**
	 * 压缩文件
	 * @param inputFile 处理前的文件
	 * @param fileName 文件名
	 * @param zipFilePathName zip文件路径
	 * @return 布尔值
	 */
	public static boolean zipFile(String inputFile, String fileName ,String zipFilePathName) {
		boolean ziped=false;
		try{
			ZipCompressor.compress(zipFilePathName, fileName, inputFile);
			ziped = true;
		}catch(RuntimeException e){
			logger.error(e.getMessage(), e);
		}
		return ziped;
	}

	/**
	 * 压缩文件并通过密码加密
	 * @param inputFilePath 处理前的文件
	 * @param zipFilePath zip文件路径
	 * @param password 密码
	 * @return 布尔值
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
			logger.error(e.getMessage(), e);
		}
		
		return ziped;
	}

	/**
	 * 加密文件：加密算法暂时不可以设定
	 * @param inputFile 处理前文件
	 * @param diminationFileName diminationFileName
	 * @param password 密码
	 * @return 布尔值
	 */
	public static boolean encryptFile(String inputFile, String diminationFileName,String password) {
		boolean encrypted=false;
		try {
			FileEncryptWithAes.encrypt(inputFile, diminationFileName, password);
			encrypted=true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return encrypted;
	}
	/**
	 * 给文件添加缩略图
	 * @param filename 文件名
	 * @param thumbWidth 宽度
	 * @param thumbHeight 高度
	 * @param quality quality
	 * @param outFilename 处理后文件名
	 * @return 布尔值
	 */
	public static boolean createImageThumbnail(String filename, int thumbWidth, int thumbHeight, int quality,
			String outFilename){
		boolean created=false;
		try {
			ImageOpt.createThumbnail(filename, thumbWidth, thumbHeight, quality, outFilename);
			created = true;
		} catch (InterruptedException | IOException e) {
			logger.error(e.getMessage(), e);
		}
		return created;
	}
	
	private static void deleteFileIfTempFile(String filePath ){
		if(filePath!=null && filePath.startsWith(SystemTempFileUtils.getRandomTempFilePath()))
			FileSystemOpt.deleteFile(filePath);
	}
	
	public static FileStoreInfo pretreatment(FileStore fs, FileStoreInfo fileStoreInfo,PretreatInfo pretreatInfo)
	throws IOException{
		if(! pretreatInfo.needPretreat())
			return fileStoreInfo;
		
		//FileStore fs = FileStoreFactory.createDefaultFileStore();
		String sourceFilePath = fs.getFile( fileStoreInfo.getFileStorePath()).getPath();

		if(pretreatInfo.getIsIndex()){

			Indexer indexer = IndexerSearcherFactory.obtainIndexer(
				IndexerSearcherFactory.loadESServerConfigFormProperties(
					SysParametersUtils.loadProperties()), FileDocument.class) ;
			FileDocument fileDoc = new FileDocument();
			fileDoc.setFileId(fileStoreInfo.getFileId() );
			fileDoc.setOsId( fileStoreInfo.getOsId());
			fileDoc.setOptId( fileStoreInfo.getOptId());
			fileDoc.setOptMethod( fileStoreInfo.getOptMethod());
			fileDoc.setOptTag( fileStoreInfo.getOptTag());
			fileDoc.setFileMD5( fileStoreInfo.getFileMd5());
			fileDoc.setFileName( fileStoreInfo.getFileName());
			fileDoc.setFileSummary( fileStoreInfo.getFileDesc());
			fileDoc.setOptUrl( fileStoreInfo.getFileShowPath());
			fileDoc.setUserCode( fileStoreInfo.getFileOwner());
			fileDoc.setUnitCode(fileStoreInfo.getFileUnit());
			//获取文件的文本信息
			try {
				fileDoc.setContent(TikaTextExtractor.extractInputStreamText(
						fs.loadFileStream(fileStoreInfo.getFileMd5(), fileStoreInfo.getFileSize())));
			}catch (TikaException te){
				logger.error(te.getMessage(), te);
			}catch (SAXException se){
				logger.error(se.getMessage(), se);
			}
			fileDoc.setCreateTime(fileStoreInfo.getCreateTime() );
			indexer.saveNewDocument(fileDoc);
			fileStoreInfo.setIndexState("I");
		}
		
		if(pretreatInfo.getAddPdf()){
			String pdfTmpFile = SysParametersUtils.getTempHome()
					+ File.separatorChar + fileStoreInfo.getFileMd5()+"1.pdf";
			boolean createPdf =
					office2Pdf(fileStoreInfo.getFileType(),sourceFilePath , pdfTmpFile );

			if(createPdf){
				fileStoreInfo.setAttachedType("P");
				if(StringUtils.isBlank(pretreatInfo.getWatermark())){
					String pdfTmpFile2 = SysParametersUtils.getTempHome()
							+ File.separatorChar + fileStoreInfo.getFileMd5()+"2.pdf";
					if( addWatermarkForPdf(pdfTmpFile , pdfTmpFile2, pretreatInfo.getWatermark())){
						fileStoreInfo.setAttachedStorePath(fs.saveFile(pdfTmpFile2));
					}else
						logger.error("给PDF添加水印出错！"+ fileStoreInfo.getFileMd5());
				}else
					fileStoreInfo.setAttachedStorePath(fs.saveFile(pdfTmpFile));
			}else
				logger.error("生产PDF文件出错！"+ fileStoreInfo.getFileMd5());
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
				logger.error("生产缩略图出错！"+ fileStoreInfo.getFileMd5());
		}
		//String oldFileStorePath = fileStoreInfo.getFileStorePath();
		if(!"N".equals(pretreatInfo.getEncryptType())){
			
			String outFilename = SysParametersUtils.getTempHome()
					+ File.separatorChar + fileStoreInfo.getFileMd5()+"1.ent";
			
			if("D".equals(pretreatInfo.getEncryptType())){				
				if(StringUtils.isBlank(pretreatInfo.getEncryptPassword()))
					logger.error("设置DES加密时请同时设置密码！"+ fileStoreInfo.getFileMd5());
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
					logger.error("DES加密文件时出错！"+ fileStoreInfo.getFileMd5());
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
						 logger.error("Zip压缩文件时出错！"+ fileStoreInfo.getFileMd5());
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
						logger.error("zipFileAndEncrypt 压缩文件时出错！"+ fileStoreInfo.getFileMd5());
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
