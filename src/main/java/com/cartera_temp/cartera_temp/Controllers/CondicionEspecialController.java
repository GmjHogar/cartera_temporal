package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Models.CondicionEspecial;
import com.cartera_temp.cartera_temp.Service.CondicionEspecialService;
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
@RequestMapping("/api/v2/condicionEspecial")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CondicionEspecialController {
    
    private final CondicionEspecialService ces;

    public CondicionEspecialController(CondicionEspecialService ces) {
        this.ces = ces;
    }
    
    @PostMapping("/saveCondicionEspecial")
    public ResponseEntity<CondicionEspecial> save (@RequestBody CondicionEspecial ce){
        CondicionEspecial ceSave = ces.saveCondicion(ce);
        if(Objects.isNull(ceSave)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ceSave);
    }
    
    @GetMapping("/getAllCondiciones")
    public ResponseEntity<List<CondicionEspecial>> getAll(){
        List<CondicionEspecial> ce = ces.getAllCondicion();
        return ResponseEntity.ok(ce);
    }
    
    @GetMapping("/getCondicionByNombre")
    public ResponseEntity<CondicionEspecial> getByNombre(@RequestParam String nombre){
        CondicionEspecial ce = ces.getCondicionByNombre(nombre);
        if(Objects.isNull(ce)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ce);
    }
    
    @GetMapping("/getCondicionById/{id}")
    public ResponseEntity<CondicionEspecial> getById(@PathVariable("id") Long id){
        CondicionEspecial ce = ces.getCondicionById(id);
        if(Objects.isNull(ce)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ce);
    }
    
    @PutMapping("/updateCondicion")
    public ResponseEntity<CondicionEspecial> update(@RequestBody CondicionEspecial ce){
        CondicionEspecial ceSave = ces.updateCondicion(ce);
        if(Objects.isNull(ceSave)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ceSave);
    }
    
}
