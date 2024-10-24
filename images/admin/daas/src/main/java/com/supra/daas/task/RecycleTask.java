package com.supra.daas.task;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.supra.daas.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecycleTask {
    
    @Value("${settings.path.files}")
    private String filesPath;

    @Scheduled(cron = "0 0 0 ? * ?")
    public void checkRecycle() {
        File filesDir = new File(filesPath);
        filesDir.listFiles((dir, name) -> {
            File file = new File(dir, name);
            if (file.lastModified() + 86400000 * 7 < System.currentTimeMillis()) {
                FileUtil.deleteFile(file);
            }
            return false;
        });
    }

}
