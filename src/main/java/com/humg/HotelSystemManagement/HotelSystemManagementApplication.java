package com.humg.HotelSystemManagement;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelSystemManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelSystemManagementApplication.class, args);
	}
}
