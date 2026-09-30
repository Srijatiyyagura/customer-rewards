package com.retailer.rewards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI 3 / Swagger documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerRewardsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retailer Customer Rewards Program API")
                        .description("RESTful Web API to calculate reward points earned by customers per month and total, " +
                                "based on recorded purchase transactions. Points: 2 points per dollar over $100, plus 1 point per dollar between $50 and $100.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Retailer Rewards Team")
                                .email("support@retailer.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
