package cn.iecas.simulate.assessment.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;



/**
 * @auther guoxun
 * @Date 2024/8/27 17:05
 * @Description 模型仿真评估记录类DTO
 */
@Data
public class ModelAssessmentDto {

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 模型所属单位名称
     */
    private String unit;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 建立者
     */
    private String creater;

    /**
     * 评估时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(hidden = true)
    private Date createTime;

    /**
     * 评估状态
     */
    private String status;

    /**
     * 评估完成时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(hidden = true)
    private Date finishTime;

    /*************************************查询字段*******************************************/

    /**
     * 评估完成时间筛选条件 区间右侧值
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date leFinishTime;

    /**
     * 评估完成时间筛选条件 区间左侧值
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date geFinishTime;

    /**
     * 评估时间筛选条件 区间右侧值
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date leCreateTime;

    /**
     * 评估时间筛选条件 区间左侧值
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(exist = false)
    private Date geCreateTime;

    /**
     * 页面大小 默认为1
     */
    @TableField(exist = false)
    private Integer pageSize = 10;

    /**
     * 当前页 默认为10
     */
    @TableField(exist = false)
    private Integer pageNo = 1;

    /**
     * 排序字段
     */
    @TableField(exist = false)
    private String orderCol;

    /**
     * 排序方式
     */
    @TableField(exist = false)
    private String orderWay;
}
