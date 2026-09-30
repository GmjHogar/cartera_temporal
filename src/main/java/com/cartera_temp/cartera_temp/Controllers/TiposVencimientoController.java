package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Models.TiposVencimiento;
import com.cartera_temp.cartera_temp.Service.TiposVencimientoService;
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
@RequestMapping("/api/v2/tiposVencimiento")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TiposVencimientoController {
    
    private final TiposVencimientoService tvs;

    public TiposVencimientoController(TiposVencimientoService tvs) {
        this.tvs = tvs;
    }
    
    @PostMapping("/guardarTipoVencimiento")
    public ResponseEntity<TiposVencimiento> save (@RequestBody TiposVencimiento tv){
        TiposVencimiento tvSave = tvs.guardarTipoVencimiento(tv);
        if(Objects.isNull(tvSave)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tvSave);
    }
    
    @GetMapping("/obtenerTodosTiposVencimientos")
    public ResponseEntity<List<TiposVencimiento>> getAll (){
        List<TiposVencimiento> tv = tvs.obtenerTiposVencimiento();
        return ResponseEntity.ok(tv);
    }
    
    @GetMapping("/obtenerTiposVencimientoPorNombre")
    public ResponseEntity<TiposVencimiento> getByNombre(@RequestParam("tv_nombre") String tvNombre){
        TiposVencimiento tv = tvs.obtenerTipoVencimientoByNombre(tvNombre);
        if(Objects.isNull(tv))
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(tv);
    }
    
    @GetMapping("/obtenerTiposVencimientoPorId/{id}")
    public ResponseEntity<TiposVencimiento> getById (@PathVariable("id")Long idTv){
        TiposVencimiento tv = tvs.obtenerTipoVencimientoById(idTv);
        if(Objects.isNull(tv)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tv);
    }
    
    @PutMapping("/updateTipoVencimiento")
    public ResponseEntity<TiposVencimiento> update(@RequestBody TiposVencimiento tv){
        TiposVencimiento tvUpdate = tvs.actualizarTipoVencimiento(tv);
        if(Objects.isNull(tvUpdate)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tvUpdate);
    }
    
}
