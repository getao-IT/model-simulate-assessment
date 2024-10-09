package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;



@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_simulate_task_info")
public class SimulateTaskInfo implements Serializable{
    private static final long serialVersionUID = 1843137685881154469L;

    /**
     * id
     */
    @TableId(value = "id",type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private int id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 单位名称
     */
    private String unit;

    /**
     * 用户层级
     */
    private String userLevel;

    /**
     * 所属领域
     */
    private String field;

    /**
     * 场景id
     */
    private int sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 模型id集合
     */
    private String modelId;

    /**
     * 模型名称集合
     */
    private String modelName;

    /**
     * 模型权重集合
     */
    private String modelWeight;

    /**
     * 创建人
     */
    @ApiModelProperty(hidden = true)
    private String creater;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date createTime;

    /**
     * 任务评估完成时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date finishTime;

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
    @ApiModelProperty(hidden = true)
    private String status;

    /**
     * 描述
     */
    private String describe;

    /**
     * 是否删除
     */
    @ApiModelProperty(hidden = true)
    private Boolean delete;

    /**
     * 任务类型：仿真评估：FZPG
     *     跨域多级评估：KYDJPG
     *     跨域多级场景实例化部署：CJSLH
     */
    private String taskType;

    /**
     * 指标体系id
     */
    private String indexSystemId;
}
