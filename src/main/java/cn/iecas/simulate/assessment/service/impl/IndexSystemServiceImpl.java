package cn.iecas.simulate.assessment.service.impl;


import cn.aircas.utils.date.DateUtils;
import cn.aircas.utils.file.FileUtils;
import cn.iecas.simulate.assessment.dao.IndexSystemDao;
import cn.iecas.simulate.assessment.dao.ModelIndexDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.dto.IndexSystemInfoDto;
import cn.iecas.simulate.assessment.service.IndexInfoService;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import cn.iecas.simulate.assessment.util.CollectionsUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;



/**
 * @auther cyl
 * @date 2024/8/27
 * @description 指标体系服务接口实现类
 */
@Service
public class IndexSystemServiceImpl extends ServiceImpl<IndexSystemDao, IndexSystemInfo> implements IndexSystemService {

    @Autowired
    private IndexSystemDao indexSystemDao;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ModelIndexDao modelIndexDao;
    
    @Autowired
    private IndexInfoService indexInfoService;
    
    private SimulateTaskServiceImpl simulateTaskService;


    @Override
    public PageResult<IndexSystemInfo> getIndexSystemInfo(IndexSystemInfoDto indexSystemInfoDto) {
        IPage<IndexSystemInfo> page = new Page<>(indexSystemInfoDto.getPageNo(), indexSystemInfoDto.getPageSize());
        QueryWrapper<IndexSystemInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(indexSystemInfoDto.getIndexSystemName() != null, "index_system_name", indexSystemInfoDto.getIndexSystemName())
                .like(indexSystemInfoDto.getModelName() != null, "model_name", indexSystemInfoDto.getModelName())
                .eq(indexSystemInfoDto.getCreateTime() != null, "create_time", indexSystemInfoDto.getCreateTime())
                .eq(indexSystemInfoDto.getModifyTime() != null, "modify_time", indexSystemInfoDto.getModifyTime())
                .like(indexSystemInfoDto.getCreater() != null, "creater", indexSystemInfoDto.getCreater())
                .like(indexSystemInfoDto.getVague() != null, "CONCAT(first_index,second_index,three_index,four_index)", indexSystemInfoDto.getVague())
                .orderByDesc("create_time");
        IPage<IndexSystemInfo> indexSystemInfos = indexSystemDao.selectPage(page, queryWrapper);
        return new PageResult<>(indexSystemInfos.getCurrent(), indexSystemInfos.getTotal(), indexSystemInfos.getRecords());

//        if (indexSystemInfoDto.getIndexSystemName() != null) {
//            queryWrapper.eq("index_system_name", indexSystemInfoDto.getIndexSystemName());
//        }
//        if (indexSystemInfoDto.getModelName() != null) {
//            queryWrapper.eq("model_name", indexSystemInfoDto.getModelName());
//        }
//        if (indexSystemInfoDto.getCreateTime() != null) {
//            queryWrapper.eq("create_time", indexSystemInfoDto.getCreateTime());
//        }
//        if (indexSystemInfoDto.getModifyTime() != null) {
//            queryWrapper.eq("modify_time", indexSystemInfoDto.getModifyTime());
//        }
//        if (indexSystemInfoDto.getCreater() != null) {
//            queryWrapper.eq("creater", indexSystemInfoDto.getCreater());
//        }
//        queryWrapper.like(indexSystemInfoDto.getVague() != null, "CONCAT(first_index,second_index,three_index,four_index)", indexSystemInfoDto.getVague());
//        return indexSystemDao.selectPage(page, queryWrapper);
    }


