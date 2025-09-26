package com.example.bankcards.config;

import com.example.bankcards.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Bank Cards API", version = "1.0.0", description = "API для управления банковскими картами и переводами"),
        servers = {@Server(url = "http://localhost:8081")}
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        // bearerAuth security scheme
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Вставьте JWT токен в формате: Bearer <token>");

        // Глобальная security: все эндпоинты по умолчанию требуют bearerAuth (кроме разрешённых в SecurityConfig)
        SecurityRequirement globalSec = new SecurityRequirement().addList("bearerAuth");

        // Общий JSON контент со схемой ErrorResponse и примерами
        Schema<ErrorResponse> errorSchema = new Schema<ErrorResponse>().$ref("#/components/schemas/ErrorResponse");

        MediaType mediaType = new MediaType().schema(errorSchema);
        io.swagger.v3.oas.models.examples.Example errExample = new io.swagger.v3.oas.models.examples.Example()
                .summary("Типовой ответ ошибки")
                .value(Map.of(
                        "timestamp", "2025-09-26T10:15:30Z",
                        "status", 400,
                        "error", "BAD_REQUEST",
                        "code", "VALIDATION_ERROR",
                        "message", "Validation failed",
                        "path", "/api/cards/search",
                        "traceId", "e7d58f2b-f6a6-4a45-8e95-b3a0e7f2f1a9",
                        "validationErrors", Map.of("field", "must not be null")
                ));
        mediaType.addExamples("default", errExample);

        Content errorJsonContent = new Content().addMediaType("application/json", mediaType);

        // Компоненты с переиспользуемыми ответами
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", bearer)
                .addResponses("BadRequest", new ApiResponse().description("Некорректный запрос").content(errorJsonContent))
                .addResponses("Unauthorized", new ApiResponse().description("Требуется аутентификация").content(errorJsonContent))
                .addResponses("Forbidden", new ApiResponse().description("Доступ запрещён").content(errorJsonContent))
                .addResponses("NotFound", new ApiResponse().description("Ресурс не найден").content(errorJsonContent))
                .addResponses("Conflict", new ApiResponse().description("Конфликт состояния/бизнес-ошибка").content(errorJsonContent))
                .addResponses("UnprocessableEntity", new ApiResponse().description("Невалидное состояние ресурса").content(errorJsonContent))
                .addResponses("InternalServerError", new ApiResponse().description("Внутренняя ошибка сервера").content(errorJsonContent));

        // Явно объявим схему ErrorResponse (springdoc обычно генерит из аннотаций, но на случай экспорта)
        components.addSchemas("ErrorResponse", new Schema<ErrorResponse>()
                .type("object")
                .addProperties("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperties("status", new Schema<>().type("integer"))
                .addProperties("error", new Schema<>().type("string"))
                .addProperties("code", new Schema<>().type("string"))
                .addProperties("message", new Schema<>().type("string"))
                .addProperties("path", new Schema<>().type("string"))
                .addProperties("traceId", new Schema<>().type("string"))
                .addProperties("validationErrors", new Schema<>().type("object"))
        );

        return new OpenAPI()
                .components(components)
                .addSecurityItem(globalSec);
    }
}
