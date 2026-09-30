package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.FirmasDto;
import com.cartera_temp.cartera_temp.Models.Firmas;
import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.Service.FirmasService;
import com.cartera_temp.cartera_temp.Utils.ArchivoResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;


@RestController
@RequestMapping("/api/v1/firmasController")
@CrossOrigin (origins = "*", maxAge = 3600)
public class FirmasController {
    
    private final FirmasService fs;
    private final DriveStorageService driveStorageService;


    public FirmasController(FirmasService fs, DriveStorageService driveStorageService) {
        this.fs = fs;
        this.driveStorageService = driveStorageService;
    }
    
    @PostMapping("/saveFirma")
    public ResponseEntity<Firmas> save (@RequestBody() FirmasDto dto){
        Firmas f = fs.guardarfirmas(dto);
        if(Objects.isNull(f)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(f);
    }
    
    @GetMapping("/obtenerTodasFirmas")
    public ResponseEntity<List<Firmas>> findAll(){
        List<Firmas> f = fs.listarFirmas();
        return ResponseEntity.ok(f);
    }
    
    @GetMapping("/obtenerFirmaByUsername")
    public ResponseEntity<Firmas> findByUsername(@RequestParam(name = "username") String username){
        Firmas f = fs.findFirmaByUsername(username);
        if(Objects.isNull(f)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(f);
    }
    
    @GetMapping("/obtenerFirmasById/{id}")
    public ResponseEntity<Firmas> findById(@PathVariable("id") Long id){
        Firmas f = fs.findFirmaById(id);
        if(Objects.isNull(f)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(f);
    }
    
    // Imagen de la firma como bytes (el frontend la pide con responseType 'blob')
    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable("id") Long id, WebRequest request){
        Firmas f = fs.findFirmaById(id);
        if(Objects.isNull(f)){
            return ResponseEntity.notFound().build();
        }
        return ArchivoResponse.desdeDrive(driveStorageService, f.getRuta(), f.getFilename(), request);
    }

    @DeleteMapping("/DeleteById/{id}")
    public void delete(@PathVariable("id") Long id){
        fs.deleteFirma(id);
    }
    
}
