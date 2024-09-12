package cn.iecas.simulate.assessment.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther chenyulin
 * @Date 2024/8/22 17:00
 * @Description 场景信息实体类DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_scene_info")
public class SceneInfoDto implements Serializable{
    private static final long serialVersionUID = -2964446726248906191L;

    /**
     * id
     */
    @ApiModelProperty(hidden = true)
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *场景名称
     */
    private String sceneName;

    /**
     *用户层级
     */
    private String userLevel;

    /**
     *场景适用领域
     */
    private String field;

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
     *场景制作者
     */
    private String creater;

    /**
     *建立时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date createTime;

    /**
     *修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date modifyTime;

    /**
     * 查询的建立起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date CleTime;

    /**
     * 查询建立结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date CgeTime;

    /**
     * 查询的建立起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date MleTime;

    /**
     * 查询建立结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date MgeTime;

    /**
     *是否删除
     */
    @ApiModelProperty(hidden = true)
    private Boolean delete;

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
     * 排序字段
     */
    @TableField(exist = false)
    private String sortField;

    /**
     * 排序方式
     */
    @TableField(exist = false)
    private String sortOrder;

    /**
     * 模糊查询字段
     */
    @TableField(exist = false)
    private String vague;
}
