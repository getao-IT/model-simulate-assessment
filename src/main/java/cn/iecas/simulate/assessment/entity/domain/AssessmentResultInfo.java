package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_assessment_result_info")
public class AssessmentResultInfo implements Serializable{

    /**
     * id
     */
    @Id
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 任务id
     */
    private int  taskId;

    /**
     * 模型id
     */
    private int  modelId;

    /**
     * 模型运行数据id
     */
    private int  runDataId;

    /**
     * 数据编号
     */
    private int dataNo;

    /**
     * 当前数据编号
     */
    private int currentNo;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 指标评估
     */
    private String value;

    /**
     * 建立时间
     */
    private Date createTime;

    /**
     * 评估状态
     */
    private Boolean status;
}
