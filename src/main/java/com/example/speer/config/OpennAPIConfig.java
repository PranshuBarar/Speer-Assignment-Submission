package com.example.speer.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpennAPIConfig {

    @Bean
    public OpenAPI springOpenAPI() {
        final String securityScheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("SpringBoot API")
                        .description("Speer Backend Application")
                        .version("v1.1")
                        .license(new License().name("No license").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Speer Backend Documentation"));
    }
}
