package com.cartera_temp.cartera_temp.Utils;

import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

/**
 * Respuesta HTTP con los bytes de un archivo guardado en Drive.
 *
 * 200 con el archivo, 304 si el navegador ya lo tiene (el archivo de una
 * referencia nunca cambia), 404 si no existe y 500 si falla Drive.
 */
public final class ArchivoResponse {

    private ArchivoResponse() {
    }

    public static ResponseEntity<byte[]> desdeDrive(DriveStorageService drive, String referencia,
            String nombre, WebRequest request) {

        if (referencia == null || referencia.trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String etag = "\"" + Integer.toHexString(referencia.hashCode()) + "\"";
        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        byte[] bytes;
        try {
            bytes = drive.descargar(referencia);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArchivoResponse.class.getName()).log(Level.WARNING, ex.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException ex) {
            Logger.getLogger(ArchivoResponse.class.getName()).log(Level.SEVERE, "Error descargando de Drive: " + referencia, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "archivo";
        }
        MediaType tipo = MediaTypeFactory.getMediaType(nombre).orElse(MediaType.APPLICATION_OCTET_STREAM);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(tipo);
        headers.setContentDisposition(ContentDisposition.inline().filename(nombre).build());
        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePrivate());
        headers.setETag(etag);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
