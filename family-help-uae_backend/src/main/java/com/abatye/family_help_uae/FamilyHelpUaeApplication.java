package com.abatye.family_help_uae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FamilyHelpUaeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamilyHelpUaeApplication.class, args);
	}

}
