package com.centit.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.centit.fileserver.utils.FileRangeInfo;
import com.centit.fileserver.utils.FileServerConstant;
import com.centit.fileserver.utils.FileStore;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseData;
import com.centit.framework.core.controller.BaseController;
import com.centit.support.file.FileIOOpt;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 仅仅用于保存文件，可以用于没有权限要求的文件存储
 * 下载也没有权限限制
 */
@Controller
@RequestMapping("/store")
public class StoreFileController extends BaseController {

	private Logger logger = LoggerFactory.getLogger(StoreFileController.class);

	@Resource
	protected FileStore fileStore;
	/**
	 * 判断文件是否存在，如果文件已经存在可以实现秒传
	 * @param token token
	 * @param size 大小
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 */
	@CrossOrigin(origins = "*",allowCredentials="true",maxAge=86400,
			allowedHeaders="*", methods=RequestMethod.GET) 
	@RequestMapping(value="/exists", method = RequestMethod.GET)
	public void checkFileExists(String  token,long size ,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		JsonResultUtils.writeOriginalObject(fileStore.checkFile(token, size), response);
	}	
	
	/**
	 * 获取文件 断点位置，前端根据断点位置续传
	 * @param token token
	 * @param size 大小
	 * @param request HttpServletRequest     
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 */
	@CrossOrigin(origins = "*",allowCredentials="true",maxAge=86400,methods=RequestMethod.GET)
	@RequestMapping(value="/range", method = { RequestMethod.GET })
	public void checkFileRange(String token, long size,
                HttpServletRequest request, HttpServletResponse response)
			throws IOException {		
		//FileRangeInfo fr = new FileRangeInfo(token,size);
        long tempFileSize = 0;
		// 如果文件已经存在则完成秒传，无需再传
		if (fileStore.checkFile(token, size)) {//如果文件已经存在 系统实现秒传
			//添加完成 后 相关的处理  类似与 uploadRange
            tempFileSize  = size;
		}else {
            //检查临时目录中的文件大小，返回文件的其实点
            //String tempFilePath = FileUploadUtils.getTempFilePath(token, size);
            tempFileSize = SystemTempFileUtils.checkTempFileSize(
					SystemTempFileUtils.getTempFilePath(token, size));
        }
		JSONObject json = new JSONObject();
		json.put("start", tempFileSize);
		json.put(ResponseData.RES_CODE_FILED, 0 );
		json.put(ResponseData.RES_MSG_FILED,  "上传文件片段成功" );
		json.put(ResponseData.RES_DATA_FILED, tempFileSize);
		JsonResultUtils.writeOriginalJson(json.toString(), response);		
	}


	private void completedFileStore(String fileMd5, long size, HttpServletResponse response) {
			// 返回响应
			JSONObject json = new JSONObject();
			json.put("start", size);
			json.put("token", fileMd5);
			json.put("filesize", size);
			json.put("success", true);
			json.put(ResponseData.RES_CODE_FILED, 0 );
			json.put(ResponseData.RES_MSG_FILED,  "上传成功" );
			JsonResultUtils.writeOriginalJson(json.toString(), response);
	}


