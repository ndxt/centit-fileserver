package com.centit.test;

import com.centit.fileserver.utils.FileIOUtils;

/**
 * 验证 hasSensitiveExtName 的敏感扩展名拦截
 */
public class TestSensitiveExtName {

    public static void main(String[] args) {
        // 应被拦截:webshell 及其变体
        String[] dangerFiles = {
            "shell.php", "shell.php3", "shell.php4", "shell.php5", "shell.php7",
            "shell.phtml", "shell.pht", "shell.phps",
            "shell.jsp", "shell.jspx", "shell.jspf", "shell.jspa",
            "shell.asp", "shell.aspx", "shell.asa", "shell.ashx", "shell.cer",
            ".htaccess", ".htpasswd",
            // 绕过手法:尾部点、尾部空格、大小写混用
            "shell.php.", "shell.php ", "shell.PHP5", "shell.Phtml ", "  shell.php5.  ",
            "x.html", "x.exe", "x.bat", "x.ps1", "x.cmd", "x.jar", "x.vbs", "x.sh"
        };
        // 正常文件,不应误伤
        String[] normalFiles = {
            "报告.docx", "照片.jpg", "数据.xlsx", "手册.pdf", "logo.svg",
            "index.tar.gz", "备份.sql", "readme", "README.", "file.rn", "文档.pdf.rn"
        };

        boolean allPass = true;
        for (String f : dangerFiles) {
            boolean r = FileIOUtils.hasSensitiveExtName(f);
            if (!r) {
                allPass = false;
                System.out.println("[漏报] " + f);
            }
        }
        for (String f : normalFiles) {
            boolean r = FileIOUtils.hasSensitiveExtName(f);
            if (r) {
                allPass = false;
                System.out.println("[误伤] " + f);
            }
        }
        System.out.println(allPass ? "全部通过 ✓" : "存在失败用例 ✗");
    }
}
