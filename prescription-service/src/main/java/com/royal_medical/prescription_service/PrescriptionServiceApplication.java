package com.royal_medical.prescription_service;

import com.royal_medical.prescription_service.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
@EnableFeignClients
public class PrescriptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrescriptionServiceApplication.class, args);
	}

}
