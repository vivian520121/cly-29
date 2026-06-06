package com.cly.project.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FileUtil {

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"};
    private static final String[] DOCUMENT_EXTENSIONS = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv"};
    private static final String[] VIDEO_EXTENSIONS = {"mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"};

    private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();

    static {
        MIME_TYPE_MAP.put("jpg", "image/jpeg");
        MIME_TYPE_MAP.put("jpeg", "image/jpeg");
        MIME_TYPE_MAP.put("png", "image/png");
        MIME_TYPE_MAP.put("gif", "image/gif");
        MIME_TYPE_MAP.put("bmp", "image/bmp");
        MIME_TYPE_MAP.put("webp", "image/webp");
        MIME_TYPE_MAP.put("svg", "image/svg+xml");
        MIME_TYPE_MAP.put("pdf", "application/pdf");
        MIME_TYPE_MAP.put("doc", "application/msword");
        MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
        MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPE_MAP.put("txt", "text/plain");
        MIME_TYPE_MAP.put("md", "text/markdown");
        MIME_TYPE_MAP.put("csv", "text/csv");
        MIME_TYPE_MAP.put("mp4", "video/mp4");
        MIME_TYPE_MAP.put("avi", "video/x-msvideo");
        MIME_TYPE_MAP.put("mov", "video/quicktime");
        MIME_TYPE_MAP.put("wmv", "video/x-ms-wmv");
        MIME_TYPE_MAP.put("flv", "video/x-flv");
        MIME_TYPE_MAP.put("mkv", "video/x-matroska");
        MIME_TYPE_MAP.put("webm", "video/webm");
        MIME_TYPE_MAP.put("zip", "application/zip");
        MIME_TYPE_MAP.put("rar", "application/x-rar-compressed");
        MIME_TYPE_MAP.put("7z", "application/x-7z-compressed");
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public static String formatFileSize(long size) {
        if (size < 0) {
            return "0 B";
        }
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        }
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    public static String generateFileName(String originalName) {
        String extension = getFileExtension(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (extension.isEmpty()) {
            return uuid;
        }
        return uuid + "." + extension;
    }

    public static String getContentType(String ext) {
        if (ext == null || ext.isEmpty()) {
            return "application/octet-stream";
        }
        String lowerExt = ext.toLowerCase();
        return MIME_TYPE_MAP.getOrDefault(lowerExt, "application/octet-stream");
    }

    public static boolean isImage(String ext) {
        if (ext == null || ext.isEmpty()) {
            return false;
        }
        return Arrays.asList(IMAGE_EXTENSIONS).contains(ext.toLowerCase());
    }

    public static boolean isDocument(String ext) {
        if (ext == null || ext.isEmpty()) {
            return false;
        }
        return Arrays.asList(DOCUMENT_EXTENSIONS).contains(ext.toLowerCase());
    }

    public static boolean isVideo(String ext) {
        if (ext == null || ext.isEmpty()) {
            return false;
        }
        return Arrays.asList(VIDEO_EXTENSIONS).contains(ext.toLowerCase());
    }
}
