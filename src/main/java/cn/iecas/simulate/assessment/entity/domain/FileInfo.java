package cn.iecas.simulate.assessment.entity.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;



/**
 * @Time: 2024/9/6 11:19
 * @Author: guoxun
 * @File: FileInfo
 * @Description: 文件信息对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    /**
     * 文件名
     */
    private String filename;

    /**
     * 创建日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;

    /**
     * 大小 单位: 字节
     */
    private Long size;

    /**
     * 类型
     */
    private String type;

    /**
     * 包含的子文件
     */
    private Long subFileCount;

    /**
     * 包含的子文件夹
     */
    private Long subFolderCount;

    /**
     * 所在目录位置
     */
    private String localPath;

}
