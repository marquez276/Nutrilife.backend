package com.itb.inf3an.nutrilife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NutriLifeApplication {

	private static final Logger log = LoggerFactory.getLogger(NutriLifeApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(NutriLifeApplication.class, args);
		log.info("NutriLife iniciado com sucesso.");
	}
}
