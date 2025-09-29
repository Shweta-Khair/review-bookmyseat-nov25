package com.bookmyseat.reviewservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI reviewServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.bookmyseat.com");
        prodServer.setDescription("Production server");

        Contact contact = new Contact();
        contact.setEmail("support@bookmyseat.com");
        contact.setName("BookMySeat Support");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Review Service API")
                .version("1.0.0")
                .contact(contact)
                .description("This API manages movie reviews and ratings for the BookMySeat application. " +
                            "It provides endpoints for submitting reviews, retrieving reviews for movies, " +
                            "and getting aggregated rating information.")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}