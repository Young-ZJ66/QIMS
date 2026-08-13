package com.young.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * 上传文件目录配置
 * <p>
 * 目录由 {@code qims.upload.dir} 配置项指定，默认为 {@code ./uploads}（相对启动目录）。
 * 默认在 qims-backend 目录下启动后端，上传文件即落在 qims-backend/uploads 下；
 * 如需调整，修改 application.yml 中的 {@code qims.upload.dir} 即可。
 * </p>
 */
@Component
public class UploadPathConfig {

    private static final Logger log = LoggerFactory.getLogger(UploadPathConfig.class);

    /**
     * 上传目录配置项，默认 ./uploads（相对启动目录）。
     */
    @Value("${qims.upload.dir:./uploads}")
    private String configuredDir;

    /**
     * 上传文件根目录绝对路径，结尾不带分隔符。
     */
    private String uploadRootPath;

    @PostConstruct
    public void init() {
        File dir = new File(configuredDir.trim());
        uploadRootPath = dir.getAbsolutePath();

        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("无法创建上传目录: {}", uploadRootPath);
        }
        log.info("上传文件根目录: {}", uploadRootPath);
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