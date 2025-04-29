package cn.iecas.simulate.assessment.service.test.service;

import cn.aircas.utils.file.FileUtils;
import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.impl.ExternalDataAccessServiceImpl;
import cn.iecas.simulate.assessment.service.test.pojo.IndexIndicatorTaskInfoBase;
import cn.iecas.simulate.assessment.service.test.pojo.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.test.pojo.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


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
                IndexIndicatorTaskInfoBase taskInfoBase = IndexIndicatorTaskInfoBase.builder().id(i).modelType(modeltype).taskname(taskName).specialIdent("no found data")
                        .status("no found data").process("no found data").participants("[]").startdate(new Date().toString()).enddate(new Date().toString())
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


    /**
     * @Description 获取模型运行数据
     * @Author getao
     * @Date 9:33 2025/3/14
     * @Param [path, way, pageSize, base]
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     */
    public JSONObject getRealData(int taskId, int modelId, String inputPath, String outputPath, String way, Integer pageSize) {
        File ipFile = FileUtils.getFile(inputPath);
        if (!ipFile.exists() || ipFile.isFile()) {
            log.info("{} 路径不存在或不是一个文件夹...", inputPath);
            return null;
        }

        File opFile = FileUtils.getFile(inputPath);
        if (!opFile.exists()) {
            opFile.mkdirs();
            log.info("====================>>{} 路径不存在，已创建...", outputPath);
        }

        List<File> fileList = Arrays.stream(ipFile.listFiles(((dir, name) -> FilenameUtils.isExtension(name,
                cn.iecas.simulate.assessment.util.FileUtils.getExtendsion())))).collect(Collectors.toList());
        String realDataId = taskId + "-" + modelId + "-" + inputPath;
        if (ExternalDataAccessServiceImpl.realDataPool.get(realDataId) == null) {
            JSONObject dataCache = new JSONObject();
            dataCache.put("fileList", fileList);
            dataCache.put("index", 0);
            dataCache.put("lastNum", pageSize);
            dataCache.put("groupCount", 0);
            ExternalDataAccessServiceImpl.realDataPool.put(realDataId, dataCache);
        }

        JSONObject result = new JSONObject();
        List<String> inputResult = new ArrayList<>();
        JSONObject dataCache = ExternalDataAccessServiceImpl.realDataPool.get(realDataId);
        inputResult = this.getFileList(way, dataCache, pageSize);
        List<String> outputResult = new ArrayList<>();
        outputResult = inputResult.stream().map(e -> (cn.iecas.simulate.assessment.util.FileUtils
                .replaceExtension(e.replace(inputPath, outputPath),"det.xml"))).collect(Collectors.toList());
        result.put("input", inputResult);
        result.put("output", outputResult);

        return result;
    }


    /**
     * @Description 根据上一次引接情况获取本次模型运行数据
     * @Author getao
     * @Date 10:02 2025/3/14
     * @Param []
     * @return java.util.List<java.lang.String>
     */
    private List<String> getFileList(String way, JSONObject dataCache, int pageSize) {
        List<String> fileList = dataCache.getJSONArray("fileList").toJavaList(String.class);
        Integer index = dataCache.getInteger("index");
        Integer lastNum = dataCache.getInteger("lastNum");
        int endIndex = index + lastNum;

        List<String> result = new ArrayList<>();
        this.getModelDataPaths(index, endIndex, result, fileList,  dataCache);
        if (way.equalsIgnoreCase("INCREASE")) {
            dataCache.put("lastNum", lastNum+pageSize);
        }
        if (way.equalsIgnoreCase("DECREASE")) {
            int currentLastNum = lastNum - pageSize;
            if (currentLastNum < 0) {
                currentLastNum = -lastNum;
            }
            if (currentLastNum == 0) {
                currentLastNum = pageSize;
            }
            dataCache.put("lastNum", currentLastNum);
        }
        if (way.equalsIgnoreCase("KEEP")) {
            dataCache.put("lastNum", lastNum);
        }

        log.info("==>> 本次引接数据数：{} 条", result.size());
        return result;
    }

    /**
     * @Description 递归获取模型运行数据
     * @Author getao
     * @Date 12:59 2025/3/14
     * @Param []
     * @return void
     */
    private int getModelDataPaths(int index, int endIndex, List<String> result, List<String> fileList, JSONObject dataCache) {
        if (endIndex > fileList.size()) {
            int surplus = endIndex - fileList.size();
            result.addAll(fileList.subList(index, fileList.size()));
            if (surplus > fileList.size()) {
                surplus = getModelDataPaths(0, surplus, result, fileList, dataCache);
                return surplus;
            }
            result.addAll(fileList.subList(0, surplus));
            dataCache.put("index", surplus);
        } else {
            result.addAll(fileList.subList(index, endIndex));
            dataCache.put("index", endIndex);
            return endIndex;
        }
        return -1;
    }
}
