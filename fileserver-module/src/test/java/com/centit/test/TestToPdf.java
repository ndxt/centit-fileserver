package com.centit.test;

import com.centit.fileserver.fileaccess.FilePretreatUtils;
import com.centit.fileserver.po.FileInfo;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.office.Watermark4Pdf;

import java.io.File;
import java.io.IOException;

public class TestToPdf {


    /**
     * @param args
     */
    public static void main(String[] args) {
//        FilePretreatUtils.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
//        Watermark4Pdf.addWatermark4Pdf("D:\\test.pdf", "D:\\out.pdf", "success", 0.4f, 45f, 60f);
        try {
            System.out.println(FileMD5Maker.makeFileMD5(new File("D:\\D\\Projects\\RunData\\file_home\\temp\\35821f7d75bda6b08420ac5e9672069a1.pdf")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
