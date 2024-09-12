package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Data
@Entity
@Table(name = "tb_model_assessment_info")
public class TbModelAssessmentInfoEntity implements Serializable{

    @Id
    @Column(name = "id",unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 仿真任务id
     */
    @Column(nullable = true, columnDefinition = "int4 DEFAULT -1")
    private int taskId;

    /**
     *模型id集合
     */
    @Column(nullable = true, columnDefinition = "int4 DEFAULT -1")
    private int modelId;

    /**
     *场景id
     */
    @Column(nullable = true, columnDefinition = "int4 DEFAULT -1")
    private int sceneId;

    /**
     *场景名称
     */
    @Column(name = "scene_name")
    private String sceneName;

    /**
     *模型所属单位名称
     */
    @Column(name = "unit")
    private String unit;

    /**
     *模型名称集合
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     *建立者
     */
    @Column(name = "creater")
    private String creater;

    /**
     *评估时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp", length = 6)
    private Date createTime;

    /**
     *评估状态
     */
    @Column(name = "status")
    private String status;

    /**
     *评估完成时间
     */
    @Column(name = "finish_time", columnDefinition = "timestamp", length = 6)
    private Date finishTime;
}
