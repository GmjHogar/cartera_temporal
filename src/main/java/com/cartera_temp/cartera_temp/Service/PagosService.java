package com.cartera_temp.cartera_temp.Service;

import com.cartera_temp.cartera_temp.Dtos.PagosCuotasDto;
import com.cartera_temp.cartera_temp.Dtos.PagosCuotasResponse;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.Pagos;
import com.cartera_temp.cartera_temp.Models.ReciboPago;

public interface PagosService {
    
    public PagosCuotasResponse guardarPago(PagosCuotasDto dto);

    public ReciboPago findReciboById(Long idRecibo);

}
