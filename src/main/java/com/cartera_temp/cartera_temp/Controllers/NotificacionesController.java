
package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.NotificacionRequest;
import com.cartera_temp.cartera_temp.Models.Notificaciones;
import com.cartera_temp.cartera_temp.Service.NotificacionesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v2/notificaciones")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificacionesController {
    
    private final NotificacionesService notificacionesService;

    public NotificacionesController(NotificacionesService notificacionesService) {
        this.notificacionesService = notificacionesService;
    }
    
    @GetMapping("/getAllNotificaciones")
    public ResponseEntity<Page<Notificaciones>> obtenerNotificacionesByAsignated(@RequestParam(name = "username") String username,@RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
       Page<Notificaciones> notificaciones = notificacionesService.getNotificacionesAscendente(username,PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }
    
    @GetMapping("/getAllNotificacionesVencidas")
    public ResponseEntity<Page<Notificaciones>> obtenerNotificacionesVencidasByAsignated(@RequestParam(name = "username") String username,@RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
        Page<Notificaciones> notificaciones = notificacionesService.getNotificacionesVencidasAscendente(username,PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }
    
    @PutMapping("/desactivateNotification")
    public ResponseEntity<HttpStatus> desactivateNotificaciones(@RequestBody() NotificacionRequest notificacionRequest){
        boolean bol = notificacionesService.desactivateNotificacion(notificacionRequest);
        if(bol == false){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/hideNotification")
    public ResponseEntity<HttpStatus> hideNotificaciones(@RequestParam(name = "idNotificion")Long id){
        boolean bol = notificacionesService.hideNotificationChecked(id);
        if(bol == false){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/getAllNotificacionesRealizadas")
    public ResponseEntity<Page<Notificaciones>> obtenerNotificacionesRealizadasByAsignated(@RequestParam(name = "username") String username, @RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
        Page<Notificaciones> notificaciones = notificacionesService.getRealizadas(username, PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }
    
    @GetMapping("/bySedeVencidas")
    public ResponseEntity<Page<Notificaciones>> obtenerPorSede(@RequestParam(name = "sede") String sede, @RequestParam(name = "username") String username, @RequestParam(name = "tipo") String tipo, @RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
        Page<Notificaciones> notificaciones = notificacionesService.findBySede(sede, username,tipo,PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }
    
    @GetMapping("/bySedeAll")
    public ResponseEntity<Page<Notificaciones>> obtenerPorSedeAll(@RequestParam(name = "sede") String sede, @RequestParam(name = "username") String username, @RequestParam(name = "tipo") String tipo, @RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
        Page<Notificaciones> notificaciones = notificacionesService.findBySedeAll(sede, username, tipo, PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }
    
    @GetMapping("/bySedeRealizadas")
    public ResponseEntity<Page<Notificaciones>> obtenerPorSedeRealizadas(@RequestParam(name = "sede") String sede, @RequestParam(name = "username") String username, @RequestParam(name = "tipo") String tipo,@RequestParam(defaultValue = "0", name = "page") int page, @RequestParam(defaultValue = "10", name = "size") int size ){
        Page<Notificaciones> notificaciones = notificacionesService.findBySedeRealizadas(sede, username, tipo,PageRequest.of(page, size));
        return ResponseEntity.ok(notificaciones);
    }

}
