package com.example.demo.Simulator.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.Simulator.mq.ActiveMqPublisher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class SimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    @Autowired
    private FolderReader folderReader;

    @Autowired
    private S3UploadService s3UploadService;
    
    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    public void run() throws Exception {

        List<File> files = folderReader.loadFiles();

        if (files.isEmpty()) {
            logger.info("No files found in the resource folder");
            return;
        }

        for (File file : files) {

            logger.info("Uploading to S3: {}", file.getName());

            boolean success = false;
            String s3Key = "incoming/" + file.getName();

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    // Upload to S3
                    s3UploadService.uploadFile(file, s3Key);
                    logger.info("Successfully uploaded {} to S3 on attempt {}", file.getName(), attempt);
                    
                    // Send to MQ
                    try (ActiveMqPublisher mqPublisher = new ActiveMqPublisher(brokerUrl, "file.queue")) {
                        mqPublisher.connect();
                        String message = "File uploaded: " + file.getName() + " to S3 key: " + s3Key;
                        mqPublisher.publish(message);
                        logger.info("Successfully sent MQ message for file: {}", file.getName());
                    }
                    
                    success = true;
                    break;
                } catch (Exception e) {
                    logger.warn("Failed attempt {} for file: {} - Reason: {}", attempt, file.getName(), e.getMessage());
                    
                    if (attempt < MAX_RETRIES) {
                        logger.info("Retrying in {} seconds...", RETRY_DELAY_MS / 1000);
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                }
            }

            if (!success) {
                logger.error("Failed to upload file after {} attempts, moving to failed folder: {}", MAX_RETRIES, file.getName());
                moveToFailed(file);
            }

            Thread.sleep(1000);
        }

        logger.info("All files processed successfully");
    }

    private void moveToFailed(File file) {
        try {
            File failedDir = new File("failed/");

            if (!failedDir.exists()) {
                failedDir.mkdirs();
            }

            Path targetPath = failedDir.toPath().resolve(file.getName());
            Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File moved to failed folder: {}", file.getName());

        } catch (Exception e) {
            logger.error("Could not move file to failed folder: {}", e.getMessage());
        }
    }
}
