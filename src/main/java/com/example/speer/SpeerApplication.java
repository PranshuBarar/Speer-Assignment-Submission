package com.example.speer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories("com.example.speer.Repository.ESRepository")
public class SpeerApplication {

	public static void main(String[] args) {
		System.setProperty("server.servlet.context-path", "/springboot3");
		SpringApplication.run(SpeerApplication.class, args);
	}

}
