package cn.iecas.simulate.assessment.util;

import cn.iecas.simulate.assessment.entity.domain.FileInfo;
import cn.iecas.simulate.assessment.entity.domain.FileUploadChunkInfo;
import cn.iecas.simulate.assessment.entity.domain.FileUploadPartialInfo;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
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
import java.util.*;

import org.apache.commons.compress.archivers.tar.*;
import com.github.junrar.*;
import com.github.junrar.rarfile.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * @Time: 2024/9/6 14:06
 * @Author: guoxun
 * @File: FileUtils
 * @Description: 文件工具类
 */
public class FileUtils {


    /**
     * 根据路径获取文件信息
     *
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
        if (Files.isDirectory(path)) {
            fileInfo.setType("folder");
            calculateFolderSizeSync(path, fileInfo);        // 同步异步改这里就行
        } else {
            if (fileInfo.getFilename().split("\\.").length > 1) {
                int idx = fileInfo.getFilename().split("\\.").length - 1;
                String type = fileInfo.getFilename().split("\\.")[idx];
                fileInfo.setType(type);
            } else {
                fileInfo.setType("none");
            }
        }
        fileInfo.setLocalPath(path.getParent().toString());
        return fileInfo;
    }


    /**
     * 异步计算文件夹的大小
     *
     * @param folder
     * @param fileInfo
     */
    private static void calculateFolderSizeAsync(Path folder, FileInfo fileInfo) {
        CompletableFuture.runAsync(() -> {
            calculateFolderSize(folder, fileInfo);
        });
    }


    /**
     * 计算文件大小
     *
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 同步计算文件夹的大小
     *
     * @param folder
     * @param fileInfo
     */
    private static void calculateFolderSizeSync(Path folder, FileInfo fileInfo) {
        calculateFolderSize(folder, fileInfo);
    }


