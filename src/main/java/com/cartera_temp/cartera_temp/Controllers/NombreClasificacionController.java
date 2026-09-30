package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.NombreClasificacionDto;
import com.cartera_temp.cartera_temp.Models.NombresClasificacion;
import com.cartera_temp.cartera_temp.Service.NombreClasificacionService;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v2/nombreClasificacion")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NombreClasificacionController {
    
    private final NombreClasificacionService nombreClasificacionService;

    public NombreClasificacionController(NombreClasificacionService nombreClasificacionService) {
        this.nombreClasificacionService = nombreClasificacionService;
    }
    
    @GetMapping("/clasificaciones")
    public ResponseEntity<List<NombresClasificacion>> obtenerClasificaciones(){
        List<NombresClasificacion> clasificaciones = nombreClasificacionService.obtenerTodosNombresClasificacion();
        return ResponseEntity.ok(clasificaciones);
    }
    
    @GetMapping("/clasificacionesByNombre")
    public ResponseEntity<NombresClasificacion> clasificacionesByNombre(@RequestParam(name = "nombre") String nombre){
        NombresClasificacion clasificaciones = nombreClasificacionService.obtenerNombresClasificacionByNombre(nombre);
        return Objects.isNull(clasificaciones)?ResponseEntity.badRequest().build():ResponseEntity.ok(clasificaciones);
    }
    
    @PostMapping("/guardarClasificacion")
    public ResponseEntity<NombresClasificacion> guardarClasificacion(@RequestBody NombreClasificacionDto clasificacionDto){
        NombresClasificacion clasificacion = nombreClasificacionService.guardarNombresClasificacion(clasificacionDto);
        return Objects.isNull(clasificacion)?ResponseEntity.badRequest().build():ResponseEntity.ok(clasificacion);
    }
    
    @GetMapping("/clasificacionById")
    public ResponseEntity<NombresClasificacion> obtenerClasificacionById(@RequestParam(name = "id") Long id){
        NombresClasificacion clasificacion = nombreClasificacionService.obtenerNombresClasificacionById(id);
        return  Objects.isNull(clasificacion)?ResponseEntity.badRequest().build():ResponseEntity.ok(clasificacion);
    }
    
    @DeleteMapping("/clasificacion")
    public ResponseEntity<HttpStatus> eliminarClasificacion(@RequestParam(name = "id") Long id){
        nombreClasificacionService.eliminarNombreClasificacion(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
