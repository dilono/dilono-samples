package com.dilono.sample.basic;


import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.client.ECSClientBuilder;
import com.dilono.edifact.client.ECSClientCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URL;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // tag::esc-client-bean-config[]
    @Bean
    ECSClient ecsClient(@Value("${dilono.server.url}") final URL url,
                        @Value("${dilono.server.token.id}") final String tokenId,
                        @Value("${dilono.server.token.secret}") final String tokenSecret) {

        return new ECSClientBuilder()
            .withBaseUrl(url)
            .withCredentials(ECSClientCredentials.token(tokenId, tokenSecret))
            .build();
    }
    // end::esc-client-bean-config[]
}
