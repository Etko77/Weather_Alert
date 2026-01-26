package org.example.weather_alert.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
*/
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI weatherAlertOpenAPI() {
        // Define the security scheme (JWT Bearer token)
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("Weather Alert Service API")
                        .description("""
                                Secure REST API for managing weather alerts with background geo-tagging.
                                
                                ## Features
                                - CRUD operations for weather alerts
                                - Automatic geo-tagging using OpenStreetMap Nominatim API
                                - JWT-based authentication
                                - Role-based access control (ADMIN, USER)
                                
                                ## Authentication
                                1. Use the `/api/auth/login` endpoint to get a JWT token
                                2. Click the 'Authorize' button and enter: `Bearer <your-token>`
                                3. All subsequent requests will include the token
                                
                                ## Roles
                                - **ADMIN**: Full CRUD access
                                - **USER**: Read-only access (GET endpoints only)
                                
                                ## Test Credentials
                                - Admin: username=`admin`, password=`admin123`
                                - User: username=`user1`, password=`user123`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Weather Alert Team")
                                .email("support@weatheralert.com")
                                .url("https://weatheralert.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // Server configuration
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development server")))

                // Security configuration
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")));
    }
}
