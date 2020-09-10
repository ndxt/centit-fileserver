package com.centit.fileserver.pretreat;

import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.report.ExcelTypeEnum;
import com.centit.support.report.WordReportUtil;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.AbstractWordUtils;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * @author zhf
 */
public abstract class AbstractOfficeToPdf {
    private AbstractOfficeToPdf() {
        throw new IllegalAccessError("Utility class");
    }
    public final static String DOC="doc";
    public final static String DOCX="docx";
    public final static String XLS="xls";
    public final static String XLSX="xlsx";
    public final static String PPT="ppt";
    public final static String PPTX="pptx";
    public final static String PDF="pdf";
    public final static String TXT="txt";

    private static Log logger = LogFactory.getLog(AbstractOfficeToPdf.class);


    public static boolean excel2Pdf(String inExcelFile, String outPdfFile) throws TransformerException, IOException, ParserConfigurationException {
        String inFilePath = inExcelFile;
        String outFilePath = outPdfFile;
        if(File.separator.equals("\\")){
            inFilePath=inExcelFile.replace('/', '\\');
            outFilePath = outPdfFile.replace('/', '\\');
        }
        HSSFWorkbook excelBook = new HSSFWorkbook();
        ExcelTypeEnum excelType = ExcelTypeEnum.checkFileExcelType(inFilePath);
        if (excelType == ExcelTypeEnum.HSSF) {
            excelBook = new HSSFWorkbook(new FileInputStream(inFilePath));
        } else if (excelType == ExcelTypeEnum.XSSF) {
            XlsxTransformXls xls = new XlsxTransformXls();
            XSSFWorkbook workbookOld = new XSSFWorkbook(inFilePath);
            xls.transformXSSF(workbookOld, excelBook);
        } else {
            return false;
        }

        ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        excelToHtmlConverter.setOutputColumnHeaders(false);
        //去掉Excel行号
        excelToHtmlConverter.setOutputRowNumbers(false);

        excelToHtmlConverter.processWorkbook(excelBook);
        Document htmlDocument = excelToHtmlConverter.getDocument();


        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(new File(outFilePath));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        // TODO set encoding from a command argument
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "no");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);

        return true;
    }

    public static boolean ppt2Pdf(String inPptFile, String outPdfFile,String suffix) {
        String inputFile = inPptFile;
        String pdfFile = outPdfFile;
        if(File.separator.equals("\\")){
            inputFile=inPptFile.replace('/', '\\');
            pdfFile = outPdfFile.replace('/', '\\');
        }
        String sFileName = FileSystemOpt.extractFullFileName(pdfFile);
        POIPptToHtmlUtils.pptToHtml(inputFile, pdfFile.replace(sFileName, ""), sFileName,suffix);
        return false;
    }

    public static boolean word2Pdf(String inWordFile, String outPdfFile,String suffix) throws Exception {
        try {
            String inputFile = inWordFile;
            String pdfFile = outPdfFile;
            if(File.separator.equals("\\")){
                inputFile=inWordFile.replace('/', '\\');
                pdfFile = outPdfFile.replace('/', '\\');
            }
            if (DOCX.equalsIgnoreCase(suffix)) {
//            WordReportUtil.convertDocxToPdf(inputFile, outPdfFile);
                XWPFDocument docx = new XWPFDocument(new FileInputStream(inputFile));
                PdfOptions options = PdfOptions.create();
                // 中文字体处理
                options.fontProvider((familyName, encoding, size, style, color) -> {
                    try {
                        BaseFont bfChinese = BaseFont.createFont(AbstractOfficeToPdf.class.getClassLoader().getResource("simsun.ttf").getPath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        Font fontChinese = new Font(bfChinese, size, style, color);
                        if (familyName != null) {
                            fontChinese.setFamily(familyName);
                        }
                        return fontChinese;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });

                PdfConverter.getInstance().convert(docx, new FileOutputStream(pdfFile), options);
            }
            if (DOC.equalsIgnoreCase(suffix)) {
                HWPFDocumentCore wordDocument = AbstractWordUtils.loadDoc(new File(inputFile));
                WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    XMLHelper.getDocumentBuilderFactory().newDocumentBuilder()
                        .newDocument());
                wordToHtmlConverter.processDocument(wordDocument);
                Document doc = wordToHtmlConverter.getDocument();

                DOMSource domSource = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(new File(pdfFile));

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer serializer = tf.newTransformer();
                // TODO set encoding from a command argument
                serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty(OutputKeys.METHOD, "html");
                serializer.transform(domSource, streamResult);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return true;
    }


    static boolean office2Pdf(String inputFile, String pdfFile) throws Exception {
        String suffix = StringUtils.lowerCase(
            FileType.getFileExtName(inputFile));
        return office2Pdf(suffix, inputFile, pdfFile);
    }

    static boolean office2Pdf(String suffix, String inputFile, String pdfFile) throws Exception {

        File file = new File(inputFile);
        if (!(file.exists())) {
            return false;
        }
        if (PDF.equalsIgnoreCase(suffix)) {
            try {
                FileSystemOpt.fileCopy(inputFile, pdfFile);
                return true;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return false;
        }
        if (DOC.equalsIgnoreCase(suffix) || DOCX.equalsIgnoreCase(suffix)) {
            return word2Pdf(inputFile, pdfFile,suffix);
        } else if (PPT.equalsIgnoreCase(suffix) || PPTX.equalsIgnoreCase(suffix)) {
            return ppt2Pdf(inputFile, pdfFile,suffix);
        } else if (XLS.equalsIgnoreCase(suffix) || XLSX.equalsIgnoreCase(suffix)) {
            return excel2Pdf(inputFile, pdfFile);
        }
        return false;
    }

}
