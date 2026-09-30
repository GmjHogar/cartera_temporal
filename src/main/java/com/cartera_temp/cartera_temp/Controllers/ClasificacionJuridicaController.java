package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Models.ClasificacionJuridica;
import com.cartera_temp.cartera_temp.Service.ClasificacionJuridicaService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v2/clasificacionJuridica")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClasificacionJuridicaController {

    private final ClasificacionJuridicaService cjs;

    public ClasificacionJuridicaController(ClasificacionJuridicaService cjs) {
        this.cjs = cjs;
    }
    
    @PostMapping("/saveClasificacionJuridica")
    public ResponseEntity<ClasificacionJuridica> save (@RequestBody ClasificacionJuridica cj){
        ClasificacionJuridica cjS = cjs.saveClasificacion(cj);
        if(Objects.isNull(cjS)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cjS);
    }
    
    @GetMapping("/getAllClasificacionJuridica")
    public ResponseEntity<List<ClasificacionJuridica>> getAll(){
        List<ClasificacionJuridica> cj = cjs.getAllClasificacion();
        return ResponseEntity.ok(cj);
    }
    
    @GetMapping("/getClasificacionJuridicaByNombre")
    public ResponseEntity<ClasificacionJuridica> getByNombre(@RequestParam String nombre){
        ClasificacionJuridica cj = cjs.getClasificacionByNombre(nombre);
        if(Objects.isNull(cj)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cj);
    }
    
    @GetMapping("/getClasificacionJuridicaById/{id}")
    public ResponseEntity<ClasificacionJuridica> getById(@PathVariable Long id){
        ClasificacionJuridica cj = cjs.getClasificacionById(id);
        if(Objects.isNull(cj)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cj);
    }
    
    @PutMapping("/updateClasificacionJuridica")
    public ResponseEntity<ClasificacionJuridica> update (@RequestBody ClasificacionJuridica cj){
        ClasificacionJuridica cjUpdate = cjs.updateClasificacion(cj);
        if(Objects.isNull(cjUpdate)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cjUpdate);
    }
    
}
