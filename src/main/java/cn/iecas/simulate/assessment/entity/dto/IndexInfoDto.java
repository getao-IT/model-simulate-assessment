package cn.iecas.simulate.assessment.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;



/**
 * @auther chenyulin
 * @Date 2024/9/2 10:00
 * @Description 模型指标信息实体类DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tb_index_info")
public class IndexInfoDto implements Serializable{
    private static final long serialVersionUID = 3245623176942754463L;

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
    private String sign;

    /**
     * 父模型指标id
     */
    private Integer parentIndexId;
}
