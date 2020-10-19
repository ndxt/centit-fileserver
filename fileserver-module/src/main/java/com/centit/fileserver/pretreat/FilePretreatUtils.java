package com.centit.fileserver.pretreat;

import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.search.document.FileDocument;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.algorithm.ZipCompressor;
import com.centit.support.file.*;
import com.centit.support.image.ImageOpt;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.AutoDetectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

public class FilePretreatUtils {

    private static final Logger logger = LoggerFactory.getLogger(FilePretreatUtils.class);


    /**
     * 将office文件转换为PDF
     *
     * @param inputFile office文件
     * @param pdfFile   PDF文件
     * @return 布尔值
     */
    public static boolean office2Pdf(String inputFile, String pdfFile) throws Exception {
        return AbstractOfficeToPdf.office2Pdf(inputFile, pdfFile);
    }

    public static boolean office2Pdf(String suffix, String inputFile, String pdfFile) throws Exception {
        return AbstractOfficeToPdf.office2Pdf(suffix, inputFile, pdfFile);
//        return OfficeToPdf.office2Pdf(suffix, "D:" + inputFile, "D:" + pdfFile); // for Windows
    }

    private static void updateCommonFileInfo(FileInfo fileInfo, String newFilePath) throws IOException {
        fileInfo.setAttachedType(fileInfo.getFileType());
        fileInfo.setAttachedFileMd5(fileInfo.getFileMd5());

        File pdfFile = new File(newFilePath);
        String fileMd5 = FileMD5Maker.makeFileMD5(pdfFile);
        fileInfo.setFileMd5(fileMd5);
    }

    /**
     * 给文件添加缩略图
     *
     * @param filename    文件名
     * @param thumbWidth  宽度
     * @param thumbHeight 高度
     * @param quality     quality
     * @param outFilename 处理后文件名
     * @return 布尔值
     */
    public static boolean createImageThumbnail(String filename, int thumbWidth, int thumbHeight, int quality,
                                               String outFilename) {
        boolean created = false;
        try {
            ImageOpt.createThumbnail(filename, thumbWidth, thumbHeight, quality, outFilename);
            created = true;
        } catch (InterruptedException | IOException e) {
            logger.error(e.getMessage(), e);
        }
        return created;
    }

    /**
     * 压缩文件
     *
     * @param inputFile       处理前的文件
     * @param fileName        文件名
     * @param zipFilePathName zip文件路径
     * @return 布尔值
     */
    public static boolean zipFile(String inputFile, String fileName, String zipFilePathName) {
        boolean ziped = false;
        try {
            ZipCompressor.compress(zipFilePathName, fileName, inputFile);
            ziped = true;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
        return ziped;
    }

    /**
     * 压缩文件并通过密码加密
     *
     * @param inputFilePath 处理前的文件
     * @param zipFilePath   zip文件路径
     * @param password      密码
     * @return 布尔值
     */
    public static boolean zipFileAndEncrypt(String inputFilePath, String zipFilePath, String password) {
        boolean ziped = false;
        try {
            // Initiate ZipFile object with the path/name of the zip file.
            ZipFile zipFile = new ZipFile(zipFilePath);
            // Build the list of files to be added in the array list
            // Objects of type File have to be added to the ArrayList
            ArrayList<File> filesToAdd = new ArrayList<>();
            filesToAdd.add(new File(inputFilePath));
            // Initiate Zip Parameters which define various properties such
            // as compression method, etc.
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to store compression
            // Set the compression level
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            // Set the encryption flag to true
            // If this is set to false, then the rest of encryption properties are ignored
            parameters.setEncryptFiles(true);

            // Set the encryption method to Standard Zip Encryption
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);

            // Set password
            parameters.setPassword(password);
            // Now add files to the zip file
            // Note: To add a single file, the method addFile can be used
            // Note: If the zip file already exists and if this zip file is a split file
            // then this method throws an exception as Zip Format Specification does not
            // allow updating split zip files
            zipFile.addFiles(filesToAdd, parameters);
            ziped = true;
        } catch (ZipException e) {
            logger.error(e.getMessage(), e);
        }

        return ziped;
    }

