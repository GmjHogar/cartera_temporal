package com.cartera_temp.cartera_temp.Service;

import java.io.IOException;

public interface DriveStorageService {

    /**
     * Sube un archivo a Drive.
     *
     * @param carpeta ruta relativa a la carpeta raiz, p.ej. "Firmas" o
     * "Recibos/RECIBOS/ANDES". Las carpetas que no existan se crean.
     * @return el fileId de Drive, que es lo que se guarda en la base de datos.
     */
    String subir(byte[] contenido, String nombre, String mimeType, String carpeta) throws IOException;

    /**
     * Descarga un archivo de Drive.
     *
     * @param referencia un fileId o una ruta antigua "/uploads/...". Los ids de
     * Drive nunca contienen "/", asi se distinguen.
     * @throws java.io.FileNotFoundException si el archivo no existe.
     */
    byte[] descargar(String referencia) throws IOException;
}
