package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_model_assessment_statistic_info")
public class AssessmentStatisticInfo implements Serializable{

    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型评估记录id
     */
    private String modelAssessmentId;

    /**
     *仿真任务id
     */
    private int taskId;

    /**
     *任务耗时
     */
    private String timeConsuming;

    /**
     *仿真数据总条数
     */
    private int simulateDataCount;

    /**
     *仿真数据平均引接数
     */
    private Double avgImportNum;

    /**
     *时频调整次数
     */
    private int frequencyAdjustNum;

    /**
     * 数据引接次数
     */
    private int callCount;

    /**
     *数据引接频率
     */
    private Double importFrequency;
}
