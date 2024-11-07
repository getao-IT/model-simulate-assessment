/**
 * @Time: 2024/10/30 10:55
 * @Author: guoxun
 * @File: ShareModelDTO
 * @Description:
 */

package cn.iecas.simulate.assessment.entity.dto;


import lombok.Data;

import java.util.Date;

@Data
public class ModelShareDTO {

    /**
     * 分页大小
     */
    private Integer pageSize = 10;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 单位名称
     */
    private String unit;

    /**
     * 模糊查询
     */
    private String fuzzy;

    /**
     * 最大时间
     */
    private Date leTime;

    /**
     * 最小时间
     */
    private Date geTime;

    /**
     * 排序字段
     */
    private String orderCol;

    /**
     * 排序方式
     */
    private String orderWay;
}
