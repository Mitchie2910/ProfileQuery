package com.hng.nameprocessing.configuration;

import com.hng.nameprocessing.dtos.DataMapping;
import com.hng.nameprocessing.exceptions.ServiceValidationException;
import com.hng.nameprocessing.repositories.DataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class ConfigurationFile {

    public ObjectMapper objectMapper;

    public ConfigurationFile(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsynchThread-");
        executor.initialize();
        return executor;
    }

}
