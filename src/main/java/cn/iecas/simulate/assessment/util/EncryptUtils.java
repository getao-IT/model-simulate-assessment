package cn.iecas.simulate.assessment.util;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;



public class EncryptUtils {


    /**
     * base64加密方法
     * @return
     */
    public static String base64Decrypt(String value, int offset){
        String result = "";
        for (int index = 0; index < value.length(); index++) {
            result+=(char)(value.charAt(index) - offset);
        }
        result = new String(java.util.Base64.getDecoder().decode(result)).trim();

        return result;
    }


    public static List<Integer> decryptIdAndToInt(List<Object> ids){
        List<Integer> idList = new ArrayList<>();
        for (Object id : ids) {
            if(StringUtils.isBlank((CharSequence) id))
                continue;
            if (StringUtils.isNumeric(id.toString())) {
                idList.add(Integer.valueOf(id.toString()));
                continue;
            }
            idList.add(Integer.valueOf(base64Decrypt(id.toString(),3)));
        }
        return idList;
    }
}
