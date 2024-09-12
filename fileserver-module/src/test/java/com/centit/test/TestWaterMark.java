package com.centit.test;

import com.centit.fileserver.pretreat.Watermark4Pdf;

import java.io.IOException;

public class TestWaterMark {
    public static void main(String[] args) throws IOException {

        /*String waterMark = "杨淮生 codefan 2024-9-12";
        System.out.println(waterMark.length());*/

        Watermark4Pdf.addWatermark4Pdf("/Users/codefan/Documents/temp/zhuanli.pdf",
            "/Users/codefan/Documents/temp/zhuanli-sy.pdf",
            "杨淮生 codefan 2024-9-12",
            0.4f,20,24);
        System.out.println("Done!");
    }
}
