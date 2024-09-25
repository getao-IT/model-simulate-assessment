package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.database.TbSystemInfoEntity;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @auther getao
 * @date 2024/8/13
 * @description 系统信息持久类
 */
@Repository
public interface SysetemDao extends BaseMapper<SystemInfo> {
//    @Select("select distinct user_level from tb_system_info")
//    List<String> findUserLevels();

     @Select("select * from tb_system_info")
     List<SystemInfo> selectAllUserLevels();

     @Update("UPDATE tb_system_info set status=#{status} where id=#{id}")
     int updateStatusById(Long id, Boolean status);

     @Select("select id from tb_system_info where status='true'")
     List<Integer> findSystemStatus();
}
