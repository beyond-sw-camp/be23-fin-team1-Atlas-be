package com.ozz.atlas.supply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SupplyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupplyServiceApplication.class, args);
	}

}
