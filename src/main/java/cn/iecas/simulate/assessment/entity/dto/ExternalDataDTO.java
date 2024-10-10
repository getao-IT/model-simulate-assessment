package cn.iecas.simulate.assessment.entity.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


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

    private String orderCol;

    private String orderWay;

    private Integer pageNo;

    /**
     * 仿真时值
     */
    private Integer pageSize;

    private String sceneName;

    private String taskInfoDto;

    private String taskName;

    private String taskType;

    private String userLevel;

    /**
     * 字段用于更新task的任务状态信息
     */
    private Integer taskId;

}
