package com.centit.fileserver.utils;

public abstract class FileServerConstant {
	/** 200~415 为保留代码
	 * 	400 - Bad Request
		401 - Unauthorized
		402 - Payment Required
		403 - Forbidden
		404 - Not Found
		405 - Bad Request
		406 - Not Acceptable
		407 - Proxy Authentication Required
		408 - Request Timed-Out
		409 - Conflict
		410 - Gone
		411 - Length Required
		412 - Precondition Failed
		413 - Request Entity Too Large
		414 - Request, URI Too Large
		415 - Unsupported Media Type
	 */
	/** the file has delete for break point upload */
	public static final int ERROR_FILE_FORBIDDEN = 403;
	public static final int ERROR_FILE_NOT_EXIST = 404;
	
	/** args error: the data block start position error */
	public static final int ERROR_FILE_RANGE_START = 420;
	public static final int ERROR_FILE_RANGE_ERROR = 421;
	public static final int ERROR_FILE_PRETREAT = 422;
	public static final int ERROR_FILE_ENCRYPT = 423;
	public static final int ERROR_FILE_MD5_ERROR = 425;

}
