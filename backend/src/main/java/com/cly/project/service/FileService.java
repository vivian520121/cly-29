package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.FileInfo;
import com.cly.project.entity.User;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.FileInfoMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.FileUtil;
import com.cly.project.util.UserContext;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService extends ServiceImpl<FileInfoMapper, FileInfo> {

    private final FileInfoMapper fileInfoMapper;
    private final UserMapper userMapper;

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    @Value("${file.access-path:/uploads/}")
    private String accessPath;

    @Value("${file.chunk-size:5242880}")
    private Long chunkSize;

    private static final String CHUNK_DIR = "chunks";

    @PostConstruct
    public void init() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path chunkDir = Paths.get(uploadPath, CHUNK_DIR);
            if (!Files.exists(chunkDir)) {
                Files.createDirectories(chunkDir);
            }
        } catch (IOException e) {
            throw new BusinessException("初始化上传目录失败");
        }
    }

    @Transactional
    public FileInfo upload(MultipartFile file, String businessType, Long businessId) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String fileExt = FileUtil.getFileExtension(originalName);
        String fileName = FileUtil.generateFileName(originalName);
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = dateDir + "/" + fileName;
        String fullPath = uploadPath + relativePath;
        String fileUrl = accessPath + relativePath;

        try {
            Path targetPath = Paths.get(fullPath);
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setOriginalName(originalName);
        fileInfo.setFilePath(relativePath);
        fileInfo.setFileUrl(fileUrl);
        fileInfo.setFileSize(fileSize);
        fileInfo.setFileType(contentType);
        fileInfo.setFileExt(fileExt);
        fileInfo.setBusinessType(businessType);
        fileInfo.setBusinessId(businessId);
        fileInfo.setUploadUserId(UserContext.getUserId());
        fileInfo.setIsCompleted(1);
        fileInfoMapper.insert(fileInfo);

        return fillFileInfo(fileInfo);
    }

    @Transactional
    public String initChunkUpload(String originalName, Long fileSize, String md5) {
        String uploadId = UUID.randomUUID().toString().replace("-", "");

        String fileExt = FileUtil.getFileExtension(originalName);
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalName(originalName);
        fileInfo.setFileExt(fileExt);
        fileInfo.setFileSize(fileSize);
        fileInfo.setMd5(md5);
        fileInfo.setUploadId(uploadId);
        fileInfo.setChunkSize(chunkSize);
        fileInfo.setTotalChunks(totalChunks);
        fileInfo.setIsCompleted(0);
        fileInfo.setUploadUserId(UserContext.getUserId());
        fileInfoMapper.insert(fileInfo);

        return uploadId;
    }

    @Transactional
    public void uploadChunk(MultipartFile file, String uploadId, Integer chunkIndex) {
        if (file.isEmpty()) {
            throw new BusinessException("分片文件不能为空");
        }

        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUploadId, uploadId);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (fileInfo == null) {
            throw new BusinessException("上传任务不存在");
        }

        if (fileInfo.getIsCompleted() == 1) {
            throw new BusinessException("文件已上传完成");
        }

        String chunkDir = uploadPath + CHUNK_DIR + "/" + uploadId;
        String chunkFileName = chunkIndex + ".part";
        String chunkFilePath = chunkDir + "/" + chunkFileName;

        try {
            Path targetPath = Paths.get(chunkFilePath);
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException("分片上传失败: " + e.getMessage());
        }
    }

    @Transactional
    public FileInfo completeChunkUpload(String uploadId, String businessType, Long businessId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUploadId, uploadId);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (fileInfo == null) {
            throw new BusinessException("上传任务不存在");
        }

        if (fileInfo.getIsCompleted() == 1) {
            throw new BusinessException("文件已上传完成");
        }

        String chunkDir = uploadPath + CHUNK_DIR + "/" + uploadId;
        File chunkDirFile = new File(chunkDir);
        File[] chunkFiles = chunkDirFile.listFiles();

        if (chunkFiles == null || chunkFiles.length != fileInfo.getTotalChunks()) {
            throw new BusinessException("分片不完整，请重新上传");
        }

        String originalName = fileInfo.getOriginalName();
        String fileName = FileUtil.generateFileName(originalName);
        String fileExt = FileUtil.getFileExtension(originalName);
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = dateDir + "/" + fileName;
        String fullPath = uploadPath + relativePath;
        String fileUrl = accessPath + relativePath;

        try {
            Path targetPath = Paths.get(fullPath);
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }

            List<File> sortedChunks = Arrays.stream(chunkFiles)
                    .sorted(new java.util.Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            int idx1 = Integer.parseInt(f1.getName().replace(".part", ""));
                            int idx2 = Integer.parseInt(f2.getName().replace(".part", ""));
                            return idx1 - idx2;
                        }
                    })
                    .collect(Collectors.toList());

            try (FileOutputStream fos = new FileOutputStream(fullPath);
                 FileChannel outputChannel = fos.getChannel()) {
                for (File chunkFile : sortedChunks) {
                    try (FileInputStream fis = new FileInputStream(chunkFile);
                         FileChannel inputChannel = fis.getChannel()) {
                        inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                    }
                }
            }

            deleteDir(chunkDirFile);

            File mergedFile = new File(fullPath);
            String contentType = FileUtil.getContentType(fileExt);

            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(relativePath);
            fileInfo.setFileUrl(fileUrl);
            fileInfo.setFileType(contentType);
            fileInfo.setFileSize(mergedFile.length());
            fileInfo.setBusinessType(businessType);
            fileInfo.setBusinessId(businessId);
            fileInfo.setIsCompleted(1);
            fileInfoMapper.updateById(fileInfo);

            return fillFileInfo(fileInfo);
        } catch (IOException e) {
            throw new BusinessException("合并分片失败: " + e.getMessage());
        }
    }

    public boolean checkChunk(String uploadId, Integer chunkIndex) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUploadId, uploadId);
        FileInfo fileInfo = fileInfoMapper.selectOne(wrapper);
        if (fileInfo == null) {
            return false;
        }

        if (fileInfo.getIsCompleted() == 1) {
            return true;
        }

        String chunkFilePath = uploadPath + CHUNK_DIR + "/" + uploadId + "/" + chunkIndex + ".part";
        return Files.exists(Paths.get(chunkFilePath));
    }

    public List<FileInfo> listByBusiness(String businessType, Long businessId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getBusinessType, businessType)
                .eq(FileInfo::getBusinessId, businessId)
                .eq(FileInfo::getIsCompleted, 1)
                .orderByDesc(FileInfo::getCreateTime);
        List<FileInfo> fileList = fileInfoMapper.selectList(wrapper);
        return fileList.stream()
                .map(this::fillFileInfo)
                .collect(Collectors.toList());
    }

    public FileInfo getFileById(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            return null;
        }
        return fillFileInfo(fileInfo);
    }

    public void preview(Long id, HttpServletResponse response) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }

        String fullPath = uploadPath + fileInfo.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        try {
            String contentType = fileInfo.getFileType();
            if (!StringUtils.hasText(contentType)) {
                contentType = FileUtil.getContentType(fileInfo.getFileExt());
            }

            response.setContentType(contentType);
            response.setContentLengthLong(file.length());

            if (!FileUtil.isImage(fileInfo.getFileExt())) {
                String encodedName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8.toString())
                        .replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "inline; filename=\"" + encodedName + "\"");
            }

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            throw new BusinessException("文件预览失败");
        }
    }

    public void download(Long id, HttpServletResponse response) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }

        String fullPath = uploadPath + fileInfo.getFilePath();
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        try {
            String encodedName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"");
            response.setContentLengthLong(file.length());

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            throw new BusinessException("文件下载失败");
        }
    }

    @Transactional
    public void removeFile(Long id) {
        FileInfo fileInfo = fileInfoMapper.selectById(id);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        fileInfoMapper.deleteById(id);
    }

    private FileInfo fillFileInfo(FileInfo fileInfo) {
        fileInfo.setFileSizeText(FileUtil.formatFileSize(fileInfo.getFileSize()));
        if (fileInfo.getUploadUserId() != null) {
            User user = userMapper.selectById(fileInfo.getUploadUserId());
            if (user != null) {
                fileInfo.setUploadUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        }
        return fileInfo;
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDir(file);
                }
            }
        }
        dir.delete();
    }
}
