package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.PagosCuotasDto;
import com.cartera_temp.cartera_temp.Dtos.PagosCuotasResponse;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.ReciboPago;
import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.Service.PagosService;
import com.cartera_temp.cartera_temp.Utils.ArchivoResponse;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;


@RestController
@RequestMapping("/api/v1/pagos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PagosController {
    private final PagosService ps;
    private final DriveStorageService driveStorageService;

    public PagosController(PagosService ps, DriveStorageService driveStorageService) {
        this.ps = ps;
        this.driveStorageService = driveStorageService;
    }
    
    @PostMapping("/guardarPago")
    public ResponseEntity<PagosCuotasResponse> guardarPago (@RequestBody PagosCuotasDto dto){
        PagosCuotasResponse acu = ps.guardarPago(dto);
        if(Objects.isNull(acu)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(acu);
    }

    // PDF del recibo como bytes (el frontend lo pide con responseType 'blob')
    @GetMapping("/recibo/{idRecibo}/archivo")
    public ResponseEntity<byte[]> archivoRecibo(@PathVariable("idRecibo") Long idRecibo, WebRequest request) {
        ReciboPago recibo = ps.findReciboById(idRecibo);
        if (Objects.isNull(recibo)) {
            return ResponseEntity.notFound().build();
        }
        return ArchivoResponse.desdeDrive(driveStorageService, recibo.getRuta(), recibo.getNombreArchivo(), request);
    }

}
