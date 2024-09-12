package cn.iecas.simulate.assessment.entity.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @auther getao
 * @date 2024/8/30 9:12
 * @description 模型评估结果实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentResultInfo {

    private int taskId;

    private int modelId;

    private JSONObject firstIndex;

    private JSONObject secondIndex;

    private JSONObject threeIndex;

    private JSONObject fourIndex;

    private Double score;

    private int weight;

    private Double contibution;
}
