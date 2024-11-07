package cn.iecas.simulate.assessment.entity.database;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "tb_model_share_info")
public class TBModelShareInfo implements Serializable {

    /**
     * 记录id
     */
    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 仿真id
     */
    @Column(name = "task_id")
    private Integer taskId;

    /**
     * 仿真任务名称
     */
    @Column(name = "task_name")
    private String taskName;

    /**
     * 模型id
     */
    @Column(name = "model_id")
    private String modelId;

    /**
     * 模型名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 任务类型
     */
    @Column(name = "task_type")
    private String taskType;

    /**
     * 体系贡献率
     */
    @Column(name = "contribution")
    private String contribution;

    /**
     * 总体评分
     */
    @Column(name = "score")
    private String score;

    /**
     * 用户层级
     */
    @Column(name = "user_level")
    private String userLevel;

    /**
     * 共享单位
     */
    @Column(name = "unit")
    private String unit;

    /**
     * 评估共享时间
     */
    @Column(name = "share_time", columnDefinition = "timestamp")
    private Date shareTime;

    /**
     * 删除位
     */
    @Column(name = "delete", columnDefinition = "bool DEFAULT false")
    private Boolean delete;
}

