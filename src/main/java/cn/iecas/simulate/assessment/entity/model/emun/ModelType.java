package cn.iecas.simulate.assessment.entity.model.emun;

import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Locale;



/**
 * @auther getao
 * @date 2024/10/30 17:16
 * @description 模型类别枚举类
 */
@AllArgsConstructor
@NoArgsConstructor
public enum ModelType {

    MFHFX("MFHFX"),
    ZMDB("ZMDB"),
    DETECTION("DETECTION"),
    DATAANALYSE("DATAANALYSE");

    private String sign;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public ModelTypeService getModelTypeService() {
        ModelTypeService modelTypeService = null;
        try {
            modelTypeService = (ModelTypeService) SpringContextUtil.getBean(this.sign.toUpperCase(Locale.ROOT) + "-SERVICE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelTypeService;
    }
}
