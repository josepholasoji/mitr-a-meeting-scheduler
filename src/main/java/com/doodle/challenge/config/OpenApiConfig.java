package com.doodle.challenge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    public OpenAPI doodleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Doodle API")
                        .version("v1")
                        .description(
                                "Meeting scheduling platform: manage personal time slots, convert them "
                                        + "into meetings with participants, and query free/busy availability."))
                .components(new Components()
                        .addSecuritySchemes(
                                BASIC_AUTH_SCHEME,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }
}
