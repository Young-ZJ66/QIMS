package com.young.controller;

import com.young.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/api/file")
public class FileController {

    // 保存到当前项目根目录的 uploads 文件夹下
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传失败，请选择文件");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 获取原文件名并生成新的 UUID 文件名
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            file.transferTo(new File(dir, newFilename));

            // 返回可以在前端访问的 URL 路径
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传异常: " + e.getMessage());
        }
    }
}
