package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringDataAsyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDataAsyncApplication.class, args);
	}

}
