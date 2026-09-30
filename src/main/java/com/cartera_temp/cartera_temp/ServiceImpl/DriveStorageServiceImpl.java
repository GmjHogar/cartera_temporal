package com.cartera_temp.cartera_temp.ServiceImpl;

import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DriveStorageServiceImpl implements DriveStorageService {

    private static final String PREFIJO_ANTIGUO = "/uploads/";
    private static final String MIME_CARPETA = "application/vnd.google-apps.folder";

    private final Drive drive;

    // Carpeta que antes se montaba con rclone (contiene Firmas/ y Recibos/)
    @Value("${DRIVE_ROOT_FOLDER_ID:}")
    private String rootFolderId;

    // Ruta relativa -> id, para carpetas y archivos ya encontrados. Se pierde al reiniciar.
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public DriveStorageServiceImpl(Drive drive) {
        this.drive = drive;
    }

    @PostConstruct
    void validar() {
        if (rootFolderId == null || rootFolderId.trim().isEmpty()) {
            throw new IllegalStateException("Falta la variable de entorno DRIVE_ROOT_FOLDER_ID");
        }
    }

    @Override
    public String subir(byte[] contenido, String nombre, String mimeType, String carpeta) throws IOException {
        String carpetaId = carpeta(carpeta, true);

        File metadata = new File();
        metadata.setName(nombre);
        metadata.setParents(Collections.singletonList(carpetaId));

        File creado = drive.files()
                .create(metadata, new ByteArrayContent(mimeType, contenido))
                .setFields("id")
                .execute();
        return creado.getId();
    }

    @Override
    public byte[] descargar(String referencia) throws IOException {
        if (referencia == null || referencia.trim().isEmpty()) {
            throw new FileNotFoundException("Referencia de archivo vacia");
        }

        String fileId = referencia.startsWith("/") ? idDeRutaAntigua(referencia) : referencia;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            drive.files().get(fileId).executeMediaAndDownloadTo(out);
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException ex) {
            if (ex.getStatusCode() == 404) {
                throw new FileNotFoundException("No existe en Drive: " + referencia);
            }
            throw ex;
        }
        return out.toByteArray();
    }

    // "/uploads/Recibos/RECIBOS/ANDES/file.pdf" -> busca Recibos/RECIBOS/ANDES y el archivo por nombre
    private String idDeRutaAntigua(String ruta) throws IOException {
        if (!ruta.startsWith(PREFIJO_ANTIGUO)) {
            throw new FileNotFoundException("Ruta no reconocida: " + ruta);
        }
        String relativa = ruta.substring(PREFIJO_ANTIGUO.length());

        String enCache = cache.get(relativa);
        if (enCache != null) {
            return enCache;
        }

        int corte = relativa.lastIndexOf('/');
        String carpeta = corte < 0 ? "" : relativa.substring(0, corte);
        String nombre = relativa.substring(corte + 1);

        String carpetaId = carpeta(carpeta, false);
        String fileId = carpetaId == null ? null : buscar(nombre, carpetaId, false);
        if (fileId == null) {
            throw new FileNotFoundException("No existe en Drive: " + ruta);
        }
        cache.put(relativa, fileId);
        return fileId;
    }

    /**
     * Recorre la ruta de carpetas desde la raiz. Con crear=false devuelve null si
     * alguna no existe. synchronized para que dos subidas simultaneas no creen la
     * misma carpeta dos veces.
     */
    private synchronized String carpeta(String ruta, boolean crear) throws IOException {
        String actual = rootFolderId;
        String acumulada = "";
        for (String segmento : ruta.split("/")) {
            if (segmento.isEmpty()) {
                continue;
            }
            acumulada = acumulada.isEmpty() ? segmento : acumulada + "/" + segmento;

            String id = cache.get(acumulada + "/");
            if (id == null) {
                id = buscar(segmento, actual, true);
                if (id == null) {
                    if (!crear) {
                        return null;
                    }
                    id = crearCarpeta(segmento, actual);
                }
                cache.put(acumulada + "/", id);
            }
            actual = id;
        }
        return actual;
    }

    private String crearCarpeta(String nombre, String parentId) throws IOException {
        File metadata = new File();
        metadata.setName(nombre);
        metadata.setMimeType(MIME_CARPETA);
        metadata.setParents(Collections.singletonList(parentId));
        return drive.files().create(metadata).setFields("id").execute().getId();
    }

    private String buscar(String nombre, String parentId, boolean esCarpeta) throws IOException {
        String escapado = nombre.replace("\\", "\\\\").replace("'", "\\'");
        String q = "name = '" + escapado + "' and '" + parentId + "' in parents and trashed = false"
                + " and mimeType " + (esCarpeta ? "=" : "!=") + " '" + MIME_CARPETA + "'";

        FileList resultado = drive.files().list()
                .setQ(q)
                .setPageSize(1)
                .setFields("files(id)")
                .execute();
        List<File> archivos = resultado.getFiles();
        return archivos == null || archivos.isEmpty() ? null : archivos.get(0).getId();
    }
}
