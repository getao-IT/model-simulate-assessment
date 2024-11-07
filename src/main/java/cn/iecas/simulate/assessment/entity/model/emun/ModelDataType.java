package cn.iecas.simulate.assessment.entity.model.emun;

import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import cn.iecas.simulate.assessment.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Locale;



/**
 * @auther getao
 * @date 2024/10/30 17:16
 * @description 模型数据类别枚举类
 */
@AllArgsConstructor
@NoArgsConstructor
public enum ModelDataType {

    MFHFX("MFHFX"),
    ZMDB("ZMDB-TASK");

    private String sign;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public SimulateDataService getModelDataTypeService() {
        SimulateDataService modelDataTypeService = null;
        try {
            modelDataTypeService = (SimulateDataService) SpringContextUtil.getBean(this.sign.toUpperCase(Locale.ROOT) + "-DATASERVICE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelDataTypeService;
    }
}
