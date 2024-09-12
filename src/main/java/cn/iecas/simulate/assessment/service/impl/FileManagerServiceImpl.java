package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.common.constant.RedisConstant;
import cn.iecas.simulate.assessment.entity.domain.FileInfo;
import cn.iecas.simulate.assessment.entity.domain.FileUploadPartialInfo;
import cn.iecas.simulate.assessment.entity.dto.SaveFilePathAndContentDTO;
import cn.iecas.simulate.assessment.entity.dto.UploadFileDTO;
import cn.iecas.simulate.assessment.service.FileManagerService;
import cn.iecas.simulate.assessment.util.FileUtils;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;



/**
 * @Time: 2024/9/6 11:14
 * @Author: guoxun
 * @File: ManagerFileServiceImpl
 * @Description: 文件上传实现类
 */
@Service
public class FileManagerServiceImpl implements FileManagerService {

    @Value("#{'${file.modify.type}'.replace(' ', '').split(',')}")
    private List<String> modifyType;

    @Value("${file.default-save-path}")
    private String defaultSavePath;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public List<FileInfo> getFileInfoByPath(String filePath) throws IOException {
        List<FileInfo> result = new ArrayList<>();
        Path path = Paths.get(filePath);
        try (Stream<Path> paths = Files.list(path)) {
            paths.forEach(e -> {
                try {
                    FileInfo info = FileUtils.getFileInfoByFile(e);
                    result.add(info);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            return result;
        }
    }


    @Override
    public Map<String, String> getFileContentByPath(String filePath) throws IOException{
        Map<String, String> result = new HashMap<>();
        String[] pathSplit = filePath.split("\\.");
        Path path = Paths.get(filePath);
        if (checkPathIsValid(result, pathSplit, path)) return result;
        List<String> stringList = Files.readAllLines(path);
        StringBuilder stringBuffer = new StringBuilder();
        for (String e : stringList){
            stringBuffer.append(e).append("\n");
        }
        result.put("content", stringBuffer.toString());
        return result;
    }


    @Override
    public Map<String, String> saveFileContentByPath(SaveFilePathAndContentDTO dto) throws IOException {
        Map<String, String> result = new HashMap<>();
        File saveFile = new File(dto.getSavePath());
        String[] pathSplit = dto.getSavePath().split("\\.");
        Path path = Paths.get(dto.getSavePath());
        if (checkPathIsValid(result, pathSplit, path)) return result;
        if (!saveFile.exists()){
            result.put("status", "false");
            result.put("message", "当前文件目录下,文件不存在");
            return result;
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String backupFilePathStr = saveFile.getParent() + "\\" + uuid + ".tmp";
        Path backupFilePath = Paths.get(backupFilePathStr);
        Files.copy(path, backupFilePath);
        try {
            try (FileWriter writer = new FileWriter(saveFile)){
                writer.write(dto.getContent());
            }
            result.put("status", "true");
            result.put("message", "修改成功");
            result.put("data", dto.getContent());
            Files.deleteIfExists(backupFilePath);
            return result;
        } catch (IOException e){
            e.printStackTrace();
            Files.deleteIfExists(path);
            Files.copy(backupFilePath, path);
            Files.deleteIfExists(backupFilePath);
        }
        result.put("status", "false");
        result.put("message", "修改失败");
        return result;
    }


    /**
     * 获取文件的保存路径
     * @param dto 前端传递过来的信息对象
     * @return 文件保存路径
     */
    private String getSavePath(UploadFileDTO dto) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = simpleDateFormat.format(new Date());
        dto.setFilename(timestamp + "-" + dto.getFilename());
        if (dto.getSavePath() == null) {
            FileUtils.createDirIfNotExist(defaultSavePath);
            return Paths.get(defaultSavePath, dto.getFilename()).toString();
        }
        else {
            FileUtils.createDirIfNotExist(dto.getSavePath());
            return Paths.get(dto.getSavePath(), dto.getFilename()).toString();
        }
    }


    @Override
    public Boolean uploadFilePartial(UploadFileDTO dto) throws Exception {
        String uploadStatusRecord = stringRedisTemplate
                .opsForValue().get(RedisConstant.FILE_TEMP_PARTIAL_UPLOAD_INFO.getFullPath(dto.getMd5()));
        String fileDiskTemp = stringRedisTemplate
                .opsForValue().get(RedisConstant.FILE_UPLOAD_PARTIAL_TEMP_CACHE.getFullPath(dto.getMd5()));
        FileUtils.writeWithChunk(fileDiskTemp, dto.getFile().getInputStream()
                , dto.getChunkMd5(), dto.getChunkId(), dto.getChunkSize());
        FileUploadPartialInfo uploadStatus = JSON.parseObject(uploadStatusRecord, FileUploadPartialInfo.class);
        if (!uploadStatus.setChunk2Achieve(dto.getChunkId()))
            throw new RuntimeException("更新上传状态信息异常");
        stringRedisTemplate.opsForValue().set(RedisConstant.FILE_TEMP_PARTIAL_UPLOAD_INFO.getFullPath(dto.getMd5())
                , JSON.toJSONString(uploadStatus));
        return true;
    }


    @Override
    public Map<String, Object> uploadFileNormal(UploadFileDTO dto) throws Exception {
        String sp = getSavePath(dto);
        Map<String, Object> result = FileUtils.saveFile(sp, dto.getMd5(), dto.getFile().getInputStream());
        stringRedisTemplate.opsForValue().set(RedisConstant.FILE_UPLOAD_RECORD.getFullPath(dto.getMd5()), sp);
        return result;
    }


    @Override
    public String checkFileExistByMd5(String md5) {
        return stringRedisTemplate.opsForValue().get(RedisConstant.FILE_UPLOAD_RECORD.getFullPath(md5));
    }


    @Override
    public void uploadFilePartialPreprocessing(UploadFileDTO dto) throws IOException {
        String sp = getSavePath(dto);
        String tempFilePath = FileUtils.applyingDiskSpace(sp, dto.getFileSize());
        String partialUploadTempInfo = FileUtils.createPartialUploadTempFile(dto.getFileSize(), dto.getChunkSize());
        stringRedisTemplate.opsForValue().set(RedisConstant.FILE_TEMP_PARTIAL_UPLOAD_INFO.getFullPath(dto.getMd5())
                , partialUploadTempInfo);
        stringRedisTemplate.opsForValue().set(RedisConstant.FILE_UPLOAD_PARTIAL_TEMP_CACHE.getFullPath(dto.getMd5())
                , tempFilePath);
    }


    @Override
    public Map<String, Object> checkUploadFilePartial(String md5) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String uploadStatusRecord = stringRedisTemplate
                .opsForValue().get(RedisConstant.FILE_TEMP_PARTIAL_UPLOAD_INFO.getFullPath(md5));
        if (uploadStatusRecord == null)
            throw new RuntimeException("未在缓存中查询到该md5编码所对应的文件, 请重走上传流程");
        FileUploadPartialInfo fileStatusInfo = JSON.parseObject(uploadStatusRecord, FileUploadPartialInfo.class);
        if (fileStatusInfo.checkIsAchieve()){
            String fileDiskTemp = stringRedisTemplate
                    .opsForValue().get(RedisConstant.FILE_UPLOAD_PARTIAL_TEMP_CACHE.getFullPath(md5));
            if (fileDiskTemp == null){
                throw new RuntimeException("未查询到缓存文件, 注：请求该方法前请先调用预处理方法");
            }
            Files.move(Paths.get(fileDiskTemp), Paths.get(fileDiskTemp.replace(".cache.tmp", "")));
            result.put("isOk", true);
            stringRedisTemplate.opsForValue().set(RedisConstant.FILE_UPLOAD_RECORD.getFullPath(md5)
                    , fileDiskTemp.replace(".cache.tmp", ""));
            stringRedisTemplate.delete(RedisConstant.FILE_TEMP_PARTIAL_UPLOAD_INFO.getFullPath(md5));
            stringRedisTemplate.delete(RedisConstant.FILE_UPLOAD_PARTIAL_TEMP_CACHE.getFullPath(md5));
        }
        else {
            List<Integer> reUploadChunkId = fileStatusInfo.getReUploadChunkId();
            result.put("isOk", false);
            result.put("reupload", reUploadChunkId);
        }
        return result;
    }


    /**
     * 检查路径是否合法
     * @param result
     * @param pathSplit
     * @param path
     * @return
     */
    private boolean checkPathIsValid(Map<String, String> result, String[] pathSplit, Path path) {
        if (pathSplit.length > 1){
            int idx = pathSplit.length - 1;
            String type = pathSplit[idx];
            if (!modifyType.contains(type)){
                result.put("status", "false");
                result.put("message", "当前文件类型不支持, 目前支持的类型如下: " + modifyType.toString());
                result.put("content", null);
                return true;
            }
            else {
                result.put("status", "true");
            }
        }
        else if (Files.isDirectory(path)){
            result.put("status", "false");
            result.put("message", "当前上传的为文件夹路径, 请上传文件路径, 目前支持的类型如下: " + modifyType.toString());
            result.put("content", null);
            return true;
        }
        return false;
    }
}
