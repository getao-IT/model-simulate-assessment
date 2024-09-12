package cn.iecas.simulate.assessment.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther chenyulin
 * @Date 2024/8/27 18:00
 * @Description 模型仿真评估统计实体类DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_model_assessment_statistic_info")
public class ModelAssessmentStatisticInfoDto implements Serializable {
    private static final long serialVersionUID = 7537694034458823982L;

    /**
     * id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型评估记录id
     */
    private int modelAssessmentId;

    /**
     *仿真任务id
     */
    private int taskId;

    /**
     *任务耗时
     */
    private Date timeConsuming;

    /**
     *仿真数据总条数
     */
    private int simulateDataCount;

    /**
     *仿真数据平均引接数
     */
    private int avgImportNum;

    /**
     *时频调整次数
     */
    private int frequencyAdjustNum;

    /**
     *数据引接频率
     */
    private int importFrequency;
}
