package com.young.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * 上传文件根目录配置
 * <p>
 * 通过 classpath 定位 qims-backend 工程根目录，确保无论从项目根目录还是 qims-backend 子目录启动，
 * 上传文件始终落在 qims-backend/uploads 下。
 * </p>
 */
@Component
public class UploadPathConfig {

    private static final Logger log = LoggerFactory.getLogger(UploadPathConfig.class);

    /**
     * 上传目录配置项，留空则自动检测。
     */
    @Value("${qims.upload.dir:}")
    private String configuredDir;

    /**
     * 上传文件根目录绝对路径，结尾不带分隔符。
     */
    private String uploadRootPath;

    @PostConstruct
    public void init() {
        if (configuredDir != null && !configuredDir.trim().isEmpty()) {
            File dir = new File(configuredDir.trim());
            uploadRootPath = dir.getAbsolutePath();
        } else {
            uploadRootPath = detectBackendUploadDir();
        }

        File dir = new File(uploadRootPath);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("无法创建上传目录: {}", uploadRootPath);
        }
        log.info("上传文件根目录: {}", uploadRootPath);
    }

    /**
     * 通过 classpath 定位 qims-backend 目录，返回其下的 uploads 子目录。
     * classpath 根目录在 IDE 或 mvn 运行时为 qims-backend/target/classes/，
     * 上溯两级即得到 qims-backend/ 工程根目录。
     */
    private String detectBackendUploadDir() {
        try {
            File classpathRoot = new ClassPathResource(".").getFile();
            File targetDir = classpathRoot.getCanonicalFile().getParentFile();
            if (targetDir == null || targetDir.getParentFile() == null) {
                throw new IOException("无法解析 classpath 父级目录");
            }
            File backendRoot = targetDir.getParentFile();
            return backendRoot.getAbsolutePath() + File.separator + "uploads";
        } catch (IOException e) {
            log.warn("无法通过 classpath 定位工程根目录，回退到工作目录下的 uploads: {}", e.getMessage());
            return System.getProperty("user.dir") + File.separator + "uploads";
        }
    }

    /**
     * 获取上传文件根目录绝对路径。
     */
    public String getUploadRootPath() {
        return uploadRootPath;
    }

    /**
     * 获取报告 PDF 文件目录绝对路径。
     */
    public String getReportsDir() {
        return uploadRootPath + File.separator + "reports";
    }
}
