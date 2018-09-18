package com.centit.test;

import com.centit.fileserver.fileaccess.FilePretreatment;

public class TestToPdf {


    /**
     * @param args
     */
    public static void main(String[] args) {
        FilePretreatment.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
    }

}
