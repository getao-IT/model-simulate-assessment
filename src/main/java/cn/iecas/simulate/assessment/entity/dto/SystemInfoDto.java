package cn.iecas.simulate.assessment.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import springfox.documentation.annotations.ApiIgnore;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther getao
 * @Date 2024/8/21 14:53
 * @Description 信息系统实体类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfoDto implements Serializable {
    private static final long serialVersionUID = -3521208829064421601L;

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    private int id = -1;

    /**
     * 用户层级
     */
    private String userLevel;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统标识
     */
    private String systemSign;

    /**
     * 单位
     */
    private String unit;

    /**
     * 模型总数
     */
    @ApiModelProperty(hidden = true)
    private int modelTotal = -1;

    /**
     * 系统描述
     */
    @ApiModelProperty(hidden = true)
    private String describe;

    /**
     * 启用状态
     */
    @ApiModelProperty(hidden = true)
    private Boolean status;

    /**
     * 系统接入时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date importTime;

    /**
     * 是否删除
     */
    @ApiModelProperty(hidden = true)
    private Boolean delete;

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

    /*************************************查询字段*******************************************/
}
