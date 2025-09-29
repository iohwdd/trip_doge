package com.tripdog.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档实体类
 */
@Data
public class DocDO {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 文件访问地址
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小，单位字节
     */
    private Double fileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
