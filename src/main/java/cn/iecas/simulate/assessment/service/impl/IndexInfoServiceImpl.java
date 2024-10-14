package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.IndexInfoDao;
import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import cn.iecas.simulate.assessment.service.IndexInfoService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.*;



/***
 * @auther getao
 * @Date 2024/8/29 16:45
 * @Description 模型指标信息服务接口实现类
 */
@Service
public class IndexInfoServiceImpl extends ServiceImpl<IndexInfoDao, IndexInfo> implements IndexInfoService {

    @Autowired
    private IndexInfoDao indexInfoDao;


    @Override
    public JSONObject getIndexBySignAndBatchNo(String sign, int batchNo) {
        JSONObject result = new JSONObject();

        QueryWrapper<IndexInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sign", sign).eq("batch_no", batchNo).eq("level", 1);
        List<Object> firstIndexList = new ArrayList<>();
        List<IndexInfo> firstIndexInfos = this.indexInfoDao.selectList(wrapper);
        for (IndexInfo firstIndexInfo : firstIndexInfos) {
            Map<String, Object> firstIndex = new HashMap<>();
            firstIndex.put("indexInfo", firstIndexInfo);
            firstIndex.put("subIndexs", null);
            firstIndexList.add(firstIndex);
        }
        result.put("firstIndex", firstIndexList);

        wrapper = new QueryWrapper<>();
        wrapper.eq("sign", sign).eq("batch_no", batchNo).eq("level", 2);
        List<IndexInfo> secondIndexInfos = this.indexInfoDao.selectList(wrapper);

        List<Object> secondIndexList = new ArrayList<>();
        for (IndexInfo secondIndexInfo : secondIndexInfos) {
            Map<String, Object> secondIndex = new HashMap<>();
            wrapper = new QueryWrapper<>();
            wrapper.eq("sign", sign).eq("batch_no", batchNo).eq("level", 3)
                    .eq("parent_index_id", secondIndexInfo.getId());
            List<IndexInfo> thireIndexInfos = this.indexInfoDao.selectList(wrapper);
            List<Object> thireIndexList = new ArrayList<>();
            for (IndexInfo thireIndexInfo : thireIndexInfos) {
                Map<String, Object> thireIndex = new HashMap<>();
                wrapper = new QueryWrapper<>();
                wrapper.eq("sign", sign).eq("batch_no", batchNo).eq("level", 4)
                        .eq("parent_index_id", thireIndexInfo.getId());
                List<IndexInfo> fourIndexInfos = this.indexInfoDao.selectList(wrapper);
                thireIndex.put("indexInfo", thireIndexInfo);
                thireIndex.put("subIndexs", fourIndexInfos);
                thireIndexList.add(thireIndex);
            }
            secondIndex.put("indexInfo", secondIndexInfo);
            secondIndex.put("subIndexs", thireIndexList);
            secondIndexList.add(secondIndex);
        }
        result.put("otherIndex", secondIndexList);
        return result;
    }


    @Override
    public Map<String, Object> getIndexInfo(String sign) {
        List<IndexInfo> indices=indexInfoDao.selectBySign(sign);
        Map<String, Object> result = new HashMap<>();
        List<Object> firstIndexes = new ArrayList<>();
//        Map<String,Map<String,List<String>>> otherIndexes = new LinkedHashMap<>();
        Map<Integer,String> idToNameMap = new HashMap<>();
        Map<Integer, List<IndexInfo>> parentToChildrenMap = new HashMap<>();
        for (IndexInfo index:indices){
            idToNameMap.put(index.getId(),index.getIndexName());
            if(index.getLevel()==1){
                firstIndexes.add(index.getIndexName());
            }else{
                parentToChildrenMap.computeIfAbsent(index.getParentIndexId(),k->new ArrayList<>()).add(index);
            }
        }
        Map<String,Map<String,List<String>>> secondLevelMap = new LinkedHashMap<>();
        for (IndexInfo index:indices){
            if (index.getLevel()==2){
            Map<String,List<String>> thirdLevelMap = new LinkedHashMap<>();
                List<IndexInfo> thirdLevelIndices = parentToChildrenMap.get(index.getId());
                if(thirdLevelIndices!=null){
                    for (IndexInfo childIndex : thirdLevelIndices){
                        if(childIndex.getLevel()==3){
                            List<String> fourthLevelIndexes = new ArrayList<>();
                            List<IndexInfo> fourthLevelIndices=parentToChildrenMap.get(childIndex.getId());
                            if(fourthLevelIndices!=null){
                                for (IndexInfo fourthLevelIndex : parentToChildrenMap.get(childIndex.getId())){
                                    if(fourthLevelIndex.getLevel()==4){
                                        fourthLevelIndexes.add(fourthLevelIndex.getIndexName());
                                    }
                                }
                            }
                            thirdLevelMap.put(childIndex.getIndexName(),fourthLevelIndexes);
                        }
                    }
                }
            secondLevelMap.put(index.getIndexName(),thirdLevelMap);
            }
        }
        result.put("firstIndex",firstIndexes);
        result.put("otherIndex",secondLevelMap);
        return result;
    }


    @Override
    public IndexInfo insert(IndexInfo indexInfo) {
        int insert = this.indexInfoDao.insert(indexInfo);
        return indexInfo;
    }

    @Override
    public List<Map<String, Object>> getIndexInfoByLevel(int modelId, int batchNo) {
        return this.indexInfoDao.getIndexInfoByLevel(modelId, batchNo);
    }

    @Override
    public IndexInfo getIndexInfoById(int id) {
        return this.getById(id);
    }

    @Override
    public List<IndexInfo> getIndexInfoByQuery(QueryWrapper<IndexInfo> queryWrapper) {
        return this.list(queryWrapper);
    }
}
