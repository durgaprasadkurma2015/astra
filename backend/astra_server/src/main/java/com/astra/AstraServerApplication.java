package com.astra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AstraServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AstraServerApplication.class, args);
	}

}
