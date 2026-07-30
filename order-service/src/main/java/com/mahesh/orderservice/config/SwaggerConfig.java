package com.mahesh.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Handles order creation and publishes events to Kafka topic: order-event-topic")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Mahesh Motale")
                                .url("https://github.com/Mahesh-Motale77")
                                .email("motalemahesh7777@gmail.com")
                        )
                );
    }
}