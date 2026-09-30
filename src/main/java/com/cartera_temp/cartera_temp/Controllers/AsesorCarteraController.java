package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.AsesorCarteraResponse;
import com.cartera_temp.cartera_temp.Models.AsesorCartera;
import com.cartera_temp.cartera_temp.Service.AsesorCarteraService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v2/asesorCartera")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AsesorCarteraController {
    
    private final AsesorCarteraService acs;

    public AsesorCarteraController(AsesorCarteraService acs) {
        this.acs = acs;
    }
    
    @GetMapping("/getAllAsesores")
    public ResponseEntity<List<AsesorCarteraResponse>> getAllASesores(){
        List<AsesorCarteraResponse> asesor = acs.listarAsesores();
        return ResponseEntity.ok(asesor );
    }
    
}
