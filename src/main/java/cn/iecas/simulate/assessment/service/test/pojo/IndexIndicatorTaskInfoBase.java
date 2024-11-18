package cn.iecas.simulate.assessment.service.test.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * @auther getao
 * @date 2024/10/30 11:13
 * @description TODO getao
 */
@Data
@Builder
public class IndexIndicatorTaskInfoBase {

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    @Column(name = "model_type", columnDefinition = "text")
    private String modelType;

    @Column(name = "taskname", columnDefinition = "text")
    private String taskname;

    @Column(name = "topic_type", columnDefinition = "text")
    private String topicType;

    @Column(name = "special_ident", columnDefinition = "text")
    private String specialIdent;

    @Column(name = "status", columnDefinition = "varchar(255)")
    private String status;

    @Column(name = "process", columnDefinition = "text")
    private String process;

    @Column(name = "participants", columnDefinition = "text")
    private String participants;

    @Column(name = "startdate", columnDefinition = "text")
    private String startdate;

    @Column(name = "enddate", columnDefinition = "text")
    private String enddate;

    @Column(name = "country_arr", columnDefinition = "text")
    private String countryArr;

    @Column(name = "zbxh", columnDefinition = "text")
    private String zbxh;

    @Column(name = "default_state", columnDefinition = "text")
    private String defaultState;

    @Column(name = "year_arr", columnDefinition = "text")
    private String yearArr;

    @Column(name = "image", columnDefinition = "text")
    private String image;

    @Column(name = "platform", columnDefinition = "text")
    private String platform;
}
