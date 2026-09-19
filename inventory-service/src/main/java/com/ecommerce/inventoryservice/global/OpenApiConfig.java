package com.ecommerce.inventoryservice.global;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final Set<String> GATEWAY_INJECTED_HEADERS = Set.of("X-Member-Id", "X-Member-Role");

    @Bean
    public OpenAPI inventoryServiceOpenApi() {
        return new OpenAPI()
                .info(new Info().title("inventory-service API").version("v1"))
                .servers(List.of(new Server().url("http://localhost:8080").description("Gateway")))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }

    @Bean
    public OpenApiCustomizer removeGatewayInjectedHeaders() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getParameters() != null) {
                        operation.getParameters().removeIf(p ->
                                "header".equals(p.getIn()) && GATEWAY_INJECTED_HEADERS.contains(p.getName()));
                    }
                }));
    }
}
