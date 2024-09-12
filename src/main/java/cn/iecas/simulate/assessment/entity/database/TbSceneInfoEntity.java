package cn.iecas.simulate.assessment.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;



@Entity
@Data
@Table(name = "tb_scene_info")
public class TbSceneInfoEntity implements Serializable{

    @Id
    @Column(name = "id", unique = true, columnDefinition = "serial4")
    @TableId(value = "id",type = IdType.AUTO)
    private int id;

    /**
     *场景名称
     */
    @Column(name = "scene_name")
    private String sceneName;

    /**
     *用户层级
     */
    @Column(name = "user_level")
    private String userLevel;

    /**
     *场景适用领域
     */
    @Column(name = "field")
    private String field;

    /**
     *描述信息
     */
    @Column(name = "describe", columnDefinition = "text")
    private String describe;

    /**
     *关键字
     */
    @Column(name = "keyword")
    private String keyword;

    /**
     *场景制作者
     */
    @Column(name = "creater")
    private String creater;

    /**
     *建立时间
     */
    @Column(name = "create_time", columnDefinition = "timestamp", length = 6)
    private Date createTime;

    /**
     *修改时间
     */
    @Column(name = "modify_time", columnDefinition = "timestamp", length = 6)
    private Date modifyTime;

    /**
     *是否删除
     */
    @Column(name = "delete", columnDefinition = "bool DEFAULT true")
    private Boolean delete;
}
