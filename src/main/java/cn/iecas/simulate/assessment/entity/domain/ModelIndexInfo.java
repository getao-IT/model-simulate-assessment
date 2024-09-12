package cn.iecas.simulate.assessment.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;



@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_index_info")
public class ModelIndexInfo implements Serializable {
    private static final long serialVersionUID = 668359380260634419L;

    /**
     * id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     * 指标名
     */
    private String indexName;

    /**
     * 指标级别
     */
    private int level;

    /**
     * 指标对应模型id
     */
    private int  modelId;

    /**
     * 指标批次
     */
    private int  batchNo;

    /**
     * 指标建立者
     */
    private String creater;

    /**
     * 建立时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 是否删除
     */
    private Boolean delete;

    /**
     * 模型标识
     */
    private String modelSign;
}

