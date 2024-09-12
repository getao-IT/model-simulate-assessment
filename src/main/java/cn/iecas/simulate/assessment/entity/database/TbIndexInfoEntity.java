package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Data
//@Entity
@Table(name = "tb_index_info")
public class TbIndexInfoEntity implements Serializable{

    /**
     * id
     */
    @Id
    @Column(name = "id",unique = true,columnDefinition ="serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 指标名
     */
    @Column(name = "index_name")
    private String indexName;

    /**
     * 指标级别
     */
    @Column(nullable = true)
    private int level;

    /**
     * 指标对应模型id
     */
    @Column(nullable = true,name = "model_id", columnDefinition = "int4 DEFAULT -1")
    private int  modelId;

    /**
     * 指标批次
     */
    @Column(nullable = true,name = "bach_no", columnDefinition = "int4 DEFAULT -1")
    private int  batchNo;

    /**
     * 指标建立者
     */
    @Column(name = "creater")
    private String creater;

    /**
     * 建立时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp", length = 6)
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "modify_time", columnDefinition = "timestamp", length = 6)
    private Date modifyTime;

    /**
     * 是否删除
     */
    @Column(nullable = true, columnDefinition = "bool DEFAULT false")
    private Boolean delete;

    /**
     * 模型标识
     */
    @Column(name = "sign", length = 255)
    private String sign;

    /**
     * 父级指标id
     */
    @Column(name = "parent_index_id", columnDefinition = "int4 DEFAULT -1")
    private int parentIndexId;
}
