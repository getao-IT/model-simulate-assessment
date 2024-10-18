package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;



@Data
@TableName(value = "tb_simulate_data_info")
public class SimulateDataInfo implements Serializable{
    private static final long serialVersionUID = 4424981711340769481L;

    @Id
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     *模型id
     */
    private int modelId;

    /**
     *仿真任务id
     */
    private int taskId;

    /**
     *议案id
     */
    private String billId;

    /**
     *议案标题
     */
    private String title;

    /**
     *中文议案标题
     */
    private String titleZh;

    /**
     *描述
     */
    private String direction;

    /**
     *议案类型
     */
    private String territory;

    /**
     *
     */
    private Date replaceTime;

    /**
     *提案时间
     */
    private Date proposalTime;

    /**
     *
     */
    private Date passThrough;

    /**
     *关键字
     */
    private String keyword;

    /**
     *共同提案人
     */
    private String coProposer;

    /**
     *委员会
     */
    private String committee;

    /**
     *数据引入时间
     */
    private Date importTime;

    /**
     *  数据所属类别
     */
    private String type;
}