    @Override
    public void addIndexSystemInfo(IndexSystemInfo indexSystemInfo) {
        this.addIndexSystemInfoNew(indexSystemInfo);
        /*Integer maxBatchNo=indexSystemDao.selectMaxBatchNoByModelId(indexSystemInfo.getModelId());
        int batchNo= (maxBatchNo==null)?1:maxBatchNo+1;
        Integer modelId=indexSystemInfo.getModelId();
        indexSystemInfo.setBatchNo(batchNo);
        indexSystemInfo.setCreater("system");
        indexSystemInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        indexSystemInfo.setSource(false);
        indexSystemInfo.setDelete(false);
        indexSystemInfo.setVersion(modelId+"-V"+batchNo+".0.0");
        save(indexSystemInfo);*/
    }


    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void addIndexSystemInfoNew(IndexSystemInfo indexSystemInfo) {
        int modelId = indexSystemInfo.getModelId();
        Integer batchNo = indexSystemDao.selectMaxBatchNoByModelId(modelId);
        int maxBatchNo = batchNo == null ? 1 : (batchNo+ 1);

        // 构建指标信息
        JSONObject indexInfos = CollectionsUtils.parseMapToJsonobject((Map)indexSystemInfo.getIndexInfos());
        JSONArray firstIndex = indexInfos.getJSONArray("firstIndex");
        for (Object index : firstIndex) {
            IndexInfo indexInfo = new JSONObject((Map<String, Object>) index).getJSONObject("indexInfo").toJavaObject(IndexInfo.class);
            indexInfo.setBatchNo(maxBatchNo);
            indexInfo.setModelId(modelId);
            indexInfo.setCreateTime(DateUtils.nowDate());
            indexInfo.setSourceIndexId(indexInfo.getId());
            IndexInfo insert = indexInfoService.insert(indexInfo);
        }
        JSONArray otherIndex = indexInfos.getJSONArray("otherIndex");
        this.inserOtherIndex(modelId, otherIndex, maxBatchNo, 0);

        // 构建指标体系
        List<Map<String, Object>> indexInfoByLevel = this.indexInfoService.getIndexInfoByLevel(modelId, maxBatchNo);
        for (Map<String, Object> objectMap : indexInfoByLevel) {
            switch (String.valueOf(objectMap.get("level"))) {
                case "1":
                    indexSystemInfo.setFirstIndex(String.valueOf(objectMap.get("stringAgg"))); break;
                case "2":
                    indexSystemInfo.setSecondIndex(String.valueOf(objectMap.get("stringAgg"))); break;
                case "3":
                    indexSystemInfo.setThreeIndex(String.valueOf(objectMap.get("stringAgg"))); break;
                case "4":
                    indexSystemInfo.setFourIndex(String.valueOf(objectMap.get("stringAgg"))); break;
            }
        }
        indexSystemInfo.setBatchNo(maxBatchNo);
        indexSystemInfo.setCreater("system");
        indexSystemInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        indexSystemInfo.setSource(false);
        indexSystemInfo.setDelete(false);
        indexSystemInfo.setVersion(modelId+"-V"+maxBatchNo+".0.0");
        save(indexSystemInfo);
    }


    @Override
    public void updateIndexSystemInfo(IndexSystemInfo indexSystemInfo) {
        UpdateWrapper<IndexSystemInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",indexSystemInfo.getId());
        //根据提供的字段设置更新条件
        if (indexSystemInfo.getIndexSystemName()!=null){
            updateWrapper.set("index_system_name",indexSystemInfo.getIndexSystemName());
        }
        if (indexSystemInfo.getFirstIndex()!=null){
            updateWrapper.set("first_index",indexSystemInfo.getFirstIndex());
        }
        if (indexSystemInfo.getSecondIndex()!=null){
            updateWrapper.set("second_index",indexSystemInfo.getSecondIndex());
        }
        if (indexSystemInfo.getThreeIndex()!=null){
            updateWrapper.set("three_index",indexSystemInfo.getThreeIndex());
        }
        if (indexSystemInfo.getFourIndex()!=null){
            updateWrapper.set("four_index",indexSystemInfo.getFourIndex());
        }
        if (indexSystemInfo.getModifyTime()!=null){
            updateWrapper.set("modify_time",indexSystemInfo.getModifyTime());
        }
        updateWrapper.set("modify_time", DateUtils.nowDate());
        update(updateWrapper);
    }


