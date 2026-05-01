package com.n11bc.payment_service.iyzico;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class IyzicoSignatureGeneratorTest {

    @Test
    void generateAuthorization_buildsIyzwsV2Header() {
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1_700_000_000_000L), ZoneOffset.UTC);
        IyzicoSignatureGenerator generator = new IyzicoSignatureGenerator(clock);

        String authorization = generator.generateAuthorization(
                "sandbox-api-key",
                "sandbox-secret-key",
                "/payment/auth",
                "{\"conversationId\":\"order-1\"}"
        );

        assertThat(authorization).startsWith("IYZWSv2 ");
        String decoded = new String(Base64.getDecoder().decode(authorization.substring("IYZWSv2 ".length())), StandardCharsets.UTF_8);
        assertThat(decoded).contains("apiKey:sandbox-api-key");
        assertThat(decoded).contains("randomKey:1700000000000123456789");
        assertThat(decoded).contains("signature:");
    }
}
