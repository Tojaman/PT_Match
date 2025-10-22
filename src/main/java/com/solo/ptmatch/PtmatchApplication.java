package com.solo.ptmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class PtmatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PtmatchApplication.class, args);
	}

}