	private InputStream	fetchInputStreamFromRequest(HttpServletRequest request) throws IOException{
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if(!isMultipart)
			return request.getInputStream();

		MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
//		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> map = multiRequest.getFileMap();

		for (Map.Entry<String, MultipartFile> entry : map.entrySet())  {

			CommonsMultipartFile cMultipartFile = (CommonsMultipartFile) entry.getValue();
			FileItem fi = cMultipartFile.getFileItem();
			if (!fi.isFormField()){
				return fi.getInputStream();
			}
		}
		return null;
	}
	/**
	 * 完成秒传，如果文件不存在会返回失败
	 * @param token token
	 * @param size 大小
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 */
	@CrossOrigin(origins = "*",allowCredentials="true",maxAge=86400, methods = RequestMethod.POST)
	@RequestMapping(value="/secondpass", method = RequestMethod.POST)
	public void secondPass(String  token,long size ,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

        request.setCharacterEncoding("utf8");
		
		if(fileStore.checkFile(token, size)){// 如果文件已经存在则完成秒传，无需再传。
			completedFileStore(token,size,  response);
			return;
		}else{
			String  tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);
			long tempFileSize = SystemTempFileUtils.checkTempFileSize(tempFilePath);
			if(tempFileSize == size) {
				fileStore.saveFile(tempFilePath, token, size);
				completedFileStore(token,size,  response);
				return;
			}
		}
		JsonResultUtils.writeAjaxErrorMessage(
					FileServerConstant.ERROR_FILE_NOT_EXIST,
					"文件不存在无法实现秒传，MD5："+token, response);
	}

	/**
	 *  续传文件（range） 如果文件已经传输完成 对文件进行保存
	 * @param token token
	 * @param size 大小
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException
	 */
	@CrossOrigin(origins = "*",allowCredentials="true",maxAge=86400, methods = RequestMethod.POST) 
	@RequestMapping(value="/range", method = { RequestMethod.POST })
	public void uploadRange(
			String  token,long size ,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, NoSuchAlgorithmException {


		//TODO 添加权限验证 : OSID + Token
		if(fileStore.checkFile(token, size)){// 如果文件已经存在则完成秒传，无需再传。
			completedFileStore(token,size,  response);
			return;
		}
		

		FileRangeInfo range = FileRangeInfo.parseRange(request);
		String tempFilePath = SystemTempFileUtils.getTempFilePath(token, size);
		long tempFileSize= SystemTempFileUtils.checkTempFileSize(tempFilePath);
		if(tempFileSize < size) {//文件还没有传输完成

            // 必须要抛出异常或者返回非200响应前台才能捕捉
            if (tempFileSize != range.getRangeStart()) {
                JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_RANGE_START,
                        "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。", response);
                return;
            }
			InputStream	fis = fetchInputStreamFromRequest(request);

            // 必须要抛出异常或者返回非200响应前台才能捕捉
            try (FileOutputStream out = new FileOutputStream(
                    new File(tempFilePath), true)) {
                int length = FileIOOpt.writeInputStreamToOutputStream(fis, out);
                if (length != range.getPartSize()) {
                    JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_RANGE_START,
                            "Code: " + FileServerConstant.ERROR_FILE_RANGE_START + " RANGE格式错误或者越界。", response);
                    return;
                }
            }
            tempFileSize = range.getRangeStart() + range.getPartSize();
        }
		//range.setRangeStart(rangeStart);
		if(tempFileSize == size){
			//判断是否传输完成
			fileStore.saveFile(tempFilePath,token, size);
			String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));
			if(StringUtils.equals(fileMd5,token)) {
				completedFileStore(token,size,  response);
			}else{
				JsonResultUtils.writeAjaxErrorMessage(FileServerConstant.ERROR_FILE_MD5_ERROR,
						"Code: " + FileServerConstant.ERROR_FILE_MD5_ERROR+" 文件MD5计算错误。", response);
			}
			FileSystemOpt.deleteFile(tempFilePath);
			return;
		}

		JSONObject json = new JSONObject();
		json.put("start", tempFileSize);
 		json.put(ResponseData.RES_CODE_FILED, 0 );
		json.put(ResponseData.RES_MSG_FILED,  "上传文件片段成功" );
		JsonResultUtils.writeOriginalJson(json.toString(), response);
	}
	
	/**
	 * 上传整个文件适用于IE8
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException IOException
	 */
	@CrossOrigin(origins = "*",allowCredentials="true",maxAge=86400, methods = RequestMethod.POST) 
	@RequestMapping(value="/file", method = RequestMethod.POST)
	public void uploadFile(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
        request.setCharacterEncoding("utf8");
		InputStream	fis = fetchInputStreamFromRequest(request);

		String tempFilePath = SystemTempFileUtils.getRandomTempFilePath();
        try{
            int fileSize = FileIOOpt.writeInputStreamToFile(fis, tempFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(new File(tempFilePath));

			fileStore.saveFile(tempFilePath);
			completedFileStore(fileMd5,fileSize,  response);
            FileSystemOpt.deleteFile(tempFilePath);
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
            JsonResultUtils.writeErrorMessageJson(e.getMessage(), response);
        }
    }
}