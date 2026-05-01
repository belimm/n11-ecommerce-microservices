package com.n11bc.user_service.config;

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
                title = "User Service API",
                version = "1.0.0",
                description = "N11 e-commerce platformu icin kullanici yonetim servisi. " +
                        "Kullanici kayit/giris, profil yonetimi ve adres yonetimi islemlerini kapsar.",
                contact = @Contact(name = "N11 Backend Team")
        ),
        servers = {
                @Server(url = "http://localhost:8763", description = "API Gateway"),
                @Server(url = "http://localhost:8083", description = "Direct Access")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Keycloak JWT access token. /api/auth/signin endpoint'inden alinir."
)
public class OpenApiConfig {
}
