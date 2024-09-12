package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Table;
import java.io.Serializable;



/**
 * @auther chenyulin
 * @Date 2024/8/21 17:00
 * @Description 模型信息实体类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_model_info")
public class TbModelInfo implements Serializable{
    private static final long serialVersionUID = 8437811674694865104L;

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型名称
     */
    private String modelName;

    /**
     * 用户层级
     */
    private String userLevel;

    /**
     *模型所属领域
     */
    private String field;

    /**
     *业务流程类型
     */
    private String serviceType;

    /**
     *系统id
     */
    @ApiModelProperty(hidden = true)
    private int systemId;

    /**
     *所属单位
     */
    private String unit;

    /**
     *描述信息
     */
    @ApiModelProperty(hidden = true)
    private String describe;

    /**
     *关键字
     */
    @ApiModelProperty(hidden = true)
    private String keyword;

    /**
     *版本号
     */
    @ApiModelProperty(hidden = true)
    private String version;

    /**
     *累计评估次数
     */
    @ApiModelProperty(hidden = true)
    private int assessmentCount;

    /**
     *启用状态
     */
    @ApiModelProperty(hidden = true)
    private Boolean status;

    /**
     *是否删除
     */
    @ApiModelProperty(hidden = true)
    private Boolean delete;

    /**
     * 模型标识
     */
    @ApiModelProperty(hidden = true)
    private String sign;

    /**
     * 页面大小
     */
    @TableField(exist = false)
    @JsonIgnore
    private int pageSize = 10;

    /**
     * 当前页
     */
    @TableField(exist = false)
    @JsonIgnore
    private int pageNo = 1;

    /**
     * 排序字段
     */
    @TableField(exist = false)
    @JsonIgnore
    private String sortField;

    /**
     * 排序方式
     */
    @TableField(exist = false)
    @JsonIgnore
    private String sortOrder;

    /**
     * 模糊查询字段
     */
    @TableField(exist = false)
    @JsonIgnore
    private String vague;
}
