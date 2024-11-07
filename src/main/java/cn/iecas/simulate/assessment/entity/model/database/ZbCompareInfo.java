package cn.iecas.simulate.assessment.entity.model.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Data
@Table(name = "tb_zb_compare_info")
public class ZbCompareInfo {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 模型id
     */
    @Column(nullable = true,name ="model_id", columnDefinition = "int4 DEFAULT -1")
    private int modelId;

    /**
     * 仿真任务id
     */
    @Column(nullable = true,name = "task_id", columnDefinition = "int4 DEFAULT -1")
    private int taskId;

    /**
     * 数据引入时间
     */
    @Column(name = "import_time", columnDefinition = "timestamp", length = 6)
    private Date importTime;

    /**
     * 对比任务id
     */
    @Column(name = "indicator_task_id", columnDefinition = "int4 DEFAULT -1")
    private int indicatorTaskId;

    @Column(name = "country_cn", columnDefinition = "text")
    private String countryCn;

    @Column(name = "score", columnDefinition = "text")
    private String score;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "rank", columnDefinition = "text")
    private String rank;

    @Column(name = "country_name", columnDefinition = "text")
    private String countryName;

    @Column(name = "value", columnDefinition = "text")
    private String value;

    @Column(name = "info", columnDefinition = "text")
    private String info;

    @Column(name = "nation_flag", columnDefinition = "text")
    private String nationFlag;

    @Column(name = "children", columnDefinition = "text")
    private String children;
}
