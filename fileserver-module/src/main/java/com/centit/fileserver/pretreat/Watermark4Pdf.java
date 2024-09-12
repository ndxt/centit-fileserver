package com.centit.fileserver.pretreat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public abstract class Watermark4Pdf {
    private Watermark4Pdf() {
        throw new IllegalAccessError("Utility class");
    }

    public static final boolean runFlag = false;

    private static Log logger = LogFactory.getLog(Watermark4Pdf.class);


    /**
     * 为文档添加水印：目前只支持给pdf、word、excel、ppt增加水印，并且输出只能是Pdf文件。
     *
     * @param inputFile       源文件路径及文件 名称
     * @param outputFile      输出文件路径及文件名称
     * @param waterMarkStr    水印字符串
     * @param opacity         文字透明度(1-10)
     * @param rotation        旋转度数(1-170)
     * @param fontSize        字体大小(1-1)
     * @return boolean        目前 不支持位置自定义：因设置了文字大小、倾斜度后不好计算水印文字的长宽数据。
     */
    public static boolean addWatermark4Pdf(String inputFile,
                                           String outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize) throws IOException {
        return addWatermark4Pdf(Files.newInputStream(Paths.get(inputFile)),
                Files.newOutputStream(Paths.get(outputFile)), waterMarkStr, opacity,rotation, fontSize);
    }

    public static boolean addWatermark4Pdf(InputStream inputFile,
                                           OutputStream outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize) {
        PdfGState gs = new PdfGState();
        PdfReader pdfReader = null;
        PdfStamper pdfStamper = null;
        try{
            pdfReader = new PdfReader(inputFile);
            pdfStamper = new PdfStamper(pdfReader,  outputFile);

            BaseFont base = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",
                    BaseFont.NOT_EMBEDDED);
            if (base == null) {
                return false;
            }
            // 设置透明度为0.4
            gs.setFillOpacity(opacity);
            gs.setStrokeOpacity(opacity);
            int toPage = pdfStamper.getReader().getNumberOfPages();
            Rectangle pageRect;
            PdfContentByte content;
            for (int i = 1; i <= toPage; i++) {
                pageRect = pdfStamper.getReader().getPageSizeWithRotation(i);
                // 计算水印X,Y坐标
                float x = pageRect.getWidth() / 2;
                float y = pageRect.getHeight() / 2;
                // 获得PDF最顶层
                content = pdfStamper.getOverContent(i);
                content.saveState();
                // set Transparency
                content.setGState(gs);
                content.beginText();
                content.setColorFill(BaseColor.GRAY);
                content.setFontAndSize(base, fontSize);
                // 水印文字成45度角倾斜
                content.showTextAligned(Element.ALIGN_CENTER, waterMarkStr, x,
                        y, rotation);
                content.endText();
            }

        } catch (IOException | DocumentException e1) {
            logger.error(e1.getMessage(), e1);//e1.printStackTrace();
            return false;
        } finally {
            try {
                if(pdfStamper != null) {
                    pdfStamper.close();
                }
                if(pdfReader != null) {
                    pdfReader.close();
                }
            } catch (DocumentException | IOException e) {
                logger.error(e.getMessage(), e);//e.printStackTrace();
            }
        }
        return true;
    }

}
