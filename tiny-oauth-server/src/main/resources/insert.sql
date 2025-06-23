INSERT INTO `tiny-web`.
    `oauth2_registered_client`
    (`id`,
     `client_id`,
     `client_id_issued_at`,
     `client_secret`,
     `client_secret_expires_at`,
     `client_name`,
     `client_authentication_methods`,
     `authorization_grant_types`,
     `redirect_uris`,
     `post_logout_redirect_uris`,
     `scopes`,
     `client_settings`,
     `token_settings`)
VALUES (
        '3eacac0e-0de9-4727-9a64-6bdd4be2ee1f',
        'oidc-client',
        '2023-07-12 07:33:42',
        '{noop}secret',
        NULL,
        '3eacac0e-0de9-4727-9a64-6bdd4be2ee1f',
        'client_secret_basic',
        'refresh_token,authorization_code',
        'http://www.baidu.com',
        'http://127.0.0.1:8080/',
        'openid,profile',
        '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":true}',
        '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.device-code-time-to-live\":[\"java.time.Duration\",300.000000000]}');