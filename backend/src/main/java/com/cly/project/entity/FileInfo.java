package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
public class FileInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String originalName;

    private String filePath;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private String fileExt;

    private String md5;

    private String uploadId;

    private Long chunkSize;

    private Integer totalChunks;

    private Integer chunkIndex;

    private Integer isCompleted;

    private String businessType;

    private Long businessId;

    private Long uploadUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String uploadUserName;

    @TableField(exist = false)
    private String fileSizeText;
}
