package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import javax.persistence.Column;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Data
@Entity
@Table(name = "tb_system_info")
public class TbSystemInfoEntity implements Serializable {

    /**
     * id
     */
    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO )
    private int id;

    /**
     * 用户层级
     */
    @Column(name = "user_level")
    private String userLevel;

    /**
     * 系统名称
     */
    @Column(name = "system_name")
    private String systemName;

    /**
     * 系统标识
     */
    @Column(name = "system_sign")
    private String systemSign;

    /**
     * 单位
     */
    @Column(name = "unit")
    private String unit;

    /**
     * 模型总数
     */
    @Column(nullable = true, name ="model_total", columnDefinition = "int4 DEFAULT 0")
    private int modelTotal;

    /**
     * 系统描述
     */
    @Column(name = "describe", columnDefinition = "text")
    private String describe;

    /**
     * 启用状态
     */
    @Column(columnDefinition = "bool DEFAULT true")
    private Boolean status;

    /**
     * 系统接入时间
     */

    @Column(name = "import_time", columnDefinition = "timestamp")
    private Date importTime;

    /**
     * 是否删除
     */
    @Column(columnDefinition = "bool DEFAULT false")
    private Boolean delete;

    /**
     * 系统ip
     */
    @Column(name = "system_ip")
    private String systemIp;
}
