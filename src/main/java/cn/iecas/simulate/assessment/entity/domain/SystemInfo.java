package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther getao
 * @Date 2024/8/14 11:13
 * @Description 信息系统实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_system_info")
public class SystemInfo implements Serializable {
    private static final long serialVersionUID = 2868665040122423572L;

    /**
     * id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 用户层级
     */
    private String userLevel;

    /**
     * 系统名称
     */
    private String systemIp;

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
    private int modelTotal;

    /**
     * 系统描述
     */
    private String describe;

    /**
     * 启用状态
     */
    private Boolean status;

    /**
     * 系统接入时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    /**
     * 是否删除
     */
    private Boolean delete;
}
