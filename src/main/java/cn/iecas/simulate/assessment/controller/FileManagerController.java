package cn.iecas.simulate.assessment.controller;


import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.ResultCodeEnum;
import cn.iecas.simulate.assessment.entity.domain.FileInfo;
import cn.iecas.simulate.assessment.entity.dto.FilePathDTO;
import cn.iecas.simulate.assessment.entity.dto.SaveFilePathAndContentDTO;
import cn.iecas.simulate.assessment.entity.dto.UploadFileDTO;
import cn.iecas.simulate.assessment.service.FileManagerService;
import cn.iecas.simulate.assessment.util.FileUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @Time: 2024/9/6 10:02
 * @Author: guoxun
 * @File: LocalFileManagerController
 * @Description:
 */
@RestController
@Slf4j
@RequestMapping("/file")
public class FileManagerController {

    @Autowired
    FileManagerService fileManagerService;


    @Log("根据所上传的路径信息来展示文件信息")
    @ApiOperation("根据用户所上传的路径信息来展示文件信息")
    @PostMapping("/getFileInfoByPath")
    public CommonResult<Object> getFileInfoByPath(@RequestBody FilePathDTO dto) throws IOException {
        List<FileInfo> result = fileManagerService.getFileInfoByPath(dto.getFilePath());
        return new CommonResult<>().success().message("查询成功").data(result);
    }


    @Log("根据路径获取文件内容")
    @ApiOperation("根据路径获取文件内容")
    @PostMapping("/getFileContentByPath")
    public CommonResult<Object> getFileContentByPath(@RequestBody FilePathDTO dto) throws IOException{
        Map<String, String> result = fileManagerService.getFileContentByPath(dto.getFilePath());
        if (result.get("status").equals("false")){
            return new CommonResult<>().fail(ResultCodeEnum.FAIL).data(null).message(result.get("message"));
        }
        return new CommonResult<>().success().message("查询成功").data(result.get("content"));
    }


    @Log("根据路径保存文件内容")
    @ApiOperation("根据路径保存文件内容")
    @PostMapping("/saveFileContentByPath")
    public CommonResult<Object> saveFileContentByPath(@RequestBody SaveFilePathAndContentDTO dto) throws IOException{
        Map<String, String> result = fileManagerService.saveFileContentByPath(dto);
        if (result.get("status").equals("true")){
            return new CommonResult<>().success().message("保存成功").data(result.get("data"));
        }
        else {
            return new CommonResult<>().fail(ResultCodeEnum.FAIL).message("保存失败").data(result.get("message"));
        }
    }


    @Log("小文件上传")
    @ApiOperation("小文件上传")
    @PostMapping("/uploadFile")
    public CommonResult<Object> uploadFile(@ModelAttribute UploadFileDTO dto) throws Exception {
        Map<String, Object> result;
        result = fileManagerService.uploadFileNormal(dto);
        return new CommonResult<>().success().message("文件上传成功").data(result);
    }


    @Log("根据文件md5校验文件是否存在")
    @ApiOperation("根据文件md5校验文件是否存在")
    @GetMapping("/checkFileExistByMd5")
    public CommonResult<Object> checkFileExistByMd5(@RequestParam String md5){
        String filePath = fileManagerService.checkFileExistByMd5(md5);
        HashMap<String, Object> result = new HashMap<>();
        result.put("path", filePath);
        if (filePath != null){
            result.put("isExist", true);
        }
        else {
            result.put("isExist", false);
        }
        return new CommonResult<>().success().message("查询成功").data(result);
    }


    @Log("分片上传文件预处理")
    @ApiOperation("分片上传文件预处理")
    @PostMapping("/uploadFilePartialPreprocessing")
    public CommonResult<Object> uploadFilePartialPreprocessing(@RequestBody UploadFileDTO dto) throws IOException {
        fileManagerService.uploadFilePartialPreprocessing(dto);
        return new CommonResult<>().success().message("文件预处理完成");
    }


    @Log("分片上传文件")
    @ApiOperation("分片上传文件")
    @PostMapping("/uploadFilePartial")
    public CommonResult<Object> uploadFilePartial(@ModelAttribute UploadFileDTO dto) throws Exception {
        Boolean status = fileManagerService.uploadFilePartial(dto);
        return new CommonResult<>().success().message("上传成功").data(status);
    }


    @Log("检查文件分片上传是否成功")
    @ApiOperation("检查文件分片上传是否成功")
    @GetMapping("/checkUploadFilePartial")
    public CommonResult<Object> checkUploadFilePartial(@RequestParam String md5) throws IOException {
        Map<String, Object> result = fileManagerService.checkUploadFilePartial(md5);
        return new CommonResult<>().success().message("请求成功").data(result);
    }
}
