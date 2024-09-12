package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;



@Data
@Entity
@Table(name = "tb_model_assessment_statistic_info")
public class TbModelAssessmentStatisticInfoEntity implements Serializable{

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型评估记录id
     */
    @Column
    private String modelAssessmentId;

    /**
     *仿真任务id
     */
    @Column(nullable = true, columnDefinition = "int4 DEFAULT -1")
    private int taskId;

    /**
     *任务耗时
     */
    @Column(name = "time_consuming")
    private String timeConsuming;

    /**
     *仿真数据总条数
     */
    @Column(nullable = true)
    private int simulateDataCount;

    /**
     *仿真数据平均引接数
     */
    @Column(name = "avg_import_num",columnDefinition = "NUMERIC(10,2)")
    private Double avgImportNum;

    /**
     *时频调整次数
     */
    @Column(nullable = true)
    private int frequencyAdjustNum;

    /**
     * 数据引接次数
     */
    @Column(nullable = true)
    private int callCount;

    /**
     *数据引接频率
     */
    @Column(nullable = true,columnDefinition = "NUMERIC(10,2)")
    private Double importFrequency;
}
