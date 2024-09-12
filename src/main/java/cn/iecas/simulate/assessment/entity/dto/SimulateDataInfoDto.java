package cn.iecas.simulate.assessment.entity.dto;

import lombok.Data;
import java.io.Serializable;



@Data
public class SimulateDataInfoDto implements Serializable{
    private static final long serialVersionUID = -8134951071421682026L;

    /**
     *模型id
     */
    private int modelId;

    /**
     *仿真任务id
     */
    private int taskId;

    /**
     * 分页大小
     */
    private long pageSize = 10;

    /**
     * 当前页
     */
    private long pageNo = 1;

    /**
     * 查询内容
     */
    private String fuzzy;
}
