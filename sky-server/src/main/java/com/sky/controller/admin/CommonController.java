package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    private static final String UPLOAD_DIR = "D:\\images\\";

    @PostMapping("/upload")
    public Result upload(String username, Integer age, MultipartFile file) throws Exception, IOException {
        log.info("上傳文件: {}, {}, {}", username, age, file);


        if(!file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String extName = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;

            File targetFlie = new File(UPLOAD_DIR + uniqueFileName);

            if(!targetFlie.getParentFile().exists()) {
                targetFlie.getParentFile().mkdirs();
            }

            file.transferTo(targetFlie);
        }

        return Result.success();
    }
}
