package com.LIB.MeesagingSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MessagingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessagingSystemApplication.class, args);
		System.out.println("serving is running ...");
	}
}
