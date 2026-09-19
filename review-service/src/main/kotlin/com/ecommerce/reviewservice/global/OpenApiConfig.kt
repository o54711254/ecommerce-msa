package com.ecommerce.reviewservice.global

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    // Gateway 가 JWT 에서 추출해 주입하는 내부 헤더 — Swagger 상에서는 감춘다
    private val gatewayInjectedHeaders = setOf("X-Member-Id", "X-Member-Role")

    @Bean
    fun reviewServiceOpenApi(): OpenAPI = OpenAPI()
        .info(Info().title("review-service API").version("v1"))
        .servers(listOf(Server().url("http://localhost:8080").description("Gateway")))
        .components(
            Components().addSecuritySchemes(
                "bearer",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        )
        .addSecurityItem(SecurityRequirement().addList("bearer"))

    @Bean
    fun removeGatewayInjectedHeaders(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.paths.values.forEach { pathItem ->
            pathItem.readOperations().forEach { operation ->
                operation.parameters?.removeIf { p ->
                    p.`in` == "header" && p.name in gatewayInjectedHeaders
                }
            }
        }
    }
}
