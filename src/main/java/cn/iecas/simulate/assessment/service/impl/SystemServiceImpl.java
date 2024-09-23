package cn.iecas.simulate.assessment.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.SysetemDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.database.TbSystemInfoEntity;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.dto.SystemInfoDto;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.SystemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.*;


/**
 * @auther getao
 * @Date 2024/8/21 15:08
 * @Description 信息系统服务接口实现类
 */
@Service
public class SystemServiceImpl extends ServiceImpl<SysetemDao, SystemInfo> implements SystemService {

    @Autowired
    private SysetemDao systemDao;

    @Autowired
    private ModelService modelService;


    @Override
    public PageResult<SystemInfo> getSystemInfo(SystemInfoDto systemInfoDto) {
        IPage<SystemInfo> page = new Page<>(systemInfoDto.getPageNo(), systemInfoDto.getPageSize());
        QueryWrapper<SystemInfo> wrapper = new QueryWrapper<>();
        wrapper.eq(systemInfoDto.getUserLevel() != null, "user_level", systemInfoDto.getUserLevel())
                .eq(systemInfoDto.getSystemName() != null, "system_name", systemInfoDto.getSystemName())
                .eq(systemInfoDto.getSystemSign() != null, "system_sign", systemInfoDto.getSystemSign())
                .eq(systemInfoDto.getUnit() != null, "unit", systemInfoDto.getUnit())
                .eq(systemInfoDto.getDescribe() != null, "describe", systemInfoDto.getDescribe())
                .le(systemInfoDto.getLeTime() != null, "import_time", systemInfoDto.getLeTime())
                .ge(systemInfoDto.getGeTime() != null, "import_time", systemInfoDto.getGeTime())
                .like(systemInfoDto.getFuzzy() != null, "CONCAT(user_level, system_name, system_sign" +
                        ",unit, describe)", systemInfoDto.getFuzzy())
                .orderByDesc(systemInfoDto.getOrderCol() != null
                && systemInfoDto.getOrderWay().equalsIgnoreCase("desc"), systemInfoDto.getOrderCol())
                .orderByAsc(systemInfoDto.getOrderCol() != null
                && systemInfoDto.getOrderWay().equalsIgnoreCase("asc"), systemInfoDto.getOrderCol());
        IPage<SystemInfo> systemInfos = systemDao.selectPage(page, wrapper);
        return new PageResult<>(systemInfos.getCurrent(), systemInfos.getTotal(), systemInfos.getRecords());
    }


    @Override
    public SystemInfo saveSystemInfo(SystemInfo systemInfo) {
        systemInfo.setStatus(true);
        systemInfo.setDelete(false);
        systemInfo.setImportTime(DateUtils.nowDate());
        int insert = systemDao.insert(systemInfo);
        return systemInfo;
    }


    @Override
    public SystemInfo updateSystemInfo(SystemInfo systemInfo) {
        LambdaUpdateChainWrapper<SystemInfo> update = new LambdaUpdateChainWrapper<>(this.systemDao);
        if (systemInfo.getId() != -1) {
            boolean flag = update.eq(SystemInfo::getId, systemInfo.getId())
                    .set(systemInfo.getUserLevel() != null, SystemInfo::getUserLevel, systemInfo.getUserLevel())
                    .set(systemInfo.getSystemName() != null, SystemInfo::getSystemName, systemInfo.getSystemName())
                    .set(systemInfo.getSystemSign() != null, SystemInfo::getSystemSign, systemInfo.getSystemSign())
                    .set(systemInfo.getUnit() != null, SystemInfo::getUnit, systemInfo.getUnit())
                    .set(systemInfo.getModelTotal() != -1, SystemInfo::getModelTotal, systemInfo.getModelTotal())
                    .set(systemInfo.getDescribe() != null, SystemInfo::getDescribe, systemInfo.getDescribe())
                    .update();
        }
        return systemInfo;
    }


    @Override
    @Transactional
    public Integer batchDeleteSystemInfo(List<Integer> idList) {
        int delete = this.systemDao.deleteBatchIds(idList);
        // 暂时不需要同步删除模型 TODO getao
        /*for (Integer id : idList) {
            modelService.deleteModelBySystemId(id);
        }*/
        return delete;
    }

    @Override
    public List<Map<String,String>> findUserLevels() {
        List<SystemInfo> userLevels = systemDao.selectAllUserLevels();
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (SystemInfo userLevel : userLevels) {
            String key = userLevel.getUserLevel(); // 使用 getUserLevel 作为 key
            String value = getKeyFromUserLevel(userLevel.getUserLevel());
            if (!keys.contains(key)) {
                Map<String, String> map = new HashMap<>();
                map.put(key, value); // value 为中文
                result.add(map);
                keys.add(key);
            }
        }
        return result;
}


    private String getKeyFromUserLevel(String userLevel) {
        switch (userLevel) {
            case "JW":
                return "军委";
            case "ZQ":
                return "战区";
            case "JBZ":
                return "军兵种";
            case "YXXXXT":
                return "一线信息系统";
            default:
                return "UNKNOWN";
        }
    }


    @Override
    public boolean updateModelStatus(Long id, Boolean status) {
        return systemDao.updateStatusById(id, status)>0;
    }

    @Override
    public List<Integer> findSystemStatus() {
        List<Integer> result=systemDao.findSystemStatus();
        return result;
    }
}
