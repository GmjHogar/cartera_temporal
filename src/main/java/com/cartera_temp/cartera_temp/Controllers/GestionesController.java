package com.cartera_temp.cartera_temp.Controllers;

import GestionesDataDto.GestionesDataDto;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.cartera_temp.cartera_temp.Dtos.AlertsGestiones;
import com.cartera_temp.cartera_temp.Dtos.GestionResponse;
import com.cartera_temp.cartera_temp.Dtos.GestionToSaveDto;
import com.cartera_temp.cartera_temp.Dtos.LinkDto;
import com.cartera_temp.cartera_temp.Dtos.LinkToClient;
import com.cartera_temp.cartera_temp.Dtos.MessageResponse;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.Cuotas;
import com.cartera_temp.cartera_temp.Models.Gestiones;
import com.cartera_temp.cartera_temp.Service.GestionesService;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v2/gestiones")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GestionesController {

    private final GestionesService gestionesService;

    public GestionesController(com.cartera_temp.cartera_temp.Service.GestionesService gestionesService) {
        this.gestionesService = gestionesService;
    }

    @PostMapping("/saveOneGestion")
    public ResponseEntity<GestionResponse> saveOneGestion(@RequestBody GestionToSaveDto dto) {
        GestionResponse gestion = gestionesService.saveOneGestion(dto);
        if (Objects.isNull(gestion)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(gestion);
    }

    @GetMapping("/getGestionByNumObligacion/{num_obligacion}")
    public ResponseEntity<List<GestionResponse>> getGestionesByNumObligacion(@PathVariable("num_obligacion") String numeroObligacion) {
        List<GestionResponse> gestion = gestionesService.findHistoricoGestiones(numeroObligacion);
        return ResponseEntity.ok(gestion);
    }

    @PostMapping("/saveGestiones")
    public ResponseEntity<List<Gestiones>> guardarGestiones(@RequestBody GestionesDataDto dataDto) {
        List<Gestiones> gestiones = gestionesService.saveMultipleGestiones(dataDto);

        return ResponseEntity.ok(gestiones);
    }

    @GetMapping("/getLastDatoAdicionalGestion/{num_obligacion}")
    public ResponseEntity<MessageResponse> getLastDatoAdicional(@PathVariable("num_obligacion") String numeroObligacion) {

        String datoAdicional = gestionesService.sendLastDatoAdicional(numeroObligacion);
        return ResponseEntity.ok(new MessageResponse(datoAdicional));

    }

    @PutMapping("/desactivateAcuerdoPago/{id}")
    public void desactivateAcuerpoPago(@PathVariable("id") Long idAcuerdoPago) {
        gestionesService.desactivateAcuerdoPago(idAcuerdoPago);
    }

    @PutMapping("/linkAndReporteAcuerdoToClient")
    public ResponseEntity<LinkToClient> link(@RequestBody LinkDto dto) {
        LinkToClient linkClient = gestionesService.sendLinkAndPdfToClient(dto);
        if (Objects.isNull(linkClient)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(linkClient);
    }

    @PutMapping("/cuotaPago")
    public ResponseEntity<List<Cuotas>> enviarCuotaPagadaCartera(@RequestBody List<Long> ids) {
        List<Cuotas> cuota = gestionesService.cuotaCumplio(ids);
        if (Objects.isNull(cuota)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cuota);
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<AlertsGestiones> alertsGestiones(@RequestParam("username") String username, @RequestParam("fecha") String fecha){
        AlertsGestiones alerts = gestionesService.alertasDeGestiones(username,fecha);
        if (Objects.isNull(alerts)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(alerts);
    }
    
    
      @GetMapping("/desactivateGestion")
    public ResponseEntity<HttpStatus> desactivarGestion(@RequestParam("idGestion") Long idGestion){
        boolean desativate = gestionesService.desactivarGestiones(idGestion);
        return desativate ? ResponseEntity.ok(HttpStatus.OK):ResponseEntity.badRequest().build();
    }


    @GetMapping("/obtenerCuentasSinGestion")
    public ResponseEntity<Object> obtenerCuentasSinGestion(@RequestParam("username") String username,
            @RequestParam("page") Integer page, @RequestParam("size") Integer size) {

        
        ResponseEntity<Object> response = gestionesService.obtenerCuentasSinGestion(username, PageRequest.of(page, size));
        return response;
    }

    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping("/obtenerAcuerdosPagoActivosVencidos")
    public ResponseEntity<Object> obtenerAcuerdosPagoActivosVencidos(@RequestParam("username") String username,
            @RequestParam("page") Integer page, @RequestParam("size") Integer size) {

        
        ResponseEntity<Object> response = gestionesService.obtenerAcuerdosPagoActivosVencidos(username, PageRequest.of(page, size));
        return response;
    }
}
