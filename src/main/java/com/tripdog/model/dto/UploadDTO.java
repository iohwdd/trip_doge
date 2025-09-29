package com.tripdog.model.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * @author: iohw
 * @date: 2025/9/26 15:17
 * @description:
 */
@Data
public class UploadDTO {
    private Long roleId;
    private MultipartFile file;
}
