package com.centit.fileserver.task;

import com.centit.fileserver.fileaccess.FilePretreatUtils;
import com.centit.fileserver.po.FileInfo;
import com.centit.fileserver.po.FileStoreInfo;
import com.centit.fileserver.service.FileInfoManager;
import com.centit.fileserver.service.FileStoreInfoManager;
import com.centit.fileserver.common.FileStore;
import com.centit.fileserver.common.FileOptTaskInfo;
import com.centit.fileserver.common.FileOptTaskQueue;
import com.centit.fileserver.utils.SystemTempFileUtils;
import com.centit.search.document.FileDocument;
import com.centit.search.service.Indexer;
import com.centit.support.file.FileMD5Maker;
import com.centit.support.file.FileSystemOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class FileOptTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(FileOptTaskExecutor.class);

    @Resource
    private FileStore fileStore;

    @Resource
    private Indexer documentIndexer;

    @Resource
    private FileOptTaskQueue fileOptTaskQueue;

    @Resource
    private FileInfoManager fileInfoManager;

    @Resource
    private FileStoreInfoManager fileStoreInfoManager;

    private Map<Integer, Consumer<FileOptTaskInfo>> fileOptList;

    public FileOptTaskExecutor(){
        fileOptList = new HashMap<>(20);
    }

    public void addFileOpt(int taskType, Consumer<FileOptTaskInfo> fileOpt){
        fileOptList.put(taskType, fileOpt);
    }
    /**
     * 保存文件至服务器
     * @param tempFilePath
     * @param fileMd5
     * @param fileSize
     */
    private void save(String tempFilePath, String fileMd5, long fileSize) {
        try {
            if (fileStoreInfoManager.getObjectById(fileMd5) == null) {
                String fileStorePath = fileStore.saveFile(tempFilePath, fileMd5, fileSize);
                FileStoreInfo fileStoreInfo = new FileStoreInfo(fileMd5, fileSize, fileStorePath, 1L);
                fileStoreInfoManager.saveNewObject(fileStoreInfo);
            } else {
                fileStoreInfoManager.increaseFileReferenceCount(fileMd5);
            }
        } catch (Exception e) {
            logger.info("保存文件出错: " + e.getMessage());
        }
    }

    /**
     * 保存文件操作
     * @param saveFileParams
     */
    private void saveFile(Map<String, Object> saveFileParams) {
        String fileMd5 = (String) saveFileParams.get("fileMd5");
        long fileSize = (long) saveFileParams.get("fileSize");
        String tempFilePath = SystemTempFileUtils.getTempFilePath(fileMd5, fileSize);
        save(tempFilePath, fileMd5, fileSize);
    }

    /**
     * 添加pdf副本操作
     * @param taskInfo
     */
    private void createPdf(FileOptTaskInfo taskInfo) {
        Map<String, Object> createPdfParams = taskInfo.getTaskOptParams();
        String fileId = (String) createPdfParams.get("fileId");
        long fileSize = (long) createPdfParams.get("fileSize");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String pdfTempFile = FilePretreatUtils.createPdf(fileInfo, originalTempFilePath);
            if (null != pdfTempFile) {
                save(pdfTempFile, fileInfo.getFileMd5(), new File(pdfTempFile).length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (IOException e) {
            logger.error("生成PDF文件出错！" + e.getMessage());
        }
    }

    /**
     * pdf添加水印操作
     * @param taskInfo
     */
    private void addWatermark(FileOptTaskInfo taskInfo) {
        Map<String, Object> waterMarkParams = taskInfo.getTaskOptParams();
        String fileId = (String) waterMarkParams.get("fileId");
        long fileSize = (long) waterMarkParams.get("fileSize");
        String waterMarkStr = (String) waterMarkParams.get("watermark");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String waterMarkPdfTempFile = FilePretreatUtils.addWatermarkForPdf(fileInfo, originalTempFilePath, waterMarkStr);
            if (null != waterMarkPdfTempFile) {
                save(waterMarkPdfTempFile, fileInfo.getFileMd5(), new File(waterMarkPdfTempFile).length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (IOException e) {
            logger.error("添加水印出错！", e);
        }
    }

    /**
     * 添加缩略图操作
     * @param taskInfo
     */
    private void addThumbnail(FileOptTaskInfo taskInfo) {
        Map<String, Object> thumbnailParams = taskInfo.getTaskOptParams();
        String fileId = (String) thumbnailParams.get("fileId");
        long fileSize = (long) thumbnailParams.get("fileSize");
        int width = (int) thumbnailParams.get("width");
        int height = (int) thumbnailParams.get("height");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String thumbnailFile = FilePretreatUtils.addThumbnail(fileInfo, originalTempFilePath, width, height);
            if (null != thumbnailFile) {
                File thumbnail = new File(thumbnailFile);
                save(thumbnailFile, FileMD5Maker.makeFileMD5(thumbnail), thumbnail.length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (IOException e) {
            logger.error("生成缩略图出错！", e);
        }
    }

    /**
     * zip压缩文件操作
     * @param zipParams
     */
    private void zipFile(Map<String, Object> zipParams) {
        String fileId = (String) zipParams.get("fileId");
        long fileSize = (long) zipParams.get("fileSize");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String zipFilePath = FilePretreatUtils.zipFile(fileInfo, originalTempFilePath);
            if (null != zipFilePath) {
                save(zipFilePath, fileInfo.getFileMd5(), new File(zipFilePath).length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (Exception e) {
            logger.error("Zip压缩文件时出错！", e);
        }
    }

    /**
     * zip加密压缩文件操作
     * @param zipEncryptParams
     */
    private void zipAndEncryptFile(Map<String, Object> zipEncryptParams) {
        String fileId = (String) zipEncryptParams.get("fileId");
        long fileSize = (long) zipEncryptParams.get("fileSize");
        String encryptPass = (String) zipEncryptParams.get("password");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String encryptedZipFile = FilePretreatUtils.zipFileAndEncrypt(fileInfo, originalTempFilePath, encryptPass);
            if (null != encryptedZipFile) {
                save(encryptedZipFile, fileInfo.getFileMd5(), new File(encryptedZipFile).length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (Exception e) {
            logger.error("Zip加密压缩文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }

    /**
     * AES加密文件操作
     * @param aesParams
     */
    private void encryptFileWithAes(Map<String, Object> aesParams) {
        String fileId = (String) aesParams.get("fileId");
        long fileSize = (long) aesParams.get("fileSize");
        String encryptPass = (String) aesParams.get("password");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        try {
            String aesEncryptedFile = FilePretreatUtils.encryptFileWithAes(fileInfo, originalTempFilePath, encryptPass);
            if (null != aesEncryptedFile) {
                save(aesEncryptedFile, fileInfo.getFileMd5(), new File(aesEncryptedFile).length());
                fileInfoManager.updateObject(fileInfo);
            }
        } catch (Exception e) {
            logger.error("AES加密文件时出错！", e);
        }
        FileSystemOpt.deleteFile(originalTempFilePath);
    }

    private void index(FileOptTaskInfo taskInfo) {
        Map<String, Object> indexParams = taskInfo.getTaskOptParams();
        String fileId = (String) indexParams.get("fileId");
        long fileSize = (long) indexParams.get("fileSize");
        FileInfo fileInfo = fileInfoManager.getObjectById(fileId);
        String originalTempFilePath = SystemTempFileUtils.getTempFilePath(fileInfo.getFileMd5(), fileSize);
        FileDocument fileDoc = FilePretreatUtils.index(fileInfo, originalTempFilePath);
        documentIndexer.saveNewDocument(fileDoc);
    }

    @PostConstruct
    public void doTask() {
        /*addFileOpt(FileOptTaskInfo.OPT_SAVE_FILE,
            new SaveFileOpt());*/
        new Thread(new FileOptTask()).start();
    }

    class FileOptTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    FileOptTaskInfo taskInfo = fileOptTaskQueue.get();
                    if (null == taskInfo) {
                        Thread.sleep(5000);
                    } else {
                        int taskType = taskInfo.getTaskType();

                        fileOptList.get(taskType).accept(taskInfo);
                        /*
                        switch (taskType) {
                            case FileOptTaskInfo.OPT_SAVE_FILE:
                                saveFile(taskInfo.getTaskOptParams());
                                break;
                            case FileOptTaskInfo.OPT_CREATE_PDF:
                                createPdf(taskInfo);
                                break;
                            case FileOptTaskInfo.OPT_PDF_WATERMARK:
                                addWatermark(taskInfo);
                                break;
                            case FileOptTaskInfo.OPT_ADD_THUMBNAIL:
                                addThumbnail(taskInfo);
                                break;
                            case FileOptTaskInfo.OPT_ZIP:
                                zipFile(taskInfo.getTaskOptParams());
                                break;
                            case FileOptTaskInfo.OPT_ENCRYPT_ZIP:
                                zipAndEncryptFile(taskInfo.getTaskOptParams());
                                break;
                            case FileOptTaskInfo.OPT_AES_ENCRYPT:
                                encryptFileWithAes(taskInfo.getTaskOptParams());
                                break;
                            case FileOptTaskInfo.OPT_DOCUMENT_INDEX:
                                index(taskInfo);
                                break;
                        }*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
