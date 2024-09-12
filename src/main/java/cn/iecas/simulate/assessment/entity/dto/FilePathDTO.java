/**
 * @Time: 2024/9/9 8:47
 * @Author: guoxun
 * @File: FilePathDTO
 * @Description:
 */

package cn.iecas.simulate.assessment.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePathDTO {

    /**
     * 文件 or 文件夹路径
     */
    private String filePath;
}
