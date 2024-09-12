/**
 * @Time: 2024/9/6 17:03
 * @Author: guoxun
 * @File: SaveFilePathAndContentDTO
 * @Description:
 */

package cn.iecas.simulate.assessment.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveFilePathAndContentDTO {

    /**
     * 文件保存路径
     */
    private String savePath;


    /**
     * 文件保存内容
     */
    private String content;
}
