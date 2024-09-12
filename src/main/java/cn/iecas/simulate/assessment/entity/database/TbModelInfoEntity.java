package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;



@Data
@Table(name = "tb_model_info")
public class TbModelInfoEntity implements Serializable{

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 用户层级
     */
    @Column(name = "user_level")
    private String userLevel;

    /**
     *模型所属领域
     */
    @Column(name = "field")
    private String field;

    /**
     *业务流程类型
     */
    @Column(name = "service_type")
    private String serviceType;

    /**
     *系统id
     */
    @Column(nullable = true,name = "system_id", columnDefinition = "int4 DEFAULT -1")
    private int systemId;

    /**
     *所属单位
     */
    @Column(name = "unit")
    private String unit;

    /**
     *描述信息
     */
    @Column(name = "describe", columnDefinition = "text")
    private String describe;

    /**
     *关键字
     */
    @Column(name = "keyword")
    private String keyword;

    /**
     *版本号
     */
    @Column(name = "version")
    private String version;

    /**
     *累计评估次数
     */
    @Column(nullable = true,name = "assessment_count")
    private int assessmentCount;

    /**
     *启用状态
     */
    @Column(name = "status", columnDefinition = "bool DEFAULT true")
    private Boolean status;

    /**
     *是否删除
     */
    @Column(name = "delete", columnDefinition = "bool DEFAULT true")
    private Boolean delete;

    /**
     * 模型标识
     */
    @Column(name = "sign", length = 255)
    private String sign;
}
