package com.centit.fileserver.pretreat;

import com.centit.fileserver.common.FileBaseInfo;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.office.OfficeToPdf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author zhf
 */
public abstract class AbstractOfficeToPdf {
    private AbstractOfficeToPdf() {
        throw new IllegalAccessError("Utility class");
    }

    public final static String DOC = "doc";
    public final static String DOCX = "docx";
    public final static String XLS = "xls";
    public final static String XLSX = "xlsx";
    public final static String PPT = "ppt";
    public final static String PPTX = "pptx";
    public final static String PDF = "pdf";
    public final static String TXT = "txt";

    private static Log logger = LogFactory.getLog(AbstractOfficeToPdf.class);
    public static boolean office2Pdf(String inputFile, String pdfFile) throws Exception {
        return office2Pdf(FileType.getFileExtName(inputFile), inputFile, pdfFile);
    }

    public static boolean canTransToPdf(String fileType) {
        if(StringUtils.isBlank(fileType)){
            return false;
        }
        return StringUtils.equalsAnyIgnoreCase(fileType,
            //AbstractOfficeToPdf.DOC,
            AbstractOfficeToPdf.DOCX,
            AbstractOfficeToPdf.XLS, AbstractOfficeToPdf.XLSX,
            AbstractOfficeToPdf.PPT, AbstractOfficeToPdf.PPTX);
    }

    public static boolean office2Pdf(String suffix, String inputFile, String pdfFile) throws Exception {

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
            return OfficeToPdf.word2Pdf(inputFile, pdfFile, suffix);
        } else if (PPT.equalsIgnoreCase(suffix) || PPTX.equalsIgnoreCase(suffix)) {
            return OfficeToPdf.ppt2Pdf(inputFile, pdfFile, suffix);
        } else if (XLS.equalsIgnoreCase(suffix) || XLSX.equalsIgnoreCase(suffix)) {
            return OfficeToPdf.excel2Pdf(inputFile, pdfFile);
        }
        return false;
    }

}
