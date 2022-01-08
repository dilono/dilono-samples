package com.dilono.sample.camel.sftp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({com.dilono.sample.basic.Application.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
