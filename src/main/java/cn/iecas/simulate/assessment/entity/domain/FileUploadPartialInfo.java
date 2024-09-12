package cn.iecas.simulate.assessment.entity.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



/**
 * @Time: 2024/9/9 11:02
 * @Author: guoxun
 * @File: FileUploadPartialInfo
 * @Description: 文件分片上传信息记录类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadPartialInfo {

    /**
     * 分块总数
     */
    private Integer totalChunks;

    /**
     * 每个块的信息
     */
    private List<FileUploadChunkInfo> chunkInfoList = new ArrayList<>();

    /**
     * 当前文件是否上传完毕
     */
    private Boolean isAchieve = false;

    /**
     * 整个文件的md5编码
     */
    private String md5;


    /**
     * 设置指定块的标记为true
     * @param chunkId
     * @return
     */
    public Boolean setChunk2Achieve(Integer chunkId){
        if (chunkInfoList.get(chunkId).getCurrentChunkId().equals(chunkId)) {
            chunkInfoList.get(chunkId).setIsAchieve(true);
            return true;
        }
        for (FileUploadChunkInfo e : chunkInfoList){
            if (Objects.equals(e.getCurrentChunkId(), chunkId)) {
                e.setIsAchieve(true);
                return true;
            }
        }
        return false;
    }


    /**
     * 检查指定块是否上传完毕
     * @param chunkId 块id
     * @return true: 上传完成  false: 未完成
     */
    public Boolean chunkIsAchieve(Integer chunkId){
        if (chunkInfoList.get(chunkId).getCurrentChunkId().equals(chunkId))
            return chunkInfoList.get(chunkId).getIsAchieve();
        for (FileUploadChunkInfo e : chunkInfoList){
            if (Objects.equals(e.getCurrentChunkId(), chunkId))
                return e.getIsAchieve();
        }
        return false;
    }


    /**
     * 检查当前文件是否全部上传完成
     * @return true: 完成  false: 未完成
     */
    public Boolean checkIsAchieve(){
        for (FileUploadChunkInfo e : chunkInfoList){
            if (!e.getIsAchieve())
                return false;
        }
        return true;
    }


    /**
     * 获取需要重传的块id
     * @return chunkIds
     */
    public List<Integer> getReUploadChunkId(){
        List<Integer> reUploadChunkIds = new ArrayList<>();
        for (FileUploadChunkInfo e : chunkInfoList){
            if (!e.getIsAchieve()){
                reUploadChunkIds.add(e.getCurrentChunkId());
            }
        }
        return reUploadChunkIds;
    }
}
