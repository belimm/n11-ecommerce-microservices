package com.n11bc.cart_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cart Service API",
                version = "1.0.0",
                description = "Authenticated shopping cart API with product snapshots and abandoned cart events.",
                contact = @Contact(name = "N11 Backend Team")
        ),
        servers = {
                @Server(url = "http://localhost:8763", description = "API Gateway"),
                @Server(url = "http://localhost:8085", description = "Direct Access")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Keycloak JWT access token. User identity is read from the token subject."
)
public class OpenApiConfig {
}
