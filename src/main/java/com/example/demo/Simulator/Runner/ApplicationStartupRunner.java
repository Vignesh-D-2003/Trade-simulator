package com.example.demo.Simulator.Runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.demo.Simulator.Service.SimulatorService;

@Component
public class ApplicationStartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupRunner.class);

    @Autowired
    private SimulatorService simulatorService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Application started - Running file simulation automatically");
        simulatorService.run();
        logger.info("File simulation completed");
    }
}