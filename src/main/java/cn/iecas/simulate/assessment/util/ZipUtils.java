package cn.iecas.simulate.assessment.util;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



/**
 * @author vanishrain
 */
@Slf4j
public class ZipUtils {


    /**
     * 将文件夹压缩为zip
     * @param srcDir 源文件夹
     * @return 压缩文件的路径
     */
    public static String zipDirectory(String srcDir){
        log.info("开始压缩文件夹：{}",srcDir);
        byte[] buffer = new byte[1024];
        File srcFile = new File(srcDir);
        File destFile = Paths.get(srcFile.getParent(),srcFile.getName()+".zip").toFile();

        if (!srcFile.exists())
            return null;
        FileInputStream fileInputStream = null;
        try( FileOutputStream fileOutputStream = new FileOutputStream(destFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            int progress = 0;
            int fileCount = srcFile.listFiles().length;
            for (File file : srcFile.listFiles()) {
                fileInputStream = new FileInputStream(file);
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                int length = 0;
                while((length=fileInputStream.read(buffer))>0)
                    zipOutputStream.write(buffer,0,length);
                progress++;
                log.info("压缩文件完成：{}/{}",progress,fileCount);
            }
        } catch (IOException e) {
            log.error("压缩文件：{}出错",srcFile.getAbsolutePath());
            return null;
        }finally {
            if (fileInputStream!=null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("文件夹压缩结束：{}",srcDir);
        return destFile.getAbsolutePath();
    }


   /**
     * 压缩文件，指定输出流
     * @param srcDir 源文件(夹)路径
     * @param outputStream 输出流
     * @throws IOException
     */
    public static void toZip(String srcDir, OutputStream outputStream) throws IOException {
        File srcDirFile = new File(srcDir);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        compress(srcDirFile,zipOutputStream);
        zipOutputStream.close();
    }


   /**
    * 递归压缩文件
    * @param srcFile 源文件
    * @param zipOutputStream zip输出流
    * @throws IOException
    */
    public static void compress(File srcFile, ZipOutputStream zipOutputStream) throws IOException {
        File[] childFiles = srcFile.listFiles();
        for (File childFile : childFiles) {
            compressToZip(childFile,zipOutputStream,childFile.getName());
        }
    }


    public static void compressToZip(File srcFile, ZipOutputStream zipOutputStream, String inZipName) throws IOException {
        byte[] buffer = new byte[1024];
        if (srcFile.isFile()){
            int length = 0;
            zipOutputStream.putNextEntry(new ZipEntry(inZipName));
            FileInputStream fileInputStream = new FileInputStream(srcFile);

            while((length = fileInputStream.read(buffer))!=-1){
                zipOutputStream.write(buffer,0,length);
            }
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }else {
            zipOutputStream.putNextEntry(new ZipEntry(inZipName + "/"));
            zipOutputStream.closeEntry();

            File[] childFiles = srcFile.listFiles();
            for (File childFile : childFiles) {
                compressToZip(childFile,zipOutputStream,inZipName + "/" + childFile.getName());
            }
        }
    }


    public static void main(String[] args) {
        String dir = "C:\\Users\\dell\\Desktop\\测试结果截图\\HKZC-SDM-GN-030101";
        ZipUtils.zipDirectory(dir);
    }
}
