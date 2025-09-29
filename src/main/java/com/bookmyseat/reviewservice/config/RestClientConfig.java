package com.bookmyseat.reviewservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${movie-service.timeout:3000}")
    private long timeout;

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeout))
                .withReadTimeout(Duration.ofMillis(timeout));
        return ClientHttpRequestFactories.get(settings);
    }

    @Bean
    public RestClient.Builder restClientBuilder(ClientHttpRequestFactory requestFactory) {
        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    // Add tracing headers for distributed tracing
                    request.getHeaders().add("Content-Type", "application/json");
                    return execution.execute(request, body);
                });
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}