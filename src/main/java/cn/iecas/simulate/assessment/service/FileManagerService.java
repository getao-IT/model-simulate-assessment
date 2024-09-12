/**
 * @Time: 2024/9/6 11:12
 * @Author: guoxun
 * @File: ManageFileService
 * @Description:
 */

package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.FileInfo;
import cn.iecas.simulate.assessment.entity.dto.SaveFilePathAndContentDTO;
import cn.iecas.simulate.assessment.entity.dto.UploadFileDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileManagerService {
    List<FileInfo> getFileInfoByPath(String filePath) throws IOException;

    Map<String, String> getFileContentByPath(String filePath) throws IOException;

    Map<String, String> saveFileContentByPath(SaveFilePathAndContentDTO dto) throws IOException;

    Boolean uploadFilePartial(UploadFileDTO dto) throws Exception;

    Map<String, Object> uploadFileNormal(UploadFileDTO dto) throws Exception;

    String checkFileExistByMd5(String md5);

    void uploadFilePartialPreprocessing(UploadFileDTO dto) throws IOException;

    Map<String, Object> checkUploadFilePartial(String md5) throws IOException;
}
