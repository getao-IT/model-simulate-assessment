package cn.iecas.simulate.assessment.service.impl;


import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.SceneDao;
import cn.iecas.simulate.assessment.entity.domain.SceneInfo;
import cn.iecas.simulate.assessment.entity.dto.SceneInfoDto;
import cn.iecas.simulate.assessment.service.SceneService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;



/**
 * @auther cyl
 * @date 2024/8/22
 * @description 作战场景服务接口实现类
 */
@Service
public class SceneServiceImpl extends ServiceImpl<SceneDao, SceneInfo> implements SceneService {

    @Autowired
    private SceneDao sceneDao;


    @Override
    public void addSceneInfo(SceneInfo sceneInfo) {
        sceneInfo.setCreater("system");
        sceneInfo.setCreateTime(cn.iecas.simulate.assessment.util.DateUtils.getVariableTime(new Date(), 8));
        sceneInfo.setDelete(false);
        save(sceneInfo);
    }


    @Override
    public IPage<SceneInfo> getSceneInfo(SceneInfoDto sceneInfoDto) {
        Page<SceneInfo> page = new Page<>(sceneInfoDto.getPageNo(), sceneInfoDto.getPageSize());
        QueryWrapper<SceneInfo> queryWrapper = new QueryWrapper<>();
        if(sceneInfoDto.getSceneName()!=null){
            queryWrapper.eq("scene_name",sceneInfoDto.getSceneName());
        }
        if(sceneInfoDto.getUserLevel()!=null){
            queryWrapper.like("user_level",sceneInfoDto.getUserLevel());
        }
        if(sceneInfoDto.getField()!=null){
            queryWrapper.like("field",sceneInfoDto.getField());
        }
        if(sceneInfoDto.getCreater()!=null){
            queryWrapper.eq("creater",sceneInfoDto.getCreater());
        }
        //TODO
        if(sceneInfoDto.getCreateTime()!=null){
            queryWrapper.eq("create_time",sceneInfoDto.getCreateTime());
        }
        if(sceneInfoDto.getModifyTime()!=null){
            queryWrapper.eq("modify_time",sceneInfoDto.getModifyTime());
        }
        if(sceneInfoDto.getDescribe()!=null){
            queryWrapper.eq("describe",sceneInfoDto.getDescribe());
        }
        if(sceneInfoDto.getKeyword()!=null){
            queryWrapper.eq("keyword",sceneInfoDto.getKeyword());
        }
        queryWrapper.le(sceneInfoDto.getCleTime() != null, "create_time", sceneInfoDto.getCleTime()).
                ge(sceneInfoDto.getCgeTime() != null, "create_time", sceneInfoDto.getCgeTime()).
                le(sceneInfoDto.getMleTime() != null, "modify_time", sceneInfoDto.getMleTime()).
                ge(sceneInfoDto.getMgeTime() != null, "modify_time", sceneInfoDto.getMgeTime()).
                like(sceneInfoDto.getVague() != null, "CONCAT(describe, keyword)",sceneInfoDto.getVague());
        String sortFied=sceneInfoDto.getSortField();
        String sortOrder=sceneInfoDto.getSortOrder();
        if(sortFied!=null && sortOrder!=null){
            if("desc".equalsIgnoreCase(sortOrder)){
                queryWrapper.orderByDesc(sortFied);
            }else{
                queryWrapper.orderByAsc(sortFied);
            }
        }else{
            queryWrapper.orderByDesc("id");
        }
        return sceneDao.selectPage(page,queryWrapper);
    }


    @Override
    public void updateScene(SceneInfo sceneInfo) {
        UpdateWrapper<SceneInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",sceneInfo.getId());
        //根据提供的字段设置更新条件
        if (sceneInfo.getSceneName()!=null){
            updateWrapper.set("scene_name",sceneInfo.getSceneName());
        }
        if (sceneInfo.getUserLevel()!=null){
            updateWrapper.set("user_level",sceneInfo.getUserLevel());
        }
        if (sceneInfo.getField()!=null){
            updateWrapper.set("field",sceneInfo.getField());
        }
        if (sceneInfo.getDescribe()!=null){
            updateWrapper.set("describe",sceneInfo.getDescribe());
        }
        if (sceneInfo.getKeyword()!=null){
            updateWrapper.set("keyword",sceneInfo.getKeyword());
        }
        if (sceneInfo.getModifyTime()!=null){
            updateWrapper.set("assessment_count",sceneInfo.getModifyTime());
        }
        updateWrapper.set("modify_time", cn.iecas.simulate.assessment.util.DateUtils.getVariableTime(new Date(), 8));
        update(updateWrapper);
    }


    @Override
    public void deleteScene(List<Integer> ids) {
        sceneDao.deleteBatchIds(ids);
        UpdateWrapper<SceneInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("modify_time", DateUtils.nowDate());
    }


    /**
     * @Description 根据id获取场景信息
     * @auther getao
     * @Date 2024/9/9 10:16
     * @Param [sceneId]
     * @Return
     */
    @Override
    public SceneInfo getSceneInfoById(int sceneId) {
        return sceneDao.selectById(sceneId);
    }
}
