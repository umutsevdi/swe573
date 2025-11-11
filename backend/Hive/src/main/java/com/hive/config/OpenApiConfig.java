package com.hive.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@Profile("dev")
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi() {
        log.info("Starting openAPI");
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("HIVE API")
                        .version("1.0.0")
                        .description("REST interface exposing Hive API operations.")
                        .contact(new Contact()
                                .name("Umut Sevdi")
                                .email("mail@umutsevdi.com")
                                .url("https://github.com/umutsevdi")));
    }
}