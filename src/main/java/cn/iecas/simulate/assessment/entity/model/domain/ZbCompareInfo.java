package cn.iecas.simulate.assessment.entity.model.domain;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;



/**
 * @auther getao
 * @date 2024/10/30 11:13
 * @description 中美对比详情实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_zb_compare_info")
public class ZbCompareInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private int modelId;

    private int taskId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    private int indicatorTaskId;

    private String countryCn;

    private String score;

    private String name;

    private String rank;

    private String countryName;

    private String value;

    private String info;

    private String nationFlag;

    private JSONArray children;
}
