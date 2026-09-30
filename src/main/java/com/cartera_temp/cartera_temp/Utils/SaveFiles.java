package com.cartera_temp.cartera_temp.Utils;

import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.ServiceImpl.CustomMultipartFile;
import java.io.IOException;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SaveFiles {

    private final DriveStorageService driveStorageService;

    public SaveFiles(DriveStorageService driveStorageService) {
        this.driveStorageService = driveStorageService;
    }

    public MultipartFile convertirFile(String base64) {

        final String[] base64Array = base64.split(",");

        String dataUir, data;

        if (base64Array.length > 1) {
            dataUir = base64Array[0];
            data = base64Array[1];
        } else {
            dataUir = "data:application/pdf;base64";
            data = base64Array[0];
        }

        MultipartFile multipartFile = new CustomMultipartFile(data, dataUir);
        return multipartFile;
    }

    /**
     * Carpeta de Drive (relativa a la raiz) donde se guardan los recibos de una
     * sede. Es la misma estructura que se usaba en /uploads/Recibos/RECIBOS/<sede>.
     */
    public String carpetaRecibos(String sede) {
        return "Recibos/RECIBOS/".concat(sede);
    }

    /**
     * @param ruta fileId de Drive o ruta antigua "/uploads/..."
     */
    public String pdfToBase64(String ruta) throws IOException {
        return Base64.getEncoder().encodeToString(driveStorageService.descargar(ruta));
    }

}
