package com.siderov.btctracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiquidTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiquidTrackerApplication.class, args);
	}

}
