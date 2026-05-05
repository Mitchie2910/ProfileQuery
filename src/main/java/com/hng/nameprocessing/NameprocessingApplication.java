package com.hng.nameprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NameprocessingApplication {

  public static void main(String[] args) {
    SpringApplication.run(NameprocessingApplication.class, args);
  }
}
