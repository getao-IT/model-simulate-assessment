package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;



public interface ModelShareService extends IService<ModelShareInfo> {

    /**
     * 共享模型评估记录
     * @param taskIdList 所要共享的评估记录的id列表
     * @return
     */
    Map<String, Object> share(List<Integer> taskIdList);


    /**
     * 根据id对共享评估记录进行删除
     * @param idList id列表
     */
    void delete(List<Integer> idList);


    /**
     * 条件查询模型分析信息
     * @param dto 条件
     * @return 结果
     */
    PageResult<ModelShareInfo> getShareModelInfo(ModelShareDTO dto);
}
