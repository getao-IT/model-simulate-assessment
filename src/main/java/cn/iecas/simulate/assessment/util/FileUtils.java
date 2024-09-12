package cn.iecas.simulate.assessment.util;

import cn.iecas.simulate.assessment.entity.domain.FileInfo;
import cn.iecas.simulate.assessment.entity.domain.FileUploadChunkInfo;
import cn.iecas.simulate.assessment.entity.domain.FileUploadPartialInfo;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;



/**
 * @Time: 2024/9/6 14:06
 * @Author: guoxun
 * @File: FileUtils
 * @Description: 文件工具类
 */
public class FileUtils {


    /**
     * 根据路径获取文件信息
     * @param path 文件路径
     * @return
     */
    public static FileInfo getFileInfoByFile(Path path) throws IOException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(path.getFileName().toString());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        fileInfo.setCreateTime(new Date(attr.creationTime().toMillis()));
        fileInfo.setModifyTime(new Date(attr.lastModifiedTime().toMillis()));
        fileInfo.setSize(Files.size(path));
        if (Files.isDirectory(path)){
            fileInfo.setType("folder");
            calculateFolderSizeSync(path, fileInfo);        // 同步异步改这里就行
        }
        else {
            if (fileInfo.getFilename().split("\\.").length > 1){
                int idx = fileInfo.getFilename().split("\\.").length - 1;
                String type = fileInfo.getFilename().split("\\.")[idx];
                fileInfo.setType(type);
            }
            else {
                fileInfo.setType("none");
            }
        }
        fileInfo.setLocalPath(path.getParent().toString());
        return fileInfo;
    }


    /**
     * 异步计算文件夹的大小
     * @param folder
     * @param fileInfo
     */
    private static void calculateFolderSizeAsync(Path folder, FileInfo fileInfo){
        CompletableFuture.runAsync(() -> {
            calculateFolderSize(folder, fileInfo);
        });
    }


    /**
     * 计算文件大小
     * @param folder
     * @param fileInfo
     */
    private static void calculateFolderSize(Path folder, FileInfo fileInfo) {
        try {
            AtomicLong subFileCount = new AtomicLong(0L);
            AtomicLong subFolderCount = new AtomicLong(0L);
            long folderSize = 0L;
            try (Stream<Path> filesWalk = Files.walk(folder)) {
                folderSize = filesWalk.mapToLong(p -> {
                    try {
                        if (Files.isDirectory(p))
                            subFolderCount.getAndIncrement();
                        else
                            subFileCount.getAndIncrement();
                        return Files.size(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0L;
                    }
                }).sum();
            }
            fileInfo.setSize(folderSize);
            fileInfo.setSubFileCount(subFileCount.get());
            fileInfo.setSubFolderCount(subFolderCount.get());
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * 同步计算文件夹的大小
     * @param folder
     * @param fileInfo
     */
    private static void calculateFolderSizeSync(Path folder, FileInfo fileInfo){
        calculateFolderSize(folder, fileInfo);
    }


    /**
     * 文件写入
     * @param target 所要保存的地址
     * @param src   输入流
     */
    public static void write(String target, InputStream src) throws IOException{
        try (FileOutputStream fileOutputStream = new FileOutputStream(target)){
            byte[] buffer = new byte[1024];
            int len;
            while (-1 != (len = src.read(buffer))){
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
        }
    }


    /**
     * 保存文本文件到指定文件目录下
     * @param filePath 文件目录
     * @param content 文本文件内容
     */
    public static void saveData2Txt(String filePath, String content) throws IOException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
            writer.write(content);
        }
    }


    /**
     * 申请保存文件所需的磁盘空间
     * @param savePath 文件保存路径
     * @param fileSize 文件大小
     * @return 所申请的临时文件的名称
     */
    public static String applyingDiskSpace(String savePath, Long fileSize) throws IOException{
        String cacheFilename = savePath + ".cache.tmp";
        try (RandomAccessFile raf = new RandomAccessFile(cacheFilename, "rw")){
            raf.setLength(fileSize);
        }
        return cacheFilename;
    }


    /**
     * 创建分片上传信息记录临时文件
     * @param savePath 临时文件保存路径, 需含文件名
     * @return
     */
    public static String createPartialUploadTempFile(String savePath, Long fileSize
            , Long partialSize) throws IOException{
        String partialUploadTempInfoSavePath = savePath + ".partial.upload.tmp";
        int chunkCount = (int) Math.ceil((fileSize * 1.0) / partialSize);
        FileUploadPartialInfo fileUploadPartialInfo = new FileUploadPartialInfo();
        fileUploadPartialInfo.setTotalChunks(chunkCount);
        for (int i = 0; i < chunkCount; i++){
            if (i != chunkCount -1)
                fileUploadPartialInfo.getChunkInfoList()
                        .add(new FileUploadChunkInfo(i, false));
            else
                fileUploadPartialInfo.getChunkInfoList()
                        .add(new FileUploadChunkInfo(i, false));
        }
        saveData2Txt(partialUploadTempInfoSavePath, JSON.toJSONString(fileUploadPartialInfo));
        return partialUploadTempInfoSavePath;
    }


    /**
     * 创建分片上传信息记录临时文件
     * @return
     */
    public static String createPartialUploadTempFile(Long fileSize, Long chunkSize){
        int chunkCount = (int) Math.ceil((fileSize * 1.0) / chunkSize);
        FileUploadPartialInfo fileUploadPartialInfo = new FileUploadPartialInfo();
        fileUploadPartialInfo.setTotalChunks(chunkCount);
        for (int i = 0; i < chunkCount; i++){
            if (i != chunkCount -1)
                fileUploadPartialInfo.getChunkInfoList()
                        .add(new FileUploadChunkInfo(i, false));
            else
                fileUploadPartialInfo.getChunkInfoList()
                        .add(new FileUploadChunkInfo(i, false));
        }
        return JSON.toJSONString(fileUploadPartialInfo);
    }


    /**
     * 如果文件夹不存在，创建文件夹
     * @param dirPath 文件夹路径
     * @throws IOException
     */
    public static void createDirIfNotExist(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path))
            Files.createDirectories(path);
    }


    /**
     * 计算MD5
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String calculateMD5(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }

        byte[] md5Bytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    /**
     * md5校验
     * @param fileInputStream
     * @param md5
     * @return
     * @throws Exception
     */
    public static Boolean checkFileMd5(InputStream fileInputStream, String md5) throws Exception{
        String calculateMD5 = calculateMD5(fileInputStream);
        return calculateMD5.equals(md5);
    }


    /**
     * 将inputStream 转化为byte
     * @param ips
     * @return
     */
    private static byte[] toByteArray(InputStream ips) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = ips.read(data, 0, data.length)) != -1){
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }


    /**
     * 保存所上传的文件
     * @param target
     * @param md5
     * @param inputStream
     * @throws IOException
     */
    public static Map<String, Object> saveFile(String target, String md5, InputStream inputStream) throws Exception {
        Map<String, Object> result = new HashMap<>();
        byte[] byteArray = toByteArray(inputStream);
        if (checkFileMd5(new ByteArrayInputStream(byteArray), md5)){
            write(target, new ByteArrayInputStream(byteArray));
            result.put("path", target);
            return result;
        }
        else {
            Files.deleteIfExists(Paths.get(target));
            throw new RuntimeException("md5码校验未通过, 请重新上传文件");
        }
    }


    /**
     * 根据文件路径, 读取文本文件内容
     * @param filePath 文件地址
     * @return 文件内容
     */
    public static String readFileByPath(String filePath) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        StringBuilder content = new StringBuilder();
        for (String line : allLines){
            content.append(line).append("\n");
        }
        return content.toString();
    }


    /**
     * 分块写入数据 不能直接调用该方法，调用该方法前，请先调用applyingDiskSpace方法申请空间
     * @param filepath 缓存块路径，即applyingDiskSpace方法的缓存块存放路径
     * @param inputStream 输入流
     * @param chunkMd5 块md5
     * @param chunkId 块id
     * @param chunkSize 块大小
     * @return 是否保存成功
     * @throws Exception error
     */
    public static Boolean writeWithChunk(String filepath, InputStream inputStream, String chunkMd5
            , Integer chunkId, Long chunkSize)
            throws Exception {
        byte[] byteArray = toByteArray(inputStream);
        if (checkFileMd5(new ByteArrayInputStream(byteArray), chunkMd5)){
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filepath, "rw")){
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
                randomAccessFile.seek(chunkId * chunkSize);
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = byteArrayInputStream.read(buffer))){
                    randomAccessFile.write(buffer, 0, len);
                }
            }
            return true;
        }
        else {
            Files.deleteIfExists(Paths.get(filepath));
            throw new RuntimeException("md5码校验未通过, 请重新上传文件");
        }
    }
}