    /**
     * 加密文件：加密算法暂时不可以设定
     *
     * @param inputFile          处理前文件
     * @param diminationFileName diminationFileName
     * @param password           密码
     * @return 布尔值
     */
    public static boolean encryptFile(String inputFile, String diminationFileName, String password) {
        boolean encrypted = false;
        try {
            FileEncryptWithAes.encrypt(inputFile, diminationFileName, password);
            encrypted = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return encrypted;
    }

    public static String createPdf(FileInfo fileInfo, String sourceFilePath) throws Exception {

        String pdfTmpFile = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.pdf";
        if (FileSystemOpt.existFile(pdfTmpFile) || office2Pdf(fileInfo.getFileType(), sourceFilePath, pdfTmpFile)) {
            updateCommonFileInfo(fileInfo, pdfTmpFile);

            fileInfo.setFileType("P");
            fileInfo.setFileName(FileType.truncateFileExtName(fileInfo.getFileName()) + ".pdf");

            return pdfTmpFile;
        } else {
            logger.error("生成PDF文件出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static String addWatermarkForPdf(FileInfo fileInfo, String inputPdfPath, String waterMarkStr)
        throws IOException {
        String outputPdfPath = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "2.pdf";
        if (!inputPdfPath.endsWith(".pdf")) {
            String realInputPdfPath = FileType.truncateFileExtName(inputPdfPath) + ".pdf";
            FileSystemOpt.fileCopy(inputPdfPath, realInputPdfPath);
            inputPdfPath = realInputPdfPath;
        }
        boolean success = Watermark4Pdf.addWatermark4Pdf(inputPdfPath, outputPdfPath, waterMarkStr, 0.4f, 45f, 60f);
        if (success) {
            if (null == fileInfo.getAttachedFileMd5()) { // 原PDF不是附件
                updateCommonFileInfo(fileInfo, outputPdfPath);
            } else { // 原PDF是其他文件生成的附件
                File pdfFile = new File(outputPdfPath);
                String fileMd5 = FileMD5Maker.makeFileMD5(pdfFile);
                fileInfo.setFileMd5(fileMd5);
            }

            fileInfo.setFileType("P");

            return outputPdfPath;
        } else {
            logger.error("给PDF添加水印出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static String addThumbnail(FileInfo fileInfo, String sourceFilePath, int width, int height)
        throws IOException {
        String outFilePath = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.jpg";
        if (createImageThumbnail(sourceFilePath, width, height, 100, outFilePath)) {
            fileInfo.setAttachedType("T");
            fileInfo.setAttachedFileMd5(FileMD5Maker.makeFileMD5(new File(outFilePath)));

            return outFilePath;
        } else {
            logger.error("生成缩略图出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static String zipFile(FileInfo fileInfo, String sourceFilePath) throws IOException {
        String outFilePath = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.ent";
        if (zipFile(sourceFilePath, fileInfo.getFileName(), outFilePath)) {
            fileInfo.setFileMd5(FileMD5Maker.makeFileMD5(new File(outFilePath)));
            fileInfo.setEncryptType("Z");
            fileInfo.setFileName(
                FileType.truncateFileExtName(fileInfo.getFileName())
                    + ".zip");
            fileInfo.setFileType("zip");

            return outFilePath;
        } else {
            logger.error("Zip压缩文件时出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static String zipFileAndEncrypt(FileInfo fileInfo, String sourceFilePath, String encryptPass)
        throws IOException {
        String entFileDir = SystemTempFileUtils.getTempDirectory()
            + fileInfo.getFileMd5();
        FileSystemOpt.createDirect(entFileDir);
        String entFilePath = entFileDir + File.separatorChar + fileInfo.getFileName();
        String outFilePath = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.ent";

        FileSystemOpt.fileCopy(sourceFilePath, entFilePath);

        if (zipFileAndEncrypt(entFilePath, outFilePath, encryptPass)) {
            fileInfo.setFileMd5(FileMD5Maker.makeFileMD5(new File(outFilePath)));
            fileInfo.setEncryptType("Z");
            fileInfo.setFileName(
                FileType.truncateFileExtName(fileInfo.getFileName())
                    + ".zip");
            fileInfo.setFileType("zip");
            //删除临时文件
            FileSystemOpt.deleteFile(entFilePath);
            FileSystemOpt.deleteDirect(entFileDir);

            return outFilePath;
        } else {
            FileSystemOpt.deleteFile(entFilePath);
            FileSystemOpt.deleteDirect(entFileDir);
            logger.error("zipFileAndEncrypt 压缩文件时出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static String encryptFileWithAes(FileInfo fileInfo, String sourceFilePath, String encryptPass)
        throws IOException {
        if (StringUtils.isBlank(encryptPass)) {
            logger.error("设置AES加密时请同时设置密码！" + fileInfo.getFileMd5());
        }
        String outFilePath = SystemTempFileUtils.getTempDirectory() + fileInfo.getFileMd5() + "1.ent";
        if (encryptFile(sourceFilePath, outFilePath, encryptPass)) {
            File file = new File(outFilePath);
            String fileMd5 = FileMD5Maker.makeFileMD5(file);
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setEncryptType("A");

            return outFilePath;
        } else {
            logger.error("AES加密文件时出错！" + fileInfo.getFileMd5());
        }

        return null;
    }

    public static FileDocument index(FileInfo fileInfo, String sourceFilePath) {
        FileDocument fileDoc = new FileDocument();
        fileDoc.setFileId(fileInfo.getFileId());
        fileDoc.setOsId(fileInfo.getOsId());
        fileDoc.setOptId(fileInfo.getLibraryId());
        fileDoc.setOptMethod(fileInfo.getOptMethod());
        fileDoc.setOptTag(fileInfo.getParentFolder());
        fileDoc.setFileMD5(fileInfo.getFileMd5());
        fileDoc.setFileName(fileInfo.getFileName());
        fileDoc.setFileSummary(fileInfo.getFileDesc());
        fileDoc.setOptUrl(fileInfo.getFileShowPath());
        fileDoc.setUserCode(fileInfo.getFileOwner());
        fileDoc.setUnitCode(fileInfo.getFileUnit());
        //获取文件的文本信息
        try {
            String charset ="";
            if("txt".equals(fileInfo.getFileType())){
                charset=new AutoDetectReader(new FileInputStream(sourceFilePath)).getCharset().name();
            }
            if("GB18030".equals(charset)){
                fileDoc.setContent(new String(Files.readAllBytes(Paths.get(sourceFilePath)), Charset.forName("GB18030")));
            }else {
                fileDoc.setContent(TikaTextExtractor.extractFileText(sourceFilePath));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        fileDoc.setCreateTime(new Date());
        fileInfo.setIndexState("I");

        return fileDoc;
    }
}
