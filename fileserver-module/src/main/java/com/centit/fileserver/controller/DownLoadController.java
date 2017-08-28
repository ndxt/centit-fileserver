package com.centit.fileserver.controller;

import com.centit.fileserver.fileaccess.FileStoreFactory;
import com.centit.fileserver.fileaccess.SystemTempFileUtils;
import com.centit.fileserver.po.FileAccessLog;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileAccessLogManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.UploadDownloadUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.file.FileEncryptWithAes;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/download")
public class DownLoadController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(DownLoadController.class);

	@Resource
	private FileStoreInfoManager fileStoreInfoManager;
	@Resource
	private FileAccessLogManager fileAccessLogManager;

	private static String encodeFilename(String paramName) {
		String downloadChineseFileName = "";
		try {
			downloadChineseFileName = new String(
					HtmlUtils.htmlUnescape(paramName).getBytes("GBK"), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return downloadChineseFileName;
	}


	private static void downFileRange(HttpServletRequest request, HttpServletResponse response,
			InputStream inputStream,long fSize, String fileName)
			throws IOException {
		 UploadDownloadUtils.downFileRange(request, response,
				 inputStream, fSize, encodeFilename(fileName));
	}

	public static void downloadFile(FileStoreInfo stroeInfo, HttpServletRequest request,
							 HttpServletResponse response) throws IOException {
		if (null != stroeInfo) {
			FileStore fs = FileStoreFactory.createDefaultFileStore();
			//对加密的进行特殊处理，ZIP加密的无需处理
			String password = request.getParameter("password");
			if("D".equals(stroeInfo.getEncryptType()) && StringUtils.isNotBlank(password) ){
				String tmpFilePath = SystemTempFileUtils.getTempFilePath(stroeInfo.getFileMd5(),stroeInfo.getFileSize() );
				File tmpFile = new File(tmpFilePath);
				try(InputStream downFile = fs.loadFileStream(stroeInfo.getFileStorePath());
					OutputStream diminationFile = new FileOutputStream(tmpFile)	){
					FileEncryptWithAes.decrypt(downFile, diminationFile, password);
				}catch (Exception e) {
					logger.error(e.getMessage(), e);
					JsonResultUtils.writeAjaxErrorMessage(
							FileServerConstant.ERROR_FILE_ENCRYPT,
							"解码文件失败："+e.getMessage(),
							response);
					return;
				}
				try(InputStream inputStream = new FileInputStream(tmpFile)){
					downFileRange(request, response,
							inputStream,tmpFile.length(), stroeInfo.getFileName());
				}

				FileSystemOpt.deleteFile(tmpFile);
			}else{
				downFileRange(request, response,
						fs.loadFileStream(stroeInfo.getFileStorePath()),
						stroeInfo.getFileSize(), stroeInfo.getFileName());
			}
		} else {
			JsonResultUtils.writeAjaxErrorMessage(
					FileServerConstant.ERROR_FILE_NOT_EXIST, "找不到该文件", response);
		}
	}


	/**
	 * 根据文件的id下载附属文件
	 * 这个需要权限 控制 用于内部服务之间文件传输
	 * @param fileId
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value= "/pattach/{fileId}",method=RequestMethod.GET)
	public void downloadAttach(@PathVariable("fileId") String fileId,  HttpServletRequest request,
							   HttpServletResponse response) throws IOException {

		FileStoreInfo stroeInfo = fileStoreInfoManager.getObjectById(fileId);

		if (null != stroeInfo) {
			FileStore fs = FileStoreFactory.createDefaultFileStore();
			String at = stroeInfo.getAttachedType();
			if("N".equals(at)){
				JsonResultUtils.writeAjaxErrorMessage(
						FileServerConstant.ERROR_FILE_NOT_EXIST, "该文件没有附属文件", response);
				return ;
			}
			String fileName = stroeInfo.getFileName();
			if("P".equals(at)){
				fileName = fileName+".pdf" ;
			}

			downFileRange(request, response,
					fs.loadFileStream(stroeInfo.getAttachedStorePath()),
					fs.getFileSize(stroeInfo.getAttachedStorePath()),fileName );
		} else {
			JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
					"找不到该文件", response);
		}
	}
	// 文件目录 = 配置目录 + file.getFileStorePath()
	/**
	 * 根据文件的id下载文件
	 * 这个需要权限 控制 用于内部服务之间文件传输
	 * @param fileId
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value= "/pfile/{fileId}", method=RequestMethod.GET)
	public void downloadByFileId(@PathVariable("fileId") String fileId, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		FileStoreInfo stroeInfo = fileStoreInfoManager.getObjectById(fileId);

		downloadFile(stroeInfo,request,response);
	}

	/**
	 * 根据文件的 access_token 下载文件
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value= "/file/{token}", method=RequestMethod.GET)
	public void downloadByAccessToken(
			@PathVariable("token") String token, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// 根据访问日志的id和授权的token查看是否已经被授权
		FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
		if(fileAccessLog!=null){
			if(fileAccessLog.checkValid(false)){
				FileStoreInfo stroeInfo = fileStoreInfoManager.getObjectById(fileAccessLog.getFileId());
				downloadFile(stroeInfo, request ,response);
				// 记录访问日志
				fileAccessLog.chargeAccessTimes();
				fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
				fileAccessLog.setLastAccessHost(request.getLocalAddr());
				fileAccessLogManager.updateObject(fileAccessLog);
			}else{
				JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
						"没有权限访问该文件或者访问授权已过期！", response);
			}
		}else{
			JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
					"找不到该文件或者您没有权限访问该文件！", response);
		}
	}

	/**
	 * 根据access_token下载附属文件
	 * @param token
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value= "/attach/{token}", method=RequestMethod.GET)
	public void downloadAttachByAccessToken(
			@PathVariable("token") String token, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// 根据访问日志的id和授权的token查看是否已经被授权
		FileAccessLog fileAccessLog = fileAccessLogManager.getObjectById(token);
		// 判断权限
		if(fileAccessLog!=null){
			if(fileAccessLog.checkValid(true)){
				downloadAttach( fileAccessLog.getFileId(), request ,response);
				// 记录访问日志
				fileAccessLog.chargeAccessTimes();
				fileAccessLog.setLastAccessTime(DatetimeOpt.currentUtilDate());
				fileAccessLog.setLastAccessHost(request.getLocalAddr());
				fileAccessLogManager.updateObject(fileAccessLog);
			}else{
				JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_FORBIDDEN,
						"没有权限访问该文件或者访问授权已过期！", response);
			}
		}else{
			JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_NOT_EXIST,
					"找不到该文件或者您没有权限访问该文件！", response);
		}
	}

	/**
	 * 根据文件的 MD5码 下载不受保护的文件，不需要访问文件记录
	 * 如果是通过 store 上传的需要指定 extName 扩展名
	 * @param md5SizeExt 文件的Md5码和文件的大小 格式为 MD5_SIZE.EXT
	 * @param fileName 文件的名称包括扩展名，如果这个不为空， 上面的 md5SizeExt 可以没有 .Ext 扩展名
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value= "/unprotected/{md5SizeExt}", method=RequestMethod.GET)
	public void downloadUnprotectedFile(@PathVariable("md5SizeExt") String md5SizeExt,
								 String fileName,
								 HttpServletRequest request,
								 HttpServletResponse response) throws IOException {
		//FileStoreInfo stroeInfo = fileStoreInfoManager.getObjectById(md5);
		//downloadFile(stroeInfo,request,response);
		String uri = request.getRequestURI();
		String [] urips = uri.split("/");
		int n=urips.length;
		if(StringUtils.isBlank(fileName)){
			fileName = urips[n-1];
		}
		String fileMd5 =  md5SizeExt.substring(0,32);
		int pos = md5SizeExt.indexOf('.');
		//String extName = md5SizeExt.substring(pos);
		long fileSize = pos<0?NumberBaseOpt.parseLong(md5SizeExt.substring(33),0l)
							:NumberBaseOpt.parseLong(md5SizeExt.substring(33,pos),0l);
		FileStore fs = FileStoreFactory.createDefaultFileStore();
		String filePath = fs.getFileStoreUrl(fileMd5, fileSize);
		InputStream inputStream = fs.loadFileStream(filePath);
		downFileRange(request,  response,
				inputStream, fileSize,
				fileName);
	}
}