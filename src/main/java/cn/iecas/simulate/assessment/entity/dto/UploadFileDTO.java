/**
 * @Time: 2024/9/9 10:44
 * @Author: guoxun
 * @File: UploadFileDTO
 * @Description:
 */

package cn.iecas.simulate.assessment.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileDTO {

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件所要保存的位置
     */
    private String savePath;

    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 分片总数
     */
    private Integer chunksCount;

    /**
     * 分片id
     */
    private Integer chunkId;

    /**
     * 分片大小
     */
    private Long chunkSize;

    /**
     * 每个分片的md5码
     */
    private String chunkMd5;

    /**
     * md5
     */
    private String md5;
}
