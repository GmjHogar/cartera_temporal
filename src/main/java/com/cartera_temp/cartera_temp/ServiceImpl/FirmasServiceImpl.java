package com.cartera_temp.cartera_temp.ServiceImpl;

import com.cartera_temp.cartera_temp.Dtos.FirmasDto;
import com.cartera_temp.cartera_temp.Models.Firmas;
import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.Service.FirmasService;
import com.cartera_temp.cartera_temp.repository.FirmasRespository;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FirmasServiceImpl implements FirmasService {

    // Carpeta de Drive relativa a la raiz (antes /uploads/Firmas)
    private static final String CARPETA_FIRMAS = "Firmas";

    private final FirmasRespository fr;
    private final DriveStorageService driveStorageService;

    public FirmasServiceImpl(FirmasRespository fr, DriveStorageService driveStorageService) {
        this.fr = fr;
        this.driveStorageService = driveStorageService;

    }

    @Override
    public Firmas guardarfirmas(FirmasDto dto) {

        if (dto.getBase64() == null || dto.getBase64() == "" || dto.getUsername() == "" || dto.getUsername() == null) {
            return null;
        }

        Firmas fFind = fr.findByUsername(dto.getUsername());
        if (Objects.nonNull(fFind)) {
            return null;
        }

        // Se espera un data URI: "data:image/png;base64,...."
        String[] data = dto.getBase64().split(",");
        if (data.length < 2 || !data[0].contains(":") || !data[0].contains("/")) {
            return null;
        }

        MultipartFile firma = new CustomMultipartFile(data[1], data[0]);

        String nombreFirmaBd = firma.getOriginalFilename();

        // En la columna ruta se guarda el fileId de Drive
        String fileId;
        try {
            fileId = driveStorageService.subir(firma.getBytes(), nombreFirmaBd, firma.getContentType(), CARPETA_FIRMAS);
        } catch (IOException ex) {
            Logger.getLogger(FirmasServiceImpl.class.getName()).log(Level.SEVERE, "No se pudo subir la firma a Drive", ex);
            return null;
        }

        Firmas fSave = new Firmas();

        fSave.setFilename(nombreFirmaBd);
        fSave.setUsername(dto.getUsername());
        fSave.setRuta(fileId);
        fSave = fr.save(fSave);
        return fSave;

    }

    @Override
    public List<Firmas> listarFirmas() {

        List<Firmas> firmasList = fr.findAll();

        return firmasList;

    }

    @Override
    public Firmas findFirmaByUsername(String username) {

        if (username == "" || username == null) {
            return null;
        }

        Firmas firmaUsu = fr.findByUsername(username);
        if (Objects.isNull(firmaUsu)) {
            return null;
        }
        return firmaUsu;

    }

    @Override
    public Firmas findFirmaById(Long id) {

        if (id == null || id == 0) {
            return null;
        }

        Firmas fId = fr.findById(id).orElse(null);
        if (Objects.isNull(fId)) {
            return null;
        }

        return fId;

    }

    @Override
    public void deleteFirma(Long id) {

        fr.deleteById(id);

    }

}
