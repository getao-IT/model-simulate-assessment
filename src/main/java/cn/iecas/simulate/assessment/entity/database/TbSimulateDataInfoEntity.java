package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Entity
@Data
@Table(name = "tb_simulate_data_info")
public class TbSimulateDataInfoEntity implements Serializable{

    @Id
    @Column(name = "id",unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *模型id
     */
    @Column(nullable = true,name ="model_id", columnDefinition = "int4 DEFAULT -1")
    private int modelId;

    /**
     *仿真任务id
     */
    @Column(nullable = true,name = "task_id", columnDefinition = "int4 DEFAULT -1")
    private int taskId;

    /**
     *议案id
     */
    @Column(name="bill_id")
    private String billId;

    /**
     *议案标题
     */
    @Column(name = "title")
    private String title;

    /**
     *中文议案标题
     */
    @Column(name = "title_zh")
    private String titleZh;

    /**
     *描述
     */
    @Column(name = "direction", columnDefinition = "text")
    private String direction;

    /**
     *议案类型
     */
    @Column(name = "territory")
    private String territory;

    /**
     *
     */
    @Column(name = "replace_time", columnDefinition = "timestamp", length = 6)
    private Date replaceTime;

    /**
     *提案时间
     */
    @Column(name = "proposal_time", columnDefinition = "timestamp", length = 6)
    private Date proposalTime;

    /**
     *
     */
    @Column(name ="pass_through", columnDefinition = "timestamp", length = 6)
    private Date passThrough;

    /**
     *关键字
     */
    @Column(name = "keyword")
    private String keyword;

    /**
     *共同提案人
     */
    @Column(name = "co_proposer")
    private String coProposer;

    /**
     *委员会
     */
    @Column(name = "committee")
    private String committee;

    /**
     *数据引入时间
     */
    @Column(name = "import_time", columnDefinition = "timestamp", length = 6)
    private Date importTime;
}
