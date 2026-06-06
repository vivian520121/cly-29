package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.ChunkUploadDTO;
import com.cly.project.entity.FileInfo;
import com.cly.project.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、预览接口")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "普通文件上传")
    @OperationLog(module = "文件管理", operation = "上传文件", businessType = "INSERT")
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) String businessType,
                                   @RequestParam(required = false) Long businessId) {
        return Result.success(fileService.upload(file, businessType, businessId));
    }

    @PostMapping("/chunk/init")
    @Operation(summary = "初始化分片上传")
    @OperationLog(module = "文件管理", operation = "初始化分片上传", businessType = "INSERT")
    public Result<Map<String, String>> initChunkUpload(@RequestBody ChunkUploadDTO dto) {
        String uploadId = fileService.initChunkUpload(dto.getOriginalName(), dto.getFileSize(), dto.getMd5());
        return Result.success(Map.of("uploadId", uploadId));
    }

    @PostMapping("/chunk/upload")
    @Operation(summary = "上传分片")
    @OperationLog(module = "文件管理", operation = "上传分片", businessType = "INSERT")
    public Result<Void> uploadChunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam String uploadId,
                                    @RequestParam Integer chunkIndex) {
        fileService.uploadChunk(file, uploadId, chunkIndex);
        return Result.success();
    }

    @PostMapping("/chunk/complete")
    @Operation(summary = "合并分片完成上传")
    @OperationLog(module = "文件管理", operation = "合并分片", businessType = "UPDATE")
    public Result<FileInfo> completeChunkUpload(@RequestParam String uploadId,
                                                @RequestParam(required = false) String businessType,
                                                @RequestParam(required = false) Long businessId) {
        return Result.success(fileService.completeChunkUpload(uploadId, businessType, businessId));
    }

    @GetMapping("/chunk/check")
    @Operation(summary = "检查分片是否已上传")
    @OperationLog(module = "文件管理", operation = "检查分片", businessType = "QUERY")
    public Result<Map<String, Boolean>> checkChunk(@RequestParam String uploadId,
                                                   @RequestParam Integer chunkIndex) {
        boolean exists = fileService.checkChunk(uploadId, chunkIndex);
        return Result.success(Map.of("exists", exists));
    }

    @GetMapping("/list")
    @Operation(summary = "查询业务附件列表")
    @OperationLog(module = "文件管理", operation = "查询附件列表", businessType = "QUERY")
    public Result<List<FileInfo>> list(@RequestParam String businessType,
                                       @RequestParam Long businessId) {
        return Result.success(fileService.listByBusiness(businessType, businessId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文件详情")
    @OperationLog(module = "文件管理", operation = "查询文件详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<FileInfo> getById(@PathVariable Long id) {
        return Result.success(fileService.getFileById(id));
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "文件预览")
    @OperationLog(module = "文件管理", operation = "预览文件", businessType = "QUERY", businessIdIndex = 0)
    public void preview(@PathVariable Long id, HttpServletResponse response) {
        fileService.preview(id, response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件")
    @OperationLog(module = "文件管理", operation = "下载文件", businessType = "QUERY", businessIdIndex = 0)
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件")
    @OperationLog(module = "文件管理", operation = "删除文件", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> remove(@PathVariable Long id) {
        fileService.removeFile(id);
        return Result.success();
    }
}
