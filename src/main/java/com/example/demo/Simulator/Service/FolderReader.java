package com.example.demo.Simulator.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class FolderReader {

    private static final Logger logger = LoggerFactory.getLogger(FolderReader.class);
    
    @Value("${simulator.resource-folder}")
    private String resourceFolder;

    public List<File> loadFiles() {
        File folder = new File(resourceFolder);

        if (!folder.exists()) {
            logger.error("Resource folder does not exist: {}", folder.getAbsolutePath());
            return List.of();
        }
        
        if (!folder.isDirectory()) {
            logger.error("Resource path is not a directory: {}", folder.getAbsolutePath());
            return List.of();
        }

        File[] files = folder.listFiles();
        if (files == null) {
            logger.warn("Unable to list files in directory: {}", folder.getAbsolutePath());
            return List.of();
        }

        List<File> fileList = Arrays.stream(files)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName))
                .toList();
                
        logger.info("Found {} files in resource folder: {}", fileList.size(), resourceFolder);
        return fileList;
    }
}
