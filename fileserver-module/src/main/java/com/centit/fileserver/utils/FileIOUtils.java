package com.centit.fileserver.utils;

import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileTaskInfo;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.pretreat.AbstractOfficeToPdf;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.service.LocalFileManager;
import com.centit.fileserver.task.CreatePdfOpt;
import com.centit.support.file.FileType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class FileIOUtils {
    private static final int URI_START_PARAM = 5;
    public static final String LOG_OPERATION_NAME = "FileServerLog";
    public static InputStream getFileStream(FileStore fileStore, FileStoreInfo fileStoreInfo) throws IOException {
        if("E".equals(fileStoreInfo.getIsTemp())) return null;
        return fileStoreInfo.isTemp() ? Files.newInputStream(new File(fileStoreInfo.getFileStorePath()).toPath()) :
            fileStore.loadFileStream(fileStoreInfo.getFileStorePath());
    }

    /**
     * 敏感(危险)文件扩展名黑名单:
     * 这些扩展名可能被 Web 服务器解析执行(webshell)或在客户端被直接执行(存储型XSS、木马),
     * 上传时将被强制追加 .rn 后缀重命名,阻止其被当作可执行文件解析
     */
    private static final Set<String> SENSITIVE_FILE_EXTS = new HashSet<>(Arrays.asList(
        // PHP webshell 及其变体
        "php", "php3", "php4", "php5", "php7", "phps", "phtml", "phtm", "pht",
        // JSP webshell 及其变体
        "jsp", "jspx", "jspf", "jspa", "jsw", "jsv",
        // ASP / ASPX webshell 及其变体
        "asp", "aspx", "asa", "asax", "ascx", "ashx", "asmx", "axd", "cer", "cdx",
        // 服务器端解析的页面 / 配置文件
        "shtml", "shtm", "stm", "cfm", "htaccess", "htpasswd",
        // 客户端脚本 / 页面,可能导致存储型XSS
        "js", "mjs", "html", "htm", "xhtml", "jse", "vbs", "vbe", "wsh", "wsf",
        // 其他脚本语言
        "py", "py3", "pyw", "rb", "pl", "pm", "cgi", "sh", "bash", "lua",
        // Windows 可执行文件
        "exe", "com", "bat", "cmd", "msi", "scr", "cpl", "ps1", "psm1", "jar",
        "dll", "reg", "lnk", "hta", "scf", "msp", "msc"
    ));

    /**
     * 判断文件名是否为敏感(危险)扩展名,是则上传时将被强制追加 .rn 后缀重命名
     * <p>防护点:
     * <ol>
     * <li>黑名单覆盖 php/jsp/asp 全家族变体(phtml、php5、jspx、aspx 等),按扩展名精确匹配</li>
     * <li>剥离文件名末尾的点和空白,防止 "shell.php."、"shell.php "(Windows 忽略尾部点/空格)绕过</li>
     * </ol>
     *
     * @param fileName 文件名
     * @return true 表示是敏感扩展名
     */
    public static boolean hasSensitiveExtName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        String name = fileName.trim();
        // 剥离末尾的点和空白,防止 "shell.php." / "shell.php " 绕过
        while (!name.isEmpty() && name.charAt(name.length() - 1) == '.') {
            name = name.substring(0, name.length() - 1).trim();
        }
        int lastDot = name.lastIndexOf('.');
        // 没有扩展名;".htaccess" 这类隐藏文件 lastDot 为 0,取点后的名字同样能正确匹配
        if (lastDot < 0) {
            return false;
        }
        String ext = name.substring(lastDot + 1).toLowerCase();
        return SENSITIVE_FILE_EXTS.contains(ext);
    }

    public static boolean reGetPdf(HttpServletRequest request, HttpServletResponse response, FileInfo fileInfo,
        FileStore fileStore, CreatePdfOpt createPdfOpt, FileInfoManager fileInfoManager,
                            FileStoreInfoManager fileStoreInfoManager) throws IOException {
        boolean canView = false;
        if (AbstractOfficeToPdf.canTransToPdf(fileInfo.getFileType())) {
            FileStoreInfo fileStoreInfo = fileStoreInfoManager.getObjectById(fileInfo.getFileMd5());
            FileTaskInfo addPdfTaskInfo = new FileTaskInfo(createPdfOpt.getOpeatorName());
            addPdfTaskInfo.setFileId(fileInfo.getFileId());
            addPdfTaskInfo.setFileSize(fileStoreInfo.getFileSize());
            createPdfOpt.doFileTask(addPdfTaskInfo);
            FileInfo newFileInfo = fileInfoManager.getObjectById(fileInfo.getFileId());
            if (StringUtils.isNotBlank(newFileInfo.getAttachedFileMd5())) {
                FileStoreInfo attachedFileStoreInfo =
                    fileStoreInfoManager.getObjectById(newFileInfo.getAttachedFileMd5());
                if (attachedFileStoreInfo != null) {
                    canView = true;
                    try(InputStream is = FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo)) {
                        UploadDownloadUtils.downFileRange(request, response, is,
                            attachedFileStoreInfo.getFileSize(),
                            FileType.truncateFileExtName(newFileInfo.getFileName())
                                + ".pdf",// + newFileInfo.getAttachedType(),
                            "inline", null);
                    }
                }
            }
        }
        return canView;
    }

    public static InputStream createPdfStream(FileInfo fileInfo,
                                                 FileStoreInfo fileStoreInfo ,
                                   FileStore fileStore, CreatePdfOpt createPdfOpt,
                                   FileStoreInfoManager fileStoreInfoManager) throws IOException {
        createPdfOpt.doPdfOpt(fileInfo, fileStoreInfo.getFileSize());
        if (StringUtils.isNotBlank(fileInfo.getAttachedFileMd5())) {
            FileStoreInfo attachedFileStoreInfo =
                fileStoreInfoManager.getObjectById(fileInfo.getAttachedFileMd5());
            return  FileIOUtils.getFileStream(fileStore, attachedFileStoreInfo);
        }
        return null;
    }
    public static ImmutableTriple<String, String, String> fetchUnitFilePath(String uri)
        throws UnsupportedEncodingException {
        String[] urips = uri.split("/");
        int n = urips.length;
        if (n < URI_START_PARAM + 2) {
            return null;
        }
        if (n == URI_START_PARAM + 2) {
            return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
                "", URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        }
        StringBuilder sb = new StringBuilder(URLDecoder.decode(urips[URI_START_PARAM + 1], "UTF-8"));
        for (int i = URI_START_PARAM + 2; i < n - 1; i++) {
            sb.append(LocalFileManager.FILE_PATH_SPLIT).append(URLDecoder.decode(urips[i], "UTF-8"));
        }
        return new ImmutableTriple<>(URLDecoder.decode(urips[URI_START_PARAM], "UTF-8"),
            sb.toString(), URLDecoder.decode(urips[n - 1], "UTF-8"));
    }
}
