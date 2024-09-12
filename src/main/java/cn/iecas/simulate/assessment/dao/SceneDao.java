package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.SceneInfo;
import cn.iecas.simulate.assessment.entity.dto.SceneInfoDto;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;



/**
 * @auther cyl
 * @date 2024/8/22
 * @description 作战场景信息持久类
 */
@Repository
public interface SceneDao extends BaseMapper<SceneInfo> {

    IPage<SceneInfoDto> selectPage(Page<SceneInfoDto> page, @Param("ew") QueryWrapper<SceneInfo> queryWrapper);

//    int updateStatusById( @RequestParam("id") Long id, @RequestParam(value = "status") Boolean status);
//    void updateById(TbModelInfoEntity tbModelInfoEntity);
}
