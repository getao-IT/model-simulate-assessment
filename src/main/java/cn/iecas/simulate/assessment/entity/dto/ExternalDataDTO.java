package cn.iecas.simulate.assessment.entity.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import java.util.Date;



/**
 * @Time: 2024/10/9 10:22
 * @Author: guoxun
 * @File: ExternalDataDTO
 * @Description: 外部接引数据参数
 */
@Data
public class ExternalDataDTO {

    /**
     * 所要请求的地址
     */
    private String requestUrl;

    /**
     * 接入频率 次/分钟
     */
    private Integer frequency;

    /**
     * 数据总量：有多少组
     */
    private Integer groupCount;

    /**
     * 数据引接方式：INCREASE(递增)，DECREASE(递减)，KEEP(维持)
     */
    private String way = "INCREASE";

    /**
     * 数据频数：原仿真时值
     */
    private Integer pageSize;

    /**
     * modelId
     */
    private Integer modelId;

    private String creater;

    private String field;

    private String fuzzy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:ss:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:ss:mm")
    private String geTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:ss:mm")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:ss:mm")
    private String leTime;

    private String modelName;

    private String modelNameZh;

    private String orderCol;

    private String orderWay;

    private Integer pageNo;

    private String sceneName;

    private String taskInfoDto;

    private String taskName;

    private String taskType;

    private String userLevel;

    /**
     * 字段用于更新task的任务状态信息
     */
    private Integer taskId;

    /**************************府会分析**************************/
    /**
     * id
     */
    private Integer id;

    /**
     * 28所页码
     */
    private Integer pageNum;

    /**
     * 查询的起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date proposalTimeGre;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(hidden = true)
    private Date proposalTimeLess;

    /**
     * 议案类型
     */
    @ApiModelProperty(hidden = true)
    private String territory;

    /**************************中美力量对比**************************/
    /**
     * 中美力量对比获取任务信息标识字段
     */
    private String modelType;
}
