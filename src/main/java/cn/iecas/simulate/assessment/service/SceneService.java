package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.SceneInfo;
import cn.iecas.simulate.assessment.entity.dto.SceneInfoDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;



/**
 * @auther chenyulin
 * @date 2024/8/22
 * @description 作战场景服务接口类
 */
public interface SceneService {

    void addSceneInfo(SceneInfo sceneInfo);

    IPage<SceneInfo> getSceneInfo(SceneInfoDto sceneInfoDto);

    void updateScene(SceneInfo sceneInfo);

    void deleteScene(List<Integer> ids);

    SceneInfo getSceneInfoById(int sceneId);
}
