package com.example.demo;

import com.example.demo.config.ConfigReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static final String PROJECT_NAME = "demo-springboot";


	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		ConfigReader.loadProperties();

	}

}
