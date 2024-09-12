package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;



@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_model_assessment_info")
public class ModelAssessmentInfo implements Serializable{
    private static final long serialVersionUID = -1943318321605099984L;

    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 仿真任务id
     */
    private Integer taskId;

    /**
     * 模型id集合
     */
    private Integer modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 场景id
     */
    private int sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 模型所属单位名称
     */
    private String unit;

    /**
     * 建立者
     */
    private String creater;

    /**
     * 评估时间
     */
    private Date createTime;

    /**
     * 状态类型：
     *    未开始：WAIT
     *    数据引接中：RUN
     *    评估中：ASSESSMENT
     *    暂停：PAUSE
     *    结束：END
     *    取消：CANCEL
     *    失败：FAIL
     *    完成：FINISH
     */
    private String status;

    /**
     * 评估完成时间
     */
    private Date finishTime;
}
