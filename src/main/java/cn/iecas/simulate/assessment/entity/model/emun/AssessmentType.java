package cn.iecas.simulate.assessment.entity.model.emun;

import cn.iecas.simulate.assessment.service.model.AssessmentService;
import cn.iecas.simulate.assessment.util.SpringContextUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Locale;



/**
 * @auther getao
 * @date 2024/10/30 17:16
 * @description 模型评估类别枚举类
 */
@AllArgsConstructor
@NoArgsConstructor
public enum AssessmentType {

    MFHFX("MFHFX"),
    ZMDB("ZMDB");

    private String sign;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public AssessmentService getAssessmentTypeService() {
        AssessmentService dataAnalysisService = null;
        try {
            dataAnalysisService = (AssessmentService) SpringContextUtil.getBean(this.sign.toUpperCase(Locale.ROOT) + "-ASMTSERVICE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataAnalysisService;
    }
}
