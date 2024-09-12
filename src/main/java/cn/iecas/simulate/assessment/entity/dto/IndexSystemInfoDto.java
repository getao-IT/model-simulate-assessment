package cn.iecas.simulate.assessment.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Table;
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
@Table(name="tb_index_system_info")
public class IndexSystemInfoDto implements Serializable{
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
    private String modelName;

    /**
     * 一级指标名称集合
     */
    @ApiModelProperty(hidden = true)
    private String firstIndex;

    /**
     * 二级指标名称集合
     */
    @ApiModelProperty(hidden = true)
    private String secondIndex;

    /**
     * 三级指标名称集合
     */
    @ApiModelProperty(hidden = true)
    private String threeIndex;

    /**
     * 四级指标名称集合
     */
    @ApiModelProperty(hidden = true)
    private String fourIndex;

    /**
     * 建立者
     */
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
     * 模型id
     */
    @ApiModelProperty(hidden = true)
    private Integer modelId;

    /**
     * 批次号
     */
    @ApiModelProperty(hidden = true)
    private Integer batchNo;

    /**
     * 模型批次号
     */
    @ApiModelProperty(hidden = true)
    private String version;

    /**
     * 页面大小
     */
    @TableField(exist = false)
    private int pageSize = 10;

    /**
     * 当前页
     */
    @TableField(exist = false)
    private int pageNo = 1;

    /**
     * 模糊查询字段
     */
    @TableField(exist = false)
    private String vague;
}