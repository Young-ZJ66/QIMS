package com.young.controller;

import com.young.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    // 保存到当前项目根目录的 uploads 文件夹下
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /** 允许上传的文件扩展名白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".pdf", ".doc", ".docx", ".xls", ".xlsx")
    );

    /** 最大文件大小（10MB） */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 文件上传（校验文件类型和大小）
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传失败，请选择文件");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小超过限制（最大 10MB）");
        }

        // 获取原文件名并校验扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error("不支持的文件类型，允许的格式: jpg, jpeg, png, gif, bmp, pdf, doc, docx, xls, xlsx");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成新的 UUID 文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            file.transferTo(new File(dir, newFilename));

            // 返回可以在前端访问的 URL 路径
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            log.error("文件上传异常", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }
}
