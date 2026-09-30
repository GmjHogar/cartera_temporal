package com.cartera_temp.cartera_temp.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente de Google Drive con OAuth de usuario (refresh token).
 *
 * No se usa cuenta de servicio porque no tiene cuota de almacenamiento y las
 * subidas fallan. La app OAuth debe estar PUBLICADA ("En produccion"): en modo
 * prueba el refresh token vence cada 7 dias.
 */
@Configuration
public class GoogleDriveConfig {

    @Value("${DRIVE_CLIENT_ID:}")
    private String clientId;

    @Value("${DRIVE_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${DRIVE_REFRESH_TOKEN:}")
    private String refreshToken;

    @Bean
    public Drive drive() throws GeneralSecurityException, IOException {
        requerida("DRIVE_CLIENT_ID", clientId);
        requerida("DRIVE_CLIENT_SECRET", clientSecret);
        requerida("DRIVE_REFRESH_TOKEN", refreshToken);

        UserCredentials credenciales = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(credenciales);
        HttpRequestInitializer init = request -> {
            adapter.initialize(request);
            request.setConnectTimeout(20000);
            request.setReadTimeout(60000);
        };

        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), init)
                .setApplicationName("cartera-temporal-microservice")
                .build();
    }

    // Mejor que el servicio no arranque a que falle la primera vez que alguien sube un archivo
    static void requerida(String nombre, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException("Falta la variable de entorno " + nombre);
        }
    }
}
