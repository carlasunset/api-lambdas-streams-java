package com.carla.apilambdasstreans;

import com.carla.apilambdasstreans.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiLambdasStreamsApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ApiLambdasStreamsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Principal principal = new Principal();
        principal.menu();

    }
}
