package cn.iecas.simulate.assessment.entity.dto;

import cn.iecas.simulate.assessment.entity.common.DatasetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;



@Data
public class FileSearchParam {

    /**
     * 查询用户id
     */
    private int userId;

    /**
     * 最小经度
     */
    private double minLon = 181;

    /**
     * 最小纬度
     */
    private double minLat = 181;

    /**
     * 最大经度
     */
    private double maxLon = 181;

    /**
     * 最大纬度
     */
    private double maxLat = 181;


    /**
     * 用户名称
     */
    private String userName;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime;

    /**
     * 查询页码
     */
    private int pageNo =1;

    /**
     * 查询开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime;

    /**
     * 查询批次号
     */
    private int batchNumber;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 是否获取文件内容
     */
    private boolean content;

    /**
     * 查询页数量
     */
    private int pageSize = 10;

    /**
     * 文件类型
     */
    private DatasetType fileType;

    /**
     * 传感器类型
     */
    private String sensorType;

    /**
     * 传感器名称
     */
    private String sensorName;

    /**
     * 模糊查询输入字段，可通过影像名称，来源，关键字，用户名进行模糊查询
     */
    private String searchParam;

    private List<Integer> fileIdList;

    private List<String> searchParamList;

    /**
     * 是否重数据集查看
     * @param fileIdListStr
     */
    private boolean isFromDataset;

    public void setFileIdList(String fileIdListStr){
        if(fileIdListStr.equals(""))
            return;
        List<String> fileList = Arrays.asList(fileIdListStr.split(","));
        fileList.forEach(str->fileIdList.add(Integer.valueOf(str.trim())));
    }

    public void setFileIdList(List<Integer> fileIdList){
        this.fileIdList = fileIdList;
    }
}
