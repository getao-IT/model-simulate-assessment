package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentProcessInfo implements Serializable{

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 任务id
     */
    private int taskId;

    /**
     * 指标对应模型id
     */
    private int modelId;

    /**
     * 指标批次
     */
    private int batchNo;

    /**
     * 指标id
     */
    private int indexId;

    /**
     * 父级指标id
     */
    private int parentIndexId;

    /**
     * 指标评估结果
     */
    private double result;

    /**
     * 原始指标id
     */
    private int sourceIndexId;

    /**
     *  任务UUID
     */
    private String assessmentUuid;
}
