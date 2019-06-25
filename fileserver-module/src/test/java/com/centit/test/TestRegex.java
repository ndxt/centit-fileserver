package com.centit.test;

import com.centit.fileserver.pretreat.FilePretreatUtils;
import com.centit.support.file.FileMD5Maker;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class TestRegex {

    static final Pattern RANGE_PATTERN = Pattern.compile("\\d+");
    //r"(?<=p[\d])"
    /**
     * 获取Range参数
     * @param args
     * @return
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        for(int i=0;i<10;i++){
            FilePretreatUtils.zipFileAndEncrypt("D:\\WorkDoc\\研发中心建设\\组织结构\\项目评估方案——草案.doc",
                "C:\\Users\\codefan\\Downloads\\项目评估方案——草案"+i+".zip","123456");

            System.out.println(FileMD5Maker.makeFileMD5(
                    new File("C:\\Users\\codefan\\Downloads\\项目评估方案——草案"+i+".zip")));

        }
        /*String range ="bytes 11-25/50";

        Matcher m = RANGE_PATTERN.matcher(range);
        //m.replaceAll(replacement)
        while (m.find()) {
            System.out.println(m.group(0));
        }
    */

    }
}
