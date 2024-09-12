package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Entity
@Data
@Table(name = "tb_simulate_task_info")
public class TbSimulateTaskInfoEntity implements Serializable{

    /**
     * id
     */
    @Id
    @Column(name = "id",unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 任务名称
     */
    @Column(name = "task_name")
    private String taskName;

    /**
     * 用户层级
     */
    @Column(name = "user_level")
    private String userLevel;

    /**
     * 所属领域
     */
    @Column(name = "field")
    private String field;

    /**
     * 场景id
     */
    @Column(nullable = true,name = "scene_id", columnDefinition = "int4 DEFAULT -1")
    private int sceneId;

    /**
     * 场景名称
     */
    @Column(name = "scene_name")
    private String sceneName;

    /**
     * 模型id集合
     */
    @Column(name = "model_id")
    private String modelId;

    /**
     * 模型名称集合
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 模型权重集合
     */
    @Column(name = "model_weight")
    private String modelWeight;

    /**
     * 创建人
     */
    @Column(name = "creater")
    private String creater;

    /**
     * 创建时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp", length = 6)
    private Date createTime;

    /**
     * 任务评估完成时间
     */
    @Column(name = "finish_time", columnDefinition = "timestamp", length = 6)
    private Date finishTime;

    /**
     * 状态
     */
    @Column(name = "status")
    private String status;

    /**
     * 描述
     */
    @Column(name = "describe", columnDefinition = "text")
    private String describe;

    /**
     * 是否删除
     */
    @Column(name = "delete", columnDefinition = "bool DEFAULT false")
    private Boolean delete;

    /**
     * 任务类型：仿真评估：FZPG
     *     跨域多级评估：KYDJPG
     *     跨域多级场景实例化部署：CJSLH
     */
    @Column(name ="task_type")
    private String taskType;

    /**
     * 指标体系id
     */
    @Column
    private String indexSystemId;
}
