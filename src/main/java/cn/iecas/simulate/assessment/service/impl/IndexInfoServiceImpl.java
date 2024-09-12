package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.IndexInfoDao;
import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import cn.iecas.simulate.assessment.service.IndexInfoService;
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
    public List<IndexInfo> getIndexBySignAndBatchNo(String sign, int batchNo) {
        QueryWrapper<IndexInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sign", sign).eq("batch_no", batchNo);
        List<IndexInfo> indexInfos = this.indexInfoDao.selectList(wrapper);
        return indexInfos;
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
}
