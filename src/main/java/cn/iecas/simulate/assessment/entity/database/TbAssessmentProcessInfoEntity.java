package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;



@Data
@Entity
@Table(name = "tb_assessment_process_info")
public class TbAssessmentProcessInfoEntity implements Serializable{

    /**
     * id
     */
    @Id
    @Column(name = "id", unique = true, columnDefinition ="serial4")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 任务id
     */
    @Column(name = "task_id")
    private int taskId;

    /**
     * 指标对应模型id
     */
    @Column(nullable = true, name = "model_id", columnDefinition = "int4 DEFAULT -1")
    private int modelId;

    /**
     * 指标批次
     */
    @Column(nullable = true, name = "bach_no", columnDefinition = "int4 DEFAULT -1")
    private int batchNo;

    /**
     * 指标id
     */
    @Column(nullable = true, name = "index_id", columnDefinition = "int4 DEFAULT -1")
    private int indexId;

    /**
     * 父级指标id
     */
    @Column(nullable = true, name = "parent_index_id", columnDefinition = "int4 DEFAULT -1")
    private int parentIndexId;

    /**
     * 指标评估结果
     */
    @Column(nullable = true, name = "result", columnDefinition = "numeric DEFAULT -1")
    private double result;

    /**
     * 原始指标id
     */
    @Column(name = "source_index_id", columnDefinition = "int4 DEFAULT -1")
    private int sourceIndexId;

    /**
     *  任务UUID
     */
    @Column(name = "assessment_uuid", columnDefinition = "int4 DEFAULT -1")
    private String assessmentUuid;
}
