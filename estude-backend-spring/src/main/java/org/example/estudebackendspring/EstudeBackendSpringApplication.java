package org.example.estudebackendspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class EstudeBackendSpringApplication {

	public static void main(String[] args) {
		// Load .env từ root project (nơi có build.gradle)
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv
				.configure()
				.directory("./")          // root project
				.ignoreIfMissing()
				.load();

		// Ghi biến vào System để Spring @Value đọc được
		dotenv.entries().forEach(e ->
				System.setProperty(e.getKey(), e.getValue())
		);

		// Chạy Spring Boot

		SpringApplication.run(EstudeBackendSpringApplication.class, args);
	}

}
