package cn.iecas.simulate.assessment.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



/**
 * 加载jar包
 */
public class ClassLoaderUtils extends ClassLoader{

    private String jarPath;


    public ClassLoaderUtils(String jarPath) {
        this.jarPath = jarPath;
    }


    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            byte[] bytes = this.loadByteFromJar(jarPath, className);
            return defineClass(className, bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private byte[] loadByteFromJar(String jarPath, String className) throws IOException {
        String suffix = "BOOT-INF/classes/";
        String entryName = suffix + className.replace(".", "/") + ".class";
        File file = new File(jarPath);
        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry = jarFile.getJarEntry(entryName);
        InputStream is = jarFile.getInputStream(jarEntry);
        int len = 0;
        byte[] bf = new byte[1024];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while ((len=is.read(bf)) != -1) {
            os.write(bf, 0, len);
        }
        os.close();
        is.close();
        return os.toByteArray();
    }
}