    @Override
    public void deleteIndexSystemInfo(@RequestParam List<Integer> ids) {
        indexSystemDao.deleteBatchIds(ids);
    }


    public void restore(String sqlFileName){
        InputStream inputStream  = null;
        InputStreamReader reader = null;
        ScriptRunner runner = null;
        try {
            runner = new ScriptRunner(dataSource.getConnection());
            String initPath = FileUtils.getStringPath(System.getProperty("user.dir"), "init");
            String sqlPath = FileUtils.getStringPath(initPath, sqlFileName) + ".sql";
            inputStream = new FileInputStream(sqlPath);
            reader = new InputStreamReader(inputStream, "utf-8");
            runner.runScript(reader);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e){
            throw new RuntimeException(e);
        }
    }


    @Transactional(readOnly = true)
    public void inserOtherIndex(int modelId, JSONArray indexInfo, int maxBatchNo, int parentIndexId){
        for (Object parentIndex : indexInfo) {
            JSONObject jsonIndex = new JSONObject((Map<String, Object>) parentIndex);
            IndexInfo parentIndexInfo = null;
            if (jsonIndex.getJSONObject("indexInfo") != null) {
                parentIndexInfo = jsonIndex.getJSONObject("indexInfo").toJavaObject(IndexInfo.class);
            } else {
                parentIndexInfo = jsonIndex.toJavaObject(IndexInfo.class);
            }
            parentIndexInfo.setModelId(modelId);
            parentIndexInfo.setBatchNo(maxBatchNo);
            parentIndexInfo.setParentIndexId(parentIndexId);
            parentIndexInfo.setCreateTime(DateUtils.nowDate());
            parentIndexInfo.setSourceIndexId(parentIndexInfo.getId());
            IndexInfo insert2 = indexInfoService.insert(parentIndexInfo);
            JSONArray subIndexs = null;
            if (jsonIndex.getJSONArray("subIndexs") == null) {
                continue;
            } else {
                subIndexs = jsonIndex.getJSONArray("subIndexs");
            }
            this.inserOtherIndex(modelId, subIndexs, maxBatchNo, parentIndexInfo.getId());
        }
    }


    @Override
    public IndexSystemInfo getById(int systemId) {
        return this.indexSystemDao.selectById(systemId);
    }


    @Override
    public List<String> getIndexList(Integer id) {
        IndexSystemInfo indexSystemInfo = indexSystemDao.selectById(id);
        List<String> list = new ArrayList<>();
        list.add(indexSystemInfo.getFirstIndex());
        list.add(indexSystemInfo.getSecondIndex());
        list.add(indexSystemInfo.getThreeIndex());
        list.add(indexSystemInfo.getFourIndex());
        return list;
    }


    @Override
    public List<Map<String, List<String>>> getIndexesByModelId(Integer modelId) {
        List<IndexSystemInfo> indexSystemInfos = indexSystemDao.selectByModelId(modelId);
        Map<String,List<String>> resultMap = new HashMap<>();
        List<String> firstIndexList = indexSystemInfos.stream()
                .flatMap(IndexSystemInfo -> Arrays.stream(IndexSystemInfo.getFirstIndex()
                .split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        List<String> secondIndexList = indexSystemInfos.stream()
                .flatMap(IndexSystemInfo -> Arrays.stream(IndexSystemInfo.getSecondIndex()
                .split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        List<String> threeIndexList = indexSystemInfos.stream()
                .flatMap(IndexSystemInfo -> Arrays.stream(IndexSystemInfo.getThreeIndex()
                .split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        List<String> fourIndexList = indexSystemInfos.stream()
                .flatMap(IndexSystemInfo -> Arrays.stream(IndexSystemInfo.getFourIndex()
                .split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        resultMap.put("firstIndex",firstIndexList);
        resultMap.put("secondIndex",secondIndexList);
        resultMap.put("threeIndex",threeIndexList);
        resultMap.put("fourIndex",fourIndexList);
        return Arrays.asList(resultMap);
    }
}
