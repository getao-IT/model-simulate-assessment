package cn.iecas.simulate.assessment.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulateTaskInfoDto implements Serializable{
    private static final long serialVersionUID = 1843137685881154469L;

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    private int id;

    /**
     * 任务名称
     */
    private String taskName;

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
    @ApiModelProperty(hidden = true)
    private int sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 模型id集合
     */
    @ApiModelProperty(hidden = true)
    private String modelId;

    /**
     * 模型名称集合
     */
    private String modelName;

    /**
     * 创建人
     */
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
     * 状态
     */
    @ApiModelProperty(hidden = true)
    private String status;

    /**
     * 描述
     */
    @ApiModelProperty(hidden = true)
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

    /*************************************查询字段*******************************************/

    /**
     * 分页大小
     */
    private long pageSize = 10;

    /**
     * 当前页
     */
    private long pageNo = 1;

    /**
     * 排序字段
     */
    private String orderCol;

    /**
     * 排序方式
     */
    private String orderWay;

    /**
     * 查询的起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leTime;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date geTime;

    /**
     * 模糊查询字段
     */
    private String fuzzy;

    /**
     * 查询的起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date proposalTimeGre;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date proposalTimeLess;

    /**
     * 议案类型
     */
    @ApiModelProperty(hidden = true)
    private String territory;

    /**
     * 仿真指令类型：
     *     开始：START
     *     暂停：PAUSE
     *     结束：END
     *     结束：CANCEL
     */
    @ApiModelProperty(hidden = true)
    private String simulateOp;

    /*************************************查询字段*******************************************/
}
