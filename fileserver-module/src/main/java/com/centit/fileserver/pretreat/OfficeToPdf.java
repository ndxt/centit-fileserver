package com.centit.fileserver.pretreat;

import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class OfficeToPdf {
    private OfficeToPdf() {
        throw new IllegalAccessError("Utility class");
    }
    private static Log logger = LogFactory.getLog(OfficeToPdf.class);

    /**
     * 修改 excel多sheet 转换PDF 问题，一张表格 超大不分页，一个sheet = 一页pdf
     * @param inExcelFile 输入excel文件
     * @param outPdfFile 临时pdf
     * @return 是否成功
     */
    public static boolean excel2Pdf(String inExcelFile, String outPdfFile) {
        String inputFile = inExcelFile.replace('/','\\');
        String pdfFile   = outPdfFile.replace('/','\\');
        ComThread.InitSTA();
        boolean successed =true;
        ActiveXComponent actcom = new ActiveXComponent("Excel.Application");
        int count=0;
        String outFile = pdfFile.substring(0,pdfFile.lastIndexOf("."));
        try {
            actcom.setProperty("Visible", new Variant(false));
            Dispatch workbooks = actcom.getProperty("Workbooks").toDispatch();
            Dispatch excel = Dispatch.invoke(workbooks,"Open",Dispatch.Method,
                    new Object[]{inputFile,new Variant(false),new Variant(false)},  new int[9] ).toDispatch();
            Dispatch sheets= Dispatch.get(excel, "Sheets").toDispatch();
            count = Dispatch.get(sheets, "Count").getInt();

            //將每一個sheet 分開轉換成 單獨的pdf
            for (int i = 1; i <=count ; i++) {
                try {
                    //獲得當前sheet
                    Dispatch sheet = Dispatch.invoke(sheets, "Item",
                            Dispatch.Get, new Object[]{i}, new int[1]).toDispatch();
                    //設置當前sheet 內容在一頁展示
                    Dispatch page = Dispatch.call(sheet, "PageSetup").toDispatch();
                    Dispatch.put(page, "PrintArea", false);//false或"" 表示打印sheet页中的 整个区域， 可以使用 excel表达式指定 要打印的单元格范围 ，比如 "$A$1:$C$5" 表示打印 A1-C5的单元格区域
                    Dispatch.put(page, "Orientation", 2);// 打印方向 1横向  2纵向
                    /**
                     * 将所有内容 无论行，列 有多少 都在一页显示，
                     * Zoom 必须为false ,FitToPagesTall、FitToPagesWide才有效！！
                     */
                    Dispatch.put(page, "Zoom", false);      //值为100=false， 缩放 10-400  %
                    Dispatch.put(page, "FitToPagesTall", 1);  //所有行为一页--   页高
                    Dispatch.put(page, "FitToPagesWide", 1);  //所有列为一页(1或false) --页宽

                    //將當前sheet轉換成 一個pdf
                    //String sheetname = Dispatch.get(sheet, "name").toString();

                    Dispatch.call(sheet, "Activate");
                    Dispatch.call(sheet, "Select");
                    Dispatch.invoke(excel, "SaveAs", Dispatch.Method,
                            new Object[]{outFile + "-" + i + ".pdf", new Variant(57), new Variant(false),
                                    new Variant(57), new Variant(57), new Variant(false),
                                    new Variant(true), new Variant(57), new Variant(false),
                                    new Variant(true), new Variant(false)}, new int[1]);
                    //System.out.println("Excel sheet to pdf Success :"+outFile+"-"+ i+".pdf");
                }catch (Exception e){
                    logger.error("可能是因为页面 "+i+" 没有任何元素导致打印失败！ "+e.getMessage(), e);
                }
            }
            //关闭
            Dispatch.call(excel, "Close", new Variant(false));

        } catch (Exception es) {
            successed =false;
            logger.error(es.getMessage(), es);
        }finally {
            //释放jcom线程
            actcom.invoke("Quit", new Variant[0]);
            ComThread.Release();
        }

        //將多個 pdf 合併到一個pdf,可能會有 頁面大小不一問題，需要合併之前 求出最大頁面pageSizes
        if(count>0){
            FileSystemOpt.deleteFile(pdfFile);
            try {

                Document document = new Document();
                FileOutputStream out = new FileOutputStream(new File(pdfFile));
                PdfCopy copy = new PdfCopy(document, out);
                document.open();
                for (int i = 1; i <= count; i++) {
                    File pdfPieceFile = new File(outFile + "-" + i + ".pdf");//合并pdf 临时文件
                    if (pdfPieceFile.exists()) {
                        PdfReader reader = new PdfReader(outFile + "-" + i + ".pdf");
                        int n = reader.getNumberOfPages();
                        for (int j = 1; j <= n; j++) {
                            document.newPage();
                            PdfImportedPage page = copy.getImportedPage(reader, j);
                            copy.addPage(page);
                        }
                        reader.close();
                    }
                    pdfPieceFile.delete();
                }
                copy.close();
                document.close();
                out.flush();
                out.close();
                //System.out.println("合并sheet pdf 到 "+pdfFile+" 成功！");
            }catch (Exception e) {
                successed =false;
                logger.error(e.getMessage(),e);//e.printStackTrace();
            }
        }
        return successed;

    }

    public static boolean excelExportToPdf(String inExcelFile, String outPdfFile) {
        String inFilePath = inExcelFile.replace('/','\\');
        String outFilePath   = outPdfFile.replace('/','\\');
        ComThread.InitSTA();
        ActiveXComponent ax=new ActiveXComponent("Excel.Application");
        try{
            ax.setProperty("Visible", new Variant(false));
            ax.setProperty("AutomationSecurity", new Variant(3)); //禁用宏
            Dispatch excels=ax.getProperty("Workbooks").toDispatch();

            Dispatch excel=Dispatch.invoke(excels,"Open",Dispatch.Method,new Object[]{
                            inFilePath,
                            new Variant(false),
                            new Variant(false)
                    },
                    new int[9]).toDispatch();
            //转换格式
            Dispatch.call(excel,"ExportAsFixedFormat",
                    new Variant(0), //PDF格式=0
                    outFilePath);

            Dispatch.call(excel, "Close",new Variant(false));

            return true;
        }catch(Exception es){
            logger.error(es.getMessage(),es);
            return false;
        }finally {
            ax.invoke("Quit",new Variant[]{});
            ComThread.Release();
        }
    }

    public static boolean ppt2Pdf(String inPptFile, String outPdfFile) {
        String inputFile = inPptFile.replace('/','\\');
        String pdfFile   = outPdfFile.replace('/','\\');
        ComThread.InitSTA();
        ActiveXComponent app = new ActiveXComponent("PowerPoint.Application");
        try {

            Dispatch ppts = app.getProperty("Presentations").toDispatch();
            Dispatch ppt = Dispatch
                    .call(ppts, "Open",  inputFile, Boolean.valueOf(true),
                            Boolean.valueOf(true), Boolean.valueOf(false))
                    .toDispatch();

            FileSystemOpt.deleteFile(pdfFile);
            /*Dispatch.invoke(ppt, "SaveAs", Dispatch.Method, new Object[] {
                    new Variant(pdfFile) , new Variant(32) }, new int[0]);*/

            Dispatch.call(ppt, "SaveAs", pdfFile, Integer.valueOf(32));
            Dispatch.call(ppt, "Close");

            //System.out.println("ppt转换为PDF完成！");
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }finally {
            app.invoke("Quit");
            ComThread.Release();
        }
        return false;
    }

    public static boolean word2Pdf(String inWordFile, String outPdfFile) {
        String inputFile = inWordFile.replace('/','\\');
        String pdfFile   = outPdfFile.replace('/','\\');
        ComThread.InitSTA();
        //long start = System.currentTimeMillis();
        ActiveXComponent app = new ActiveXComponent("Word.Application");
        try {
            // 设置word不可见
            app.setProperty("Visible", new Variant(false));
            // 打开word文件
            Dispatch docs = app.getProperty("Documents").toDispatch();
            //doc = Dispatch.call(docs,  "Open" , sourceFile).toDispatch();
            Dispatch doc = Dispatch.invoke(docs,"Open",Dispatch.Method,new Object[] {
                    inputFile, new Variant(false),new Variant(true) }, new int[1]).toDispatch();
            //System.out.println("打开文档..." + inputFile);
            //System.out.println("转换文档到PDF..." + pdfFile);
            FileSystemOpt.deleteFile(pdfFile);
            // Dispatch.call(doc, "SaveAs",  destFile,  17);
            // 作为html格式保存到临时文件：：参数 new Variant(8)其中8表示word转html;7表示word转txt;
            //   44表示Excel转html;17表示word转成pdf。。
            Dispatch.invoke(doc, "SaveAs", Dispatch.Method, new Object[] {
                    pdfFile, new Variant(17) }, new int[1]);
            Dispatch.call(doc,"Close",false);

        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage(),e);//e.printStackTrace();
            //System.out.println("========Error:文档转换失败：" + e.getMessage());
        } finally {
            app.invoke("Quit", new Variant[] {});
            ComThread.Release();
        }
        //如果没有这句话,winword.exe进程将不会关闭

        return true;
    }

    public static boolean wps2Pdf(String inWpsFile, String outPdfFile) {
        File sFile = new File(inWpsFile.replace('/','\\'));
        File tFile = new File(outPdfFile.replace('/','\\'));
        boolean successed =true;
        ActiveXComponent wps = null;
        try {
            ComThread.InitSTA();
            wps = new ActiveXComponent("wps.application");
            ActiveXComponent doc = wps.invokeGetComponent("Documents")
                    .invokeGetComponent("Open", new Variant(sFile.getAbsolutePath()));
            doc.invoke("ExportPdf", new Variant(tFile.getAbsolutePath()));
            doc.invoke("Close");
            doc.safeRelease();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            successed=false;
        } finally {
            if (wps != null) {
                wps.invoke("Terminate");
                wps.safeRelease();
            }
            ComThread.Release();
        }
        return successed;
    }

    public static boolean office2Pdf(String inputFile, String pdfFile) {
        String suffix = StringUtils.lowerCase(
                FileType.getFileExtName(inputFile));
        //System.out.println("文件格式不支持转换为PDF!");
        return office2Pdf(suffix,inputFile, pdfFile);
    }

    public static boolean office2Pdf(String suffix, String inputFile, String pdfFile) {

        File file = new File(inputFile);
        if (!(file.exists())) {
            //System.err.println("文件不存在！");
            return false;
        }
        if (suffix.equalsIgnoreCase("pdf")) {
            //System.out.println("PDF文件无需转换为PDF!");
            try {
                FileSystemOpt.fileCopy(inputFile, pdfFile);
                return true;
            } catch (IOException e) {
                logger.error(e.getMessage(),e);//e.printStackTrace();
            }
            return false;
        }
        if (suffix.equalsIgnoreCase("doc") || suffix.equalsIgnoreCase("docx")) {
            return word2Pdf(inputFile, pdfFile);
        }else if (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx")) {
            return ppt2Pdf(inputFile, pdfFile);
        }else if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")
                || suffix.equalsIgnoreCase("xlsm") ){
            return excel2Pdf(inputFile, pdfFile);
        }else if (suffix.equalsIgnoreCase("wps")) {
            return wps2Pdf(inputFile, pdfFile);
        }
        //System.out.println("文件格式不支持转换为PDF!");
        return false;
    }

}
