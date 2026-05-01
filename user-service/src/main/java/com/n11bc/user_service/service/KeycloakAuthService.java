package com.n11bc.user_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bc.user_service.dto.response.KeycloakTokenResponse;
import com.n11bc.user_service.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthService {

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.grant-type-password}")
    private String grantTypePassword;

    @Value("${keycloak.grant-type-refresh}")
    private String grantTypeRefresh;

    @Value("${keycloak.scope}")
    private String scope;

    private final ObjectMapper objectMapper;

    /**
     * Authenticate user with username and password via Keycloak
     */
    public KeycloakTokenResponse authenticate(String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(tokenUri);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", grantTypePassword));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("scope", scope));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return KeycloakTokenResponse.builder()
                            .accessToken(jsonNode.get("access_token").asText())
                            .refreshToken(jsonNode.get("refresh_token").asText())
                            .expiresIn(jsonNode.get("expires_in").asInt())
                            .refreshExpiresIn(jsonNode.get("refresh_expires_in").asInt())
                            .tokenType(jsonNode.get("token_type").asText())
                            .build();
                } else {
                    log.error("Keycloak authentication failed. Status: {}, Response: {}", statusCode, responseBody);
                    throw new AuthenticationException("Invalid username or password");
                }
            });

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error communicating with Keycloak", e);
            throw new AuthenticationException("Authentication service unavailable", e);
        }
    }

    /**
     * Refresh access token using refresh token via Keycloak
     */
    public KeycloakTokenResponse refreshToken(String refreshToken) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(tokenUri);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", grantTypeRefresh));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return KeycloakTokenResponse.builder()
                            .accessToken(jsonNode.get("access_token").asText())
                            .refreshToken(jsonNode.has("refresh_token") ?
                                    jsonNode.get("refresh_token").asText() : refreshToken)
                            .expiresIn(jsonNode.get("expires_in").asInt())
                            .refreshExpiresIn(jsonNode.has("refresh_expires_in") ?
                                    jsonNode.get("refresh_expires_in").asInt() : 0)
                            .tokenType(jsonNode.get("token_type").asText())
                            .build();
                } else {
                    log.error("Keycloak token refresh failed. Status: {}, Response: {}", statusCode, responseBody);
                    throw new AuthenticationException("Invalid or expired refresh token");
                }
            });

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token with Keycloak", e);
            throw new AuthenticationException("Token refresh service unavailable", e);
        }
    }
}
