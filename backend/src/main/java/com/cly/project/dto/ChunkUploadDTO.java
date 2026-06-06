package com.cly.project.dto;

import lombok.Data;

@Data
public class ChunkUploadDTO {

    private String uploadId;

    private Integer chunkIndex;

    private Integer totalChunks;

    private Long chunkSize;

    private String originalName;

    private Long fileSize;

    private String md5;
}
