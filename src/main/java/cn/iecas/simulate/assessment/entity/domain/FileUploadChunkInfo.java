package cn.iecas.simulate.assessment.entity.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @Time: 2024/9/9 11:07
 * @Author: guoxun
 * @File: FileUploadChunkInfo
 * @Description: 文件分块上传分块信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadChunkInfo {

    /**
     * 当前块号
     */
    private Integer currentChunkId;


    /**
     * 当前块是否上传完成 默认为false
     */
    private Boolean isAchieve = false;
}
