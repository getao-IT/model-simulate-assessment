package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.dao.SysetemDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.SystemInfoDto;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.SystemService;
import cn.iecas.simulate.assessment.util.DateUtils;
import cn.iecas.simulate.assessment.util.UserUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private UserUtils userUtils;


    @Override
    public PageResult<SystemInfo> getSystemInfo(SystemInfoDto systemInfoDto) {
        JSONObject userInfoByToken = userUtils.getUserJsonInfoByToken();
        Boolean isAdmin = userInfoByToken.getBoolean("is_admin");
        Boolean isSuperAdmin = userInfoByToken.getBoolean("is_super_admin");
        IPage<SystemInfo> page = new Page<>(systemInfoDto.getPageNo(), systemInfoDto.getPageSize());
        QueryWrapper<SystemInfo> wrapper = new QueryWrapper<>();
        wrapper.like(systemInfoDto.getUserLevel() != null, "user_level", systemInfoDto.getUserLevel())
                .like(systemInfoDto.getSystemName() != null, "system_name", systemInfoDto.getSystemName())
                .like(systemInfoDto.getSystemSign() != null, "system_sign", systemInfoDto.getSystemSign())
                .like(systemInfoDto.getUnit() != null, "unit", systemInfoDto.getUnit())
                .like(systemInfoDto.getDescribe() != null, "describe", systemInfoDto.getDescribe())
                .le(systemInfoDto.getLeTime() != null, "import_time", systemInfoDto.getLeTime())
                .ge(systemInfoDto.getGeTime() != null, "import_time", systemInfoDto.getGeTime())
                .like(systemInfoDto.getFuzzy() != null, "CONCAT(user_level, system_name, system_sign" +
                        ",unit, describe)", systemInfoDto.getFuzzy())
                .orderByDesc(systemInfoDto.getOrderCol() == null, "import_time")
                .orderByDesc(systemInfoDto.getOrderCol() != null
                && systemInfoDto.getOrderWay().equalsIgnoreCase("desc"), systemInfoDto.getOrderCol())
                .orderByAsc(systemInfoDto.getOrderCol() != null
                && systemInfoDto.getOrderWay().equalsIgnoreCase("asc"), systemInfoDto.getOrderCol());
        if (!isAdmin && !isSuperAdmin){
            if (userInfoByToken.getInteger("id") == null)
                throw new RuntimeException("未获取到当前登录用户id");
            Integer uid = userInfoByToken.getInteger("id");
            wrapper.eq("uid", uid).or().eq("is_visible", true);
        }
        IPage<SystemInfo> systemInfos = systemDao.selectPage(page, wrapper);
        return new PageResult<>(systemInfos.getCurrent(), systemInfos.getTotal(), systemInfos.getRecords());
    }


    @Override
    public SystemInfo saveSystemInfo(SystemInfo systemInfo) {
        QueryWrapper<SystemInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("system_sign", systemInfo.getSystemSign());
        List<SystemInfo> systemInfos = this.systemDao.selectList(queryWrapper);
        if (systemInfos.size() > 0) {
            return null;
        }
        systemInfo.setStatus(true);
        systemInfo.setDelete(false);
        systemInfo.setImportTime(DateUtils.getVariableTime(new Date(), 8));
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
            case "军委":
                return "军委";
            case "战区":
                return "战区";
            case "军兵种":
                return "军兵种";
            case "一线信息系统":
                return "一线信息系统";
            default:
                return "未知";
        }
    }


    @Override
    public boolean updateModelStatus(Long id, Boolean status) {
        JSONObject userJsonInfoByToken = userUtils.getUserJsonInfoByToken();
        if (!userJsonInfoByToken.getBoolean("is_admin") && !userJsonInfoByToken.getBoolean("is_super_admin")){
            return false;
        }
        return systemDao.updateStatusById(id, status)>0;
    }

    @Override
    public List<Integer> findSystemStatus() {
        List<Integer> result=systemDao.findSystemStatus();
        return result;
    }


    @Override
    @Transactional
    public void updateSystemVisible(Long id, Boolean visible) {
        JSONObject userInfoByToken = userUtils.getUserJsonInfoByToken();
        SystemInfo systemInfo = baseMapper.selectById(id);
        if (userInfoByToken.getBoolean("is_admin") || userInfoByToken.getBoolean("is_super_admin")
                || Long.parseLong(String.valueOf(userInfoByToken.getInteger("id"))) == systemInfo.getUid()){
            LambdaUpdateChainWrapper<SystemInfo> updateChainWrapper = new LambdaUpdateChainWrapper<>(baseMapper);
            updateChainWrapper.eq(SystemInfo::getId, id).set(SystemInfo::getIsVisible, visible);
            if (systemInfo.getStatus() && !visible) {
                updateChainWrapper.set(SystemInfo::getStatus, visible).update();
                UpdateWrapper<TbModelInfo> wrapper = new UpdateWrapper<>();
                wrapper.eq("system_id", id).set("status", visible).set("is_visible", visible);
                this.modelService.updateByWrapper(wrapper);
            } else {
                updateChainWrapper.update();
            }
        }
        else {
            throw new RuntimeException("当前登录用户无修改权限");
        }
    }
}
