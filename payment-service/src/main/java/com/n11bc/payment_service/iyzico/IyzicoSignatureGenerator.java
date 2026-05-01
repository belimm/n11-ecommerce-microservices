package com.n11bc.payment_service.iyzico;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class IyzicoSignatureGenerator {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String RANDOM_SUFFIX = "123456789";
    private final Clock clock;

    public IyzicoSignatureGenerator() {
        this(Clock.systemUTC());
    }

    IyzicoSignatureGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generateAuthorization(String apiKey, String secretKey, String uriPath, String body) {
        String randomKey = clock.millis() + RANDOM_SUFFIX;
        String payload = randomKey + uriPath + (body == null ? "" : body);
        String signature = hmacSha256(payload, secretKey);
        String authorizationString = "apiKey:" + apiKey
                + "&randomKey:" + randomKey
                + "&signature:" + signature;
        String encoded = Base64.getEncoder().encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8));
        return "IYZWSv2 " + encoded;
    }

    public String responseSignature(String secretKey, String... values) {
        return hmacSha256(String.join(":", values), secretKey);
    }

    private String hmacSha256(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IyzicoPaymentException("Unable to generate Iyzico signature", ex);
        }
    }
}
