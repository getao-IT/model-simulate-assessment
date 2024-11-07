package cn.iecas.simulate.assessment.entity.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.List;



/**
 * @auther getao
 * @date 2024/10/30 11:13
 * @description 中美对比任务实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_index_indicator_task_info")
public class IndexIndicatorTaskInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    private int srcIndicatorTaskId;

    private int modelId;

    private int taskId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    private String modelType;

    private String taskname;

    private String topicType;

    private String specialIdent;

    private String status;

    private String process;

    private String participants;

    private String startdate;

    private String enddate;

    private String countryArr;

    private String zbxh;

    private String defaultState;

    private String yearArr;

    private String image;

    private String platform;

    @TableField(exist = false)
    private List<ZbCompareInfo> compareInfos;
}
