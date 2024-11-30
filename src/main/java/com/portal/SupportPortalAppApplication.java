package com.portal;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.portal.constant.FileConstant;

@SpringBootApplication
@EnableDiscoveryClient
public class SupportPortalAppApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(SupportPortalAppApplication.class, args);
	    new File(FileConstant.USER_FOLDER).mkdirs();
	
	}
	
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
