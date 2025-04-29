package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;



public interface ModelShareService extends IService<ModelShareInfo> {


    /**
     * 共享模型评估记录
     * @param idList 所要共享的评估记录的id列表
     * @return
     */
    Map<String, Object> share(List<Integer> idList);


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

    /**
     * 统计评估总数
     * @return
     */
    Integer getAssessmentTotal();

    /**
     * 统计模型总数
     * @return
     */
    Integer getModelTotal();

    /**
     * 统计评估的模型种类及其百分比
     * @return
     */
    List<ModelInfo> getModelAssessmentType();

    /**
     * 根据模型类别统计数量
     * @return
     */
    List<Map<String, Object>> getServiceTypeByType();

    /**
     * 统计模型所占百分比
     * @return
     */
    List<Map<String, Object>> getModelPercent();


    /**
     * 根据id给共享评估记录判分
     * @param id 共享评估记录主键
     * @param judgementScore 分数
     * @param judgementStatus 是否审核通过 0 未审核 1 通过 2 未通过
     * @return
     */
    ModelShareInfo judgementById(Integer id, Double judgementScore, Integer judgementStatus);


    /**
     * 条件查询已经评判的共享模型记录
     * @param dto 查询条件
     * @return
     */
    PageResult<ModelShareInfo> getJudgementShareModelInfo(ModelShareDTO dto);
}
