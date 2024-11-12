package cn.iecas.simulate.assessment.entity.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_model_share_info")
public class ModelShareInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 记录id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 仿真id
     */
    private Integer taskId;

    /**
     * 仿真任务名称
     */
    private String taskName;

    /**
     * 模型id
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 体系贡献率
     */
    private String contribution;

    /**
     * 总体评分
     */
    private String score;

    /**
     * 用户层级
     */
    private String userLevel;

    /**
     * 共享单位
     */
    private String unit;

    /**
     * 评估共享时间
     */
    private Date shareTime;

    /**
     * 删除位
     */
    private Boolean delete;

    /**
     * model_assessment_info对应的id
     */
    private Integer assessmentId;

    /**
     * 审核分数
     */
    private Double judgementScore;

    /**
     * 审核状态 0:未审核; 1:审核通过; 2:审核未通过
     */
    private Integer judgementStatus;

    /**
     * 评分人
     */
    private String judgementUser;

    /**
     * 评分人id
     */
    private Integer judgementUserId;

    /**
     * 评分时间
     */
    private Date judgementTime;

    /**
     * 共享人名称
     */
    private String shareUser;
}

