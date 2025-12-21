package com.tiny.web.oauth2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
@Data
@ConfigurationProperties(prefix = "authentication")
public class ClientProperties {

    private Jwt jwt;
    private List<Client> clients;

    @Data
    public static class Jwt {
        private String publicKeyPath;
        private String privateKeyPath;
    }

    @Data
    public static class Client {
        private String clientId;
        private String clientSecret;
        private List<String> authenticationMethods;
        private List<String> grantTypes;
        private List<String> scopes;
        private List<String> redirectUris;
        private List<String> postLogoutRedirectUris;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}