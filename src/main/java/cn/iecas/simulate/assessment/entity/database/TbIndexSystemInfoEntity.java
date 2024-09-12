package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Entity
@Data
@Table(name = "tb_index_system_info")
public class TbIndexSystemInfoEntity implements Serializable{

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 指标体系名称
     */
    @Column(name = "index_system_name")
    private String indexSystemName;

    /**
     * 评估模型名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 一级指标名称集合
     */
    @Column(name = "first_index")
    private String firstIndex;

    /**
     * 二级指标名称集合
     */
    @Column(name = "second_index")
    private String secondIndex;

    /**
     * 三级指标名称集合
     */
    @Column(name = "three_index")
    private String threeIndex;

    /**
     * 四级指标名称集合
     */
    @Column(name = "four_index")
    private String fourIndex;

    /**
     * 建立者
     */
    @Column(name = "creater")
    private String creater;

    /**
     * 建立时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp")
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "modify_time", columnDefinition = "timestamp")
    private Date modifyTime;

    /**
     * 是否为源体系
     */
    @Column(name = "source")
    private Boolean source;

    /**
     * 是否删除
     */
    @Column(name = "delete", columnDefinition = "bool DEFAULT false")
    private Boolean delete;

    /**
     * 模型id
     */
    @Column(name = "model_id", columnDefinition = "int4 DEFAULT -1")
    private Integer modelId;

    /**
     * 批次号
     */
    @Column(name = "batch_no", columnDefinition = "int4 DEFAULT -1")
    private Integer batchNo;

    /**
     * 模型批次号
     */
    @Column(name = "version")
    private String version;

    /**
     * 指标体系描述
     */
    @Column(name = "describe")
    private String describe;
}
