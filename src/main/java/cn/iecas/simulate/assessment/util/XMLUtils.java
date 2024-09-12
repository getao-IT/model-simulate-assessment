package cn.iecas.simulate.assessment.util;

import lombok.extern.slf4j.Slf4j;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;



@Slf4j
public class XMLUtils {


    /**
     * 将对象输出为xml文件
     * @param filePath
     * @return
     */
    public static boolean toXMLFile(String filePath, Object object){
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            FileWriter fw = new FileWriter(filePath);
            marshaller.marshal(object, fw);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            log.error("输出xml: {} 失败",filePath);
            return false;
        }
        return true;
    }


    /**
     * 将对象输出为xml字符串
     * @return
     */
    public static String toXMLString(Object object){
        StringWriter xmlString = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);

            marshaller.marshal(object, xmlString);
        } catch (JAXBException e) {
            e.printStackTrace();
            log.error("输出xml字符串失败");
        }
        return xmlString.toString();
    }


    /**
     * 读取xml文件并转换为labelObjectInfo
     * @param xmlFilePath
     * @return
     */
    @SuppressWarnings("unchecked")
    public static<T> T parseXMLFromFile(Class<T> clazz, String xmlFilePath){
        T xmlObject = null;
        try {
            FileReader fileReader = new FileReader(xmlFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            xmlObject = (T) unmarshaller.unmarshal(fileReader);
        } catch (JAXBException | FileNotFoundException e) {
            log.error(e.getMessage());
            log.error("xml文件转{} labelObjectInfo失败.",xmlFilePath);
        }
        return xmlObject;
    }


    /**
     * 将xml字符串转换为labelObjectInfo
     * @param xmlString
     * @return
     */
    @SuppressWarnings("unchecked")
    public static<T> T parseXMLFromString(String xmlString, Class<T> clazz){
        T xmlObject = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            xmlObject = (T) unmarshaller.unmarshal(new StringReader(xmlString));
        } catch (JAXBException e) {
            log.error(e.getMessage());
        }
        return xmlObject;
    }


    /**
     * 将输入流转换为xml
     * @param inputStream
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static<T> T parseXMLFromStream(InputStream inputStream, Class<T> clazz){
        T xmlObject = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            xmlObject = (T) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            log.error(e.getMessage());
        }
        return xmlObject;
    }
}
