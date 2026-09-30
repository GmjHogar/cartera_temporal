
package com.cartera_temp.cartera_temp.Controllers;

import com.cartera_temp.cartera_temp.Dtos.ItemsFiltros;
import com.cartera_temp.cartera_temp.ServiceImpl.FiltrosServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/items")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ItemsFiltrosController {
    
    
    private final FiltrosServiceImpl filtrosServiceImpl;

    public ItemsFiltrosController(FiltrosServiceImpl filtrosServiceImpl) {
        this.filtrosServiceImpl = filtrosServiceImpl;
    }

    
   @GetMapping("/")
    public ResponseEntity<ItemsFiltros> itemsByAsesor(@RequestParam("username") String username){
        ItemsFiltros items = filtrosServiceImpl.sedeByUsuario(username);
        return ResponseEntity.ok(items);
    }

}
