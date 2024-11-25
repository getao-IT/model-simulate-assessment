package cn.iecas.simulate.assessment.service.test.service;

import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.test.pojo.IndexIndicatorTaskInfoBase;
import cn.iecas.simulate.assessment.service.test.pojo.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.test.pojo.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Slf4j
@Service
public class DataTestServiceImpl {


    public JSONObject getSimulateData(SimulateTaskInfoDto simulateTaskInfoDto) {
        JSONObject result = new JSONObject();
        try {
            int total = 1000;
            QueryWrapper<SimulateDataInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_name", simulateTaskInfoDto.getModelName());

            List<SimulateDataInfo> records = new ArrayList<>();
            long pageSize = simulateTaskInfoDto.getPageSize();
            long pageNum = simulateTaskInfoDto.getPageNum();
            for (long i = (pageSize * pageNum - pageSize + 1); i <= total ; i++) {
                SimulateDataInfo dataInfo = SimulateDataInfo.builder().id(i).billId("BILLS-11859").title("德州府会议案分析en")
                        .titleZh("德州府会议案分析").direction("美国德克萨斯州政党议员分析").territory("税收").replaceTime(new Date())
                        .proposalTime(new Date()).passThrough(new Date()).keyword("key1,word2").coProposer("共同提案人1、共同提案人2")
                        .committee("德州委员会").importTime(new Date()).build();
                records.add(dataInfo);
                if (records.size() >= pageSize) {
                    break;
                }
            }

            log.info("本次引接的数据 第 {} 页，每页 {} 条数，实际 {} 条 ......",
                    pageNum, pageSize, pageSize);
            result.put("code", 200);
            result.put("message", "获取数据成功");
            result.put("success", true);
            JSONObject data = new JSONObject();
            data.put("dataList", records);
            data.put("billCount", total);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取数据失败");
            result.put("success", true);
            JSONObject data = new JSONObject();
            data.put("dataList", new ArrayList<>());
            data.put("billCount", 0);
            result.put("data", data);
        }

        return result;
    }


    public JSONObject getAllIndexIndicatorTask(String modeltype, Integer pageSize, Integer pageNum) {
        JSONObject result = new JSONObject();
        try {
            int total = 3;
            QueryWrapper<IndexIndicatorTaskInfoBase> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("model_type", modeltype);

            List<IndexIndicatorTaskInfoBase> records = new ArrayList<>();
            for (int i = (pageSize * pageNum - pageSize + 1); i <= total ; i++) {
                String taskName = i == 1 ? "中美军力实力对比" : (i == 2 ? "中美影响力对比" : "中美综合实力对比");
                IndexIndicatorTaskInfoBase taskInfoBase = IndexIndicatorTaskInfoBase.builder().id(i).modelType(modeltype).taskname(taskName).specialIdent("没有哦")
                        .status("没有哦").process("没有哦").participants("[]").startdate(new Date().toString()).enddate(new Date().toString())
                        .countryArr("[{\"alpha3\":\"USA\",\"countryCn\":\"美国\"},{\"alpha3\":\"CHN\",\"countryCn\":\"中国\"}]")
                        .zbxh("00601").defaultState("1").yearArr("[2022,2023,2024]").image("C:/default").platform("0").build();
                records.add(taskInfoBase);
            }

            result.put("code", 200);
            result.put("message", "获取数据成功");
            result.put("success", true);
            JSONObject data = new JSONObject();
            data.put("dataList", records);
            data.put("billCount", 3);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取数据失败");
            result.put("success", false);
            JSONObject data = new JSONObject();
            data.put("dataList", new ArrayList<>());
            data.put("billCount", 0);
            result.put("data", data);
        }

        return result;
    }


    public JSONObject queryZbCompare(int taskId, String countryCn) {
        JSONObject result = new JSONObject();
        try {
            String taskname = taskId == 1 ? "中美军力实力对比" : (taskId == 2 ? "中美影响力对比" : "中美综合国力对比");
            ZbCompareInfo templateInfo = ZbCompareInfo.builder().score("100").name(taskname).rank("2").countryName(countryCn).value("500").info("500")
                    .nationFlag("Base64").children(new JSONArray()).build();

            JSONArray children = new JSONArray();
            for (int i = 0; i < 10; i++) {
                ZbCompareInfo element = new ZbCompareInfo();
                BeanUtils.copyProperties(templateInfo, element);
                element.setName(element.getName()+"-方向"+i);
                element.setScore(String.valueOf(new Random().nextInt(50) + 50));
                element.setInfo(String.valueOf(new Random().nextInt(50) + 50));
                element.setValue(String.valueOf(new Random().nextInt(50) + 50));
                JSONArray childrens = new JSONArray();
                for (int i1 = 0; i1 < 5; i1++) {
                    ZbCompareInfo subElement = new ZbCompareInfo();
                    BeanUtils.copyProperties(element, subElement);
                    subElement.setName(subElement.getName()+"-属性"+i1);
                    subElement.setScore(String.valueOf(new Random().nextInt(50) + 50));
                    subElement.setInfo(String.valueOf(new Random().nextInt(50) + 50));
                    subElement.setValue(String.valueOf(new Random().nextInt(50) + 50));
                    childrens.add(subElement);
                }
                element.setChildren(childrens);
                children.add(element);
            }
            templateInfo.setChildren(children);

            result.put("code", 200);
            result.put("message", "获取数据成功");
            result.put("success", true);
            result.put("data", templateInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取数据失败");
            result.put("success", false);
            JSONObject data = new JSONObject();
            data.put("dataList", new ArrayList<>());
            data.put("billCount", 0);
            result.put("data", data);
        }

        return result;
    }
}