    /**
     * 文件写入
     *
     * @param target 所要保存的地址
     * @param src    输入流
     */
    public static void write(String target, InputStream src) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int len;
            while (-1 != (len = src.read(buffer))) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
        }
    }


    /**
     * 保存文本文件到指定文件目录下
     *
     * @param filePath 文件目录
     * @param content  文本文件内容
     */
    public static void saveData2Txt(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }


    /**
     * 申请保存文件所需的磁盘空间
     *
     * @param savePath 文件保存路径
     * @param fileSize 文件大小
     * @return 所申请的临时文件的名称
     */
    public static String applyingDiskSpace(String savePath, Long fileSize) throws IOException {
        String cacheFilename = savePath + ".cache.tmp";
        try (RandomAccessFile raf = new RandomAccessFile(cacheFilename, "rw")) {
            raf.setLength(fileSize);
        }
        return cacheFilename;
    }


    /**
     * 创建分片上传信息记录临时文件
     *
     * @param savePath 临时文件保存路径, 需含文件名
     * @return
     */
    public static String createPartialUploadTempFile(String savePath, Long fileSize
            , Long partialSize) throws IOException {
        String partialUploadTempInfoSavePath = savePath + ".partial.upload.tmp";
        int chunkCount = (int) Math.ceil((fileSize * 1.0) / partialSize);
        FileUploadPartialInfo fileUploadPartialInfo = new FileUploadPartialInfo();
        fileUploadPartialInfo.setTotalChunks(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            if (i != chunkCount - 1)
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
     *
     * @return
     */
    public static String createPartialUploadTempFile(Long fileSize, Long chunkSize) {
        int chunkCount = (int) Math.ceil((fileSize * 1.0) / chunkSize);
        FileUploadPartialInfo fileUploadPartialInfo = new FileUploadPartialInfo();
        fileUploadPartialInfo.setTotalChunks(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            if (i != chunkCount - 1)
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
     *
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
     *
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
     *
     * @param fileInputStream
     * @param md5
     * @return
     * @throws Exception
     */
    public static Boolean checkFileMd5(InputStream fileInputStream, String md5) throws Exception {
        String calculateMD5 = calculateMD5(fileInputStream);
        return calculateMD5.equals(md5);
    }


    /**
     * 将inputStream 转化为byte
     *
     * @param ips
     * @return
     */
    private static byte[] toByteArray(InputStream ips) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = ips.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }


    /**
     * 保存所上传的文件
     *
     * @param target
     * @param md5
     * @param inputStream
     * @throws IOException
     */
    public static Map<String, Object> saveFile(String target, String md5, InputStream inputStream) throws Exception {
        Map<String, Object> result = new HashMap<>();
        byte[] byteArray = toByteArray(inputStream);
        if (checkFileMd5(new ByteArrayInputStream(byteArray), md5)) {
            write(target, new ByteArrayInputStream(byteArray));
            result.put("path", target);
            return result;
        } else {
            Files.deleteIfExists(Paths.get(target));
            throw new RuntimeException("md5码校验未通过, 请重新上传文件");
        }
    }


    /**
     * 根据文件路径, 读取文本文件内容
     *
     * @param filePath 文件地址
     * @return 文件内容
     */
    public static String readFileByPath(String filePath) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        StringBuilder content = new StringBuilder();
        for (String line : allLines) {
            content.append(line).append("\n");
        }
        return content.toString();
    }


    /**
     * 分块写入数据 不能直接调用该方法，调用该方法前，请先调用applyingDiskSpace方法申请空间
     *
     * @param filepath    缓存块路径，即applyingDiskSpace方法的缓存块存放路径
     * @param inputStream 输入流
     * @param chunkMd5    块md5
     * @param chunkId     块id
     * @param chunkSize   块大小
     * @return 是否保存成功
     * @throws Exception error
     */
    public static Boolean writeWithChunk(String filepath, InputStream inputStream, String chunkMd5
            , Integer chunkId, Long chunkSize)
            throws Exception {
        byte[] byteArray = toByteArray(inputStream);
        if (checkFileMd5(new ByteArrayInputStream(byteArray), chunkMd5)) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filepath, "rw")) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
                randomAccessFile.seek(chunkId * chunkSize);
                byte[] buffer = new byte[1024];
                int len;
                while (-1 != (len = byteArrayInputStream.read(buffer))) {
                    randomAccessFile.write(buffer, 0, len);
                }
            }
            return true;
        } else {
            Files.deleteIfExists(Paths.get(filepath));
            throw new RuntimeException("md5码校验未通过, 请重新上传文件");
        }
    }

    /**
     * @return java.lang.Boolean
     * @Description 判断文件是否存在
     * @Author getao
     * @Date 17:43 2025/3/13
     * @Param [path]
     */
    public static Boolean isExist(String path) {
        File file = new File(path);
        return file.exists();
    }


    /**
     * @return java.lang.String[]
     * @Description 获取影像文件后缀
     * @Author getao
     * @Date 16:12 2025/3/14
     * @Param []
     */
    public static String[] getExtendsion() {
        return new String[]{"jpg", "JPG", "tiff", "tif", "png", "PNG", "TIF", "TIFF", "jpeg"};
    }


    /**
     * 替换文件名后缀
     *
     * @param path
     * @param extension
     * @return
     */
    public static String replaceExtension(String path, String extension) {
        if (path == null) {
            return null;
        } else {
            int extensionPos = path.lastIndexOf(46);
            int lastUnixPos = path.lastIndexOf(47);
            int lastWindowsPos = path.lastIndexOf(92);
            int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
            int index = lastSeparator > extensionPos ? -1 : extensionPos;
            String srcExt = index == -1 ? "" : path.substring(index + 1);
            return path.replace(srcExt, extension);
        }
    }


    /**
     * @author: getao
     * @Date: 2025/7/23 20:48
     * @Description: 获取压缩文件目录结构
     */
    @Data
    public static class ArchiveNode {
        private final String name;
        private final String fullPath;
        private final boolean isDirectory;
        private final long size;
        private final List<ArchiveNode> children = new ArrayList<>();

        public ArchiveNode(String name, String fullPath, boolean isDirectory, long size) {
            this.name = name;
            this.fullPath = fullPath;
            this.isDirectory = isDirectory;
            this.size = size;
        }

        public void addChild(ArchiveNode node) {
            children.add(node);
        }

        // 树形结构打印方法
        public String toTreeString() {
            return toTreeString("", new StringBuilder()).toString();
        }

        private StringBuilder toTreeString(String prefix, StringBuilder sb) {
            sb.append(prefix);
            if (!prefix.isEmpty()) {
                sb.append("├── ");
            }
            sb.append(name);
            if (isDirectory) {
                sb.append("/");
            }
            sb.append(" (").append(isDirectory ? "dir" : "file").append(", ").append(size).append(" bytes)");
            sb.append("\n");

            for (int i = 0; i < children.size(); i++) {
                boolean isLast = (i == children.size() - 1);
                String childPrefix = prefix + (prefix.isEmpty() ? "" : (isLast ? "    " : "│   "));
                children.get(i).toTreeString(childPrefix, sb);
            }
            return sb;
        }
    }

    public static ArchiveNode exploreArchive(File archiveFile) throws Exception {
        String name = archiveFile.getName().toLowerCase();

        if (name.endsWith(".zip")) {
            return exploreZip(archiveFile);
        } else if (name.endsWith(".tar")) {
            return exploreTar(archiveFile);
        } else if (name.endsWith(".rar")) {
            return exploreRar(archiveFile);
        } else {
            throw new IllegalArgumentException("Unsupported archive format: " + name);
        }
    }

    private static ArchiveNode exploreZip(File file) throws IOException {
        ArchiveNode root = new ArchiveNode(file.getName(), "", true, 0);
        Map<String, ArchiveNode> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        try (ZipFile zip = new ZipFile(file, Charset.forName("GBK"))) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = entry.getName();

                // 处理目录路径
                if (entry.isDirectory() && !path.endsWith("/")) {
                    path += "/";
                }

                // 创建路径节点
                createPathNodes(path, entry.isDirectory(), entry.getSize(), nodeMap);
            }
        }
        return root;
    }

    /**
     *  @author: getao
     *  @Date: 2025/7/25 17:54
     *  @Description: 解析tar压缩包目录结构
     */
    private static ArchiveNode exploreTar(File file) throws IOException {
        ArchiveNode root = new ArchiveNode(file.getName(), "", true, 0);
        Map<String, ArchiveNode> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        try (TarArchiveInputStream tis = new TarArchiveInputStream(new FileInputStream(file))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tis.getNextEntry()) != null) {
                String path = entry.getName();

                // 处理目录路径
                if (entry.isDirectory() && !path.endsWith("/")) {
                    path += "/";
                }

                // 创建路径节点
                createPathNodes(path, entry.isDirectory(), entry.getSize(), nodeMap);
            }
        }
        return root;
    }

    /**
     *  @author: getao
     *  @Date: 2025/7/25 17:53
     *  @Description: 解析rar压缩包目录结构
     *   - 目前仅支持rar4版本
     */
    private static ArchiveNode exploreRar(File file) throws Exception {
        ArchiveNode root = new ArchiveNode(file.getName(), "", true, 0);
        Map<String, ArchiveNode> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        try (FileInputStream fis = new FileInputStream(file);
             Archive archive = new Archive(fis)) {
            FileHeader fh;
            while ((fh = archive.nextFileHeader()) != null) {
                if (fh.isEncrypted()) continue;

                String path = fh.getFileName().replace('\\', '/');

                // 处理目录路径
                if (fh.isDirectory()) {
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                }

                // 创建路径节点
                createPathNodes(path, fh.isDirectory(), fh.getFullUnpackSize(), nodeMap);
            }
        }
        return root;
    }

    private static void createPathNodes(String path, boolean isDirectory, long size,
                                        Map<String, ArchiveNode> nodeMap) {
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean isLastPart = (i == parts.length - 1);

            // 跳过空部分
            if (part.isEmpty()) continue;

            // 构建当前完整路径
            String parentPath = currentPath.toString();
            currentPath.append(part).append("/");
            String fullPath = currentPath.toString();

            // 如果是最后一部分且不是目录，则去掉末尾的斜杠
            if (isLastPart && !isDirectory) {
                fullPath = fullPath.substring(0, fullPath.length() - 1);
            }

            // 如果节点不存在则创建
            if (!nodeMap.containsKey(fullPath)) {
                boolean nodeIsDir = isDirectory || !isLastPart;
                long nodeSize = (nodeIsDir) ? 0 : size;

                ArchiveNode node = new ArchiveNode(part, fullPath, nodeIsDir, nodeSize);
                nodeMap.put(fullPath, node);

                // 添加到父节点
                ArchiveNode parent = nodeMap.get(parentPath);
                if (parent != null) {
                    parent.addChild(node);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            // 替换为您的压缩文件路径
            File archive = new File("D:\\iecas\\images\\airenv_redis.tar");
            ArchiveNode root = exploreArchive(archive);

            System.out.println("Archive structure:");
            System.out.println(root.toTreeString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
