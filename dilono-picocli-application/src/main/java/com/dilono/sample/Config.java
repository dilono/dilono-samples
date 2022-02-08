package com.dilono.sample;

import com.dilono.edifact.client.ECSClient;
import com.dilono.edifact.client.ECSClientBuilder;
import com.dilono.edifact.client.ECSClientCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    ECSClient ecsClient(@Value("${dilono.server.url}") final URL url,
                        @Value("${dilono.server.token.id}") final String tokenId,
                        @Value("${dilono.server.token.secret}") final String tokenSecret) {

        return new ECSClientBuilder()
            .withBaseUrl(url)
            .withCredentials(ECSClientCredentials.token(tokenId, tokenSecret))
            .build();
    }
}
