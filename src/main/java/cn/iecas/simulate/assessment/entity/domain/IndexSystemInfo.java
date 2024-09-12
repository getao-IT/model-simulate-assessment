package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther chenyulin
 * @Date 2024/8/23 17:00
 * @Description 指标体系信息实体类DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_index_system_info")
public class IndexSystemInfo implements Serializable{
    private static final long serialVersionUID = -3931893600149317840L;

    /**
     * id
     */
    @TableId(value = "id",type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private int id;

    /**
     * 指标体系名称
     */
    private String indexSystemName;

    /**
     * 评估模型名称
     */
    @ApiModelProperty(hidden = true)
    private String modelName;

    /**
     * 一级指标名称集合
     */
    private String firstIndex;

    /**
     * 二级指标名称集合
     */
    private String secondIndex;

    /**
     * 三级指标名称集合
     */
    private String threeIndex;

    /**
     * 四级指标名称集合
     */
    private String fourIndex;

    /**
     * 建立者
     */
    @ApiModelProperty(hidden = true)
    private String creater;

    /**
     * 建立时间
     */
    @ApiModelProperty(hidden = true)
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(hidden = true)
    private Date modifyTime;

    /**
     * 是否为源体系
     */
    @ApiModelProperty(hidden = true)
    private Boolean source;

    /**
     * 是否删除
     */
    @ApiModelProperty(hidden = true)
    private Boolean delete;

    /**
     * 关联模型id
     */
    @ApiModelProperty(hidden = true)
    private int modelId;

    /**
     * 关联指标批次
     */
    @ApiModelProperty(hidden = true)
    private int batchNo;

    /**
     * 模型批次号
     */
    @ApiModelProperty(hidden = true)
    private String version;

    /**
     * 指标体系描述
     */
    private String describe;

    /**
     * 页面大小
     */
    @ApiModelProperty(hidden = true)
    @TableField(exist = false)
    private int pageSize = 10;

    /**
     * 当前页
     */
    @TableField(exist = false)
    @ApiModelProperty(hidden = true)
    private int pageNo = 1;

    /**
     * 模糊查询字段
     */
    @TableField(exist = false)
    @ApiModelProperty(hidden = true)
    private String vague;
}