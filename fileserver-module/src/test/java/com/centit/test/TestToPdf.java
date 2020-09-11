package com.centit.test;


import com.centit.fileserver.pretreat.AbstractOfficeToPdf;

public class TestToPdf {


    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

//        FilePretreatUtils.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
//        Watermark4Pdf.addWatermark4Pdf("D:\\test.pdf", "D:\\out.pdf", "success", 0.4f, 45f, 60f);
//        try {
//
////            System.out.println(FileMD5Maker.makeFileMD5(new File("D:\\D\\Projects\\RunData\\file_home\\temp\\35821f7d75bda6b08420ac5e9672069a1.pdf")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        AbstractOfficeToPdf.excel2Pdf("C:\\Users\\zhf\\Postman\\files\\1.xls","C:\\Users\\zhf\\Postman\\files\\3.html");
        AbstractOfficeToPdf.word2Pdf("d:\\test3.docx","d:\\5.pdf","docx");
//        AbstractOfficeToPdf.ppt2Pdf("C:\\Users\\zhf\\Postman\\files\\1.pptx","C:\\Users\\zhf\\Postman\\files\\6.html");
//        String pathOfXls = "C:\\Users\\zhf\\Postman\\files\\2.xlsx";
//        String pathOfPdf = "C:\\Users\\zhf\\Postman\\files\\2.pdf";
//
//        FileInputStream fis = new FileInputStream(pathOfXls);
//        List<ExcelObject> objects = new ArrayList<ExcelObject>();
//        objects.add(new ExcelObject("导航1",fis));
//        FileOutputStream fos = new FileOutputStream(pathOfPdf);
//        Excel2Pdf pdf = new Excel2Pdf(objects, fos);
//        pdf.convert();
//        System.out.println(
//            TikaTextExtractor.extractFileText("/D/Projects/RunData/file_home/temp\\db06f45a30226a56b59930ac52732e8d_13512.tmp"));

    }


}
