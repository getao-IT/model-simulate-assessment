package cn.iecas.simulate.assessment.listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;



@Slf4j
public class TomcatListener {

    static String KEY = md5("这个密码独一无二");
    static File file = new File("./.asd");
    static long canUserTime = 1000 * 60 * 60 * 24 * 30L;
    static long sleepTime = 1000 * 30L;


    public static void encryption(ConfigurableApplicationContext run) {
        if (KEY.equals(md5("陈佳良专用")))
            return;
        Thread thread = new Thread(()->
        {
            long time = System.currentTimeMillis();
            if (!file.exists()) {
                log.error("系统文件不存在，或已被删除");
                run.close();
            }

            while (checkUserDuration(time)) {
                try {
                    if (checkThreadDuration(time)) {
                        log.error("试用已到期");
                        break;
                    }
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            run.close();

        });
        thread.start();
    }


    private static boolean checkThreadDuration(long systemTime){
        long useTime = System.currentTimeMillis() - systemTime;
        log.info("进程已启动时长{}", useTime);
        return useTime > canUserTime;
    }


    private static boolean checkUserDuration(long time) {
//        InputStream inputStream = TomcatListener.class.getClassLoader().getResourceAsStream("asd");
        String createTime = readTxt(file);
        if(createTime == null || !binaryToString(createTime).contains(KEY)){
            if(createTime == null){
                log.error("系统文件被修改");
            }else{
                log.error("系统已被重启，秘钥不匹配");
            }
            return false;
        }

        String binaryToString = binaryToString(createTime);
        if(binaryToString.equals(KEY)){
            writeTxt(file , KEY + "-0-" + time);
            return true;
        }else {
            String[] split = binaryToString.split("-");
            long nowTime = System.currentTimeMillis();
            long useTime = nowTime - Long.parseLong(split[2]);
            log.info("已使用时长{}", useTime);
            if(useTime > canUserTime){
                log.error("系统已到期");
                return false;
            }
            if(useTime <Long.parseLong(split[1])){
                log.error("用户使用修改系统的方式重置试用期，已强制关闭");
                return false;
            }else {
                writeTxt(file , KEY + "-" + useTime + "-" +Long.parseLong(split[2]));
                return true;
            }
        }
    }


    private static String md5(String key){
        String bytesToHex =null;
        try{
            MessageDigest md =MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            bytesToHex = bytesToHex(digest);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        return bytesToHex;
    }


    private static String bytesToHex(byte[] bytes){
        StringBuilder hexString =new StringBuilder();
        for(byte b :bytes){
            String hex =Integer.toHexString(0xff & b);
            if(hex.length() == 1){
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private static String binaryToString(String binary){
        StringBuilder result =new StringBuilder();
        String[] split = binary.split("(?<=\\G.{8})");
        for (String str : split){
            int charCode = Integer.parseInt(str, 2);
            result.append((char)charCode);
        }
        return result.toString();
    }


    private static String readTxt(File file){
        String time =null;
        BufferedReader bufferedReader =null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            time = bufferedReader.readLine();
        }catch (IOException e){
            log.error("文件读取失败 {}", file);
        }finally {
            try{
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return time;
    }


    private static void writeTxt(File file, String txt){
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(stringToBinary(txt));
        }catch (IOException e){
            log.error("过期时长写入错误 {}", Date.valueOf(String.valueOf(System.currentTimeMillis())));
        }finally {
            try{
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    private static String stringToBinary(String str){
        StringBuilder result = new StringBuilder();
        char[] chars =  str.toCharArray();
        for(char ch : chars){
            result.append(String.format("%8s", Integer.toBinaryString(ch)).replaceAll(" ", "0"));
        }
        return  result.toString();
    }


    public static void main(String[] args) {
        //不启用
        //writeTxt(file , md5("陈佳良专用"));
        //写秘钥
        //writeTxt(file, KEY);

        // 试图更改.asd文件
        String timeBin = readTxt(new File("./.asd"));
        log.info("当前秘钥：{}", binaryToString(timeBin));
        String newTime = KEY + "-" + 0 + "-" + System.currentTimeMillis();
        writeTxt(file, newTime);
        log.info("更改后的秘钥：{}", binaryToString(readTxt(new File("./.asd"))));
    }
}
