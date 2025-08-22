package com.royal_medical.prescription_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FileStorageConfig {

    @Bean
    @Qualifier("fileStorageProperties")
    @Primary
    public FileStorageProperties fileStorageProperties() {
        return new FileStorageProperties();
    }
}
