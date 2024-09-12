/**
 * @Time: 2024/9/10 14:17
 * @Author: guoxun
 * @File: RedisConstant
 * @Description:
 */

package cn.iecas.simulate.assessment.common.constant;



public enum RedisConstant {

    /**
     * 文件上传记录
     */
    FILE_UPLOAD_RECORD("FILE:UPLOAD:RECORD:"),
    /**
     * 文件分片上传分片信息记录
     */
    FILE_TEMP_PARTIAL_UPLOAD_INFO("FILE:TEMP:PARTIAL:UPLOAD:INFO:"),
    /**
     * 预申请的空间位置
     */
    FILE_UPLOAD_PARTIAL_TEMP_CACHE("FILE:UPLOAD:PARTIAL:TEMP:CACHE:");


    private final String path;


    RedisConstant(String path){
        this.path = path;
    }


    public String getFullPath(String key){
        return this.path + key;
    }
}
