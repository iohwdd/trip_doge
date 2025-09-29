package com.tripdog.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 文档下载请求DTO
 */
@Data
public class DocDownloadDTO {

    /**
     * 文件ID
     */
    @NotBlank(message = "文件ID不能为空")
    private String fileId;
}
