package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Models.Sede;
import com.cartera_temp.cartera_temp.Service.SedeService;
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
@RequestMapping("/api/v2/sedesController")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SedeController {
    
    private final SedeService ss;

    public SedeController(SedeService ss) {
        this.ss = ss;
    }
    
    @PostMapping("/guardarSede")
    public ResponseEntity<Sede> save (@RequestBody Sede sedeObj){
        
        Sede sede = ss.guardarSede(sedeObj);
        if(Objects.isNull(sede)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sede);
        
    }
    
    @GetMapping("/obtenerTodasSedes")
    public ResponseEntity<List<Sede>> findAll(){
        List<Sede> sede = ss.listarSede();
        return ResponseEntity.ok(sede);
    }
    
    @GetMapping("/obtenerSedeByNombre")
    public ResponseEntity<Sede> findBySede(@RequestParam String nombre){
        Sede sede = ss.findSede(nombre);
        if(Objects.isNull(sede)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sede);
    }
    
    @GetMapping("/obtenerSedeById/{id}")
    public ResponseEntity<Sede> findById(@PathVariable("id")Long idSede){
        Sede sede = ss.findById(idSede);
        if(Objects.isNull(sede)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sede);
    }
    
    @PutMapping("/actualizarSede")
    public ResponseEntity<Sede> update(@RequestBody Sede sede){
        Sede sedeUpdate = ss.actualizarSede(sede);
        if(Objects.isNull(sedeUpdate)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sedeUpdate);
    }
    
}
