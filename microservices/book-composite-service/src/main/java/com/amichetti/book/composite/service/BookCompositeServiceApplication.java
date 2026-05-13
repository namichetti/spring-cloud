package com.amichetti.book.composite.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.amichetti")
@SpringBootApplication
public class BookCompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookCompositeServiceApplication.class, args);
	}

}
