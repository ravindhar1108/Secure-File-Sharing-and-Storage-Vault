package com.example.Vault;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecureStorageVaultApplication {

	public static void main(String[] args) {
		// Try loading from root or backend directory
		Dotenv dotenv = Dotenv.configure()
				.directory("./backend")
				.ignoreIfMissing()
				.load();

		// Fallback to current directory if backend/.env wasn't found or was empty
		if (dotenv.entries().isEmpty()) {
			dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
		}

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(SecureStorageVaultApplication.class, args);
	}

}
