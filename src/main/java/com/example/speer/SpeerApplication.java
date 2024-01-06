package com.example.speer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories("com.example.speer.Repository.ESRepo")
public class SpeerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeerApplication.class, args);
	}

}
