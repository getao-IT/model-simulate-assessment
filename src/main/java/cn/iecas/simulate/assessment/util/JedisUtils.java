package cn.iecas.simulate.assessment.util;


import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.*;
import java.nio.charset.StandardCharsets;



/**
 * Jedis工具类
 */
@Component
public class JedisUtils {

    @Autowired
    private JedisPool jedisPool;

    private Jedis jedis = null;


    /**
     * 返回一个Jedis实例
     *
     * @return
     */
    public Jedis getInstance() {
        jedis = jedisPool.getResource();
        jedis.select(1);
        return jedis;
    }


    /**
     * 收回Jedis实例
     *
     * @param jedis
     */
    public void takebackJedis(Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedis.close();
        }
    }


    //##########################################################
    // STRING类型
    //##########################################################
    /**
     * 根据key获取Value
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return jedis.get(key);
    }


    /**
     * 添加键值对
     *
     * @param key
     * @param value
     * @return
     */
    public String set(String key, String value) {
        // NX是不存在才set，XX是存在才set，EX是秒，PX是毫秒
        return jedis.set(key, value, "NX", "EX", 1800);
    }


    /**
     * 删除一个或多个key
     *
     * @param keys
     * @return
     */
    public Long del(String... keys) {
        return jedis.del(keys);
    }


    //##########################################################
    // Java对象
    //##########################################################
    /**
     * 根据key获取Value
     *
     * @param key
     * @return
     */
    public Object getJavaObject(String key, Class c) {
        String objectStr = get(key);
        //return deObjectSerialize(objectStr.getBytes(StandardCharsets.UTF_8));
        return jsonStrToObject(objectStr, c);
    }


    /**
     * JavaObject：添加键值对
     * @param key
     * @param object
     * @return
     */
    public String setJavaObject(String key, Object object) {
        //byte[] bytes = objectSerialize(object);
        return set(key, objectToJsonStr(object));
    }


    /**
     * 根据key获取Value
     *
     * @param key
     * @return
     */
    public Object getJavaObjectByByte(String key) {
        String objectStr = get(key);
        return deObjectSerialize(objectStr.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 添加键值对
     * @param key
     * @param object
     * @return
     */
    public String setJavaObjectByByte(String key, Object object) {
        byte[] bytes = objectSerialize(object);
        return set(key, new String(bytes));
    }


    /**
     * java对象转为JSON字符串
     * @param object
     * @return
     */
    public String objectToJsonStr(Object object) {
        return JSONObject.toJSONString(object);
    }


    /**
     * JSON字符串转为java对象
     * @param jsonStr
     * @param c
     * @return
     */
    public Object jsonStrToObject(String jsonStr, Class c) {
        return JSONObject.parseObject(jsonStr, c);
    }


    /**
     * java对象序列化后存入redis
     * @param object
     * @return
     */
    public byte[] objectSerialize(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * byte反序列化为Java对象
     * @param bytes
     * @return
     */
    public Object deObjectSerialize(byte[] bytes) {
        ObjectInputStream ois = null;
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
