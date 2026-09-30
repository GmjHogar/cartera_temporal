package com.cartera_temp.cartera_temp.ServiceImpl;

import com.cartera_temp.cartera_temp.Components.GenerarPdf;
import com.cartera_temp.cartera_temp.Dtos.CuentasPorCobrarResponse;
import com.cartera_temp.cartera_temp.Dtos.CuotasDto;
import com.cartera_temp.cartera_temp.Dtos.PagosCuotasDto;
import com.cartera_temp.cartera_temp.Dtos.PagosCuotasResponse;
import com.cartera_temp.cartera_temp.FeignClients.usuario_client;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.AsesorCartera;
import com.cartera_temp.cartera_temp.Models.CuentasPorCobrar;
import com.cartera_temp.cartera_temp.Models.Cuotas;
import com.cartera_temp.cartera_temp.Models.Gestiones;
import com.cartera_temp.cartera_temp.Models.NombresClasificacion;
import com.cartera_temp.cartera_temp.Models.Nota;
import com.cartera_temp.cartera_temp.Models.Notificaciones;
import com.cartera_temp.cartera_temp.Models.Pagos;
import com.cartera_temp.cartera_temp.Models.ReciboPago;
import com.cartera_temp.cartera_temp.ModelsClients.Usuario;
import com.cartera_temp.cartera_temp.Service.CuentasPorCobrarService;
import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.Service.PagosService;
import com.cartera_temp.cartera_temp.Utils.Functions;
import com.cartera_temp.cartera_temp.Utils.SaveFiles;
import com.cartera_temp.cartera_temp.repository.AcuerdoPagoRepository;
import com.cartera_temp.cartera_temp.repository.AsesorCarteraRepository;
import com.cartera_temp.cartera_temp.repository.CuentasPorCobrarRepository;
import com.cartera_temp.cartera_temp.repository.GestionesRepository;
import com.cartera_temp.cartera_temp.repository.NombresClasificacionRepository;
import com.cartera_temp.cartera_temp.repository.NotaRepository;
import com.cartera_temp.cartera_temp.repository.NotificacionesRepository;
import com.cartera_temp.cartera_temp.repository.PagosRespositoty;
import com.cartera_temp.cartera_temp.repository.ReciboPagoRepository;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PagosServiceImpl implements PagosService {

    private final PagosRespositoty pagosRespositoty;
    private final CuentasPorCobrarRepository cpcr;
    private final usuario_client usuClient;
    private final GestionesRepository gr;
    private final AcuerdoPagoRepository apr;
    private final GenerarPdf generarPdf;
    private final SaveFiles saveFiles;
    private final ReciboPagoRepository reciboPagoRepository;
    private final AsesorCarteraRepository asesorCarteraRepository;
    private final NotaRepository notaRepository;
    private final NombresClasificacionRepository nombresClasificacionRepository;
    private final NotificacionesRepository notificacionesRepository;
    private final DriveStorageService driveStorageService;

    public PagosServiceImpl(PagosRespositoty pagosRespositoty, CuentasPorCobrarRepository cpcr,
            usuario_client usuClient, GestionesRepository gr, AcuerdoPagoRepository apr, GenerarPdf generarPdf,
            SaveFiles saveFiles, ReciboPagoRepository reciboPagoRepository,
            AsesorCarteraRepository asesorCarteraRepository, NotaRepository notaRepository,
            NombresClasificacionRepository nombresClasificacionRepository,
            NotificacionesRepository notificacionesRepository, DriveStorageService driveStorageService) {
        this.pagosRespositoty = pagosRespositoty;
        this.cpcr = cpcr;
        this.usuClient = usuClient;
        this.gr = gr;
        this.apr = apr;
        this.generarPdf = generarPdf;
        this.saveFiles = saveFiles;
        this.reciboPagoRepository = reciboPagoRepository;
        this.asesorCarteraRepository = asesorCarteraRepository;
        this.notaRepository = notaRepository;
        this.nombresClasificacionRepository = nombresClasificacionRepository;
        this.notificacionesRepository = notificacionesRepository;
        this.driveStorageService = driveStorageService;
    }

    @Override
    public PagosCuotasResponse guardarPago(PagosCuotasDto dto) {

        if (dto.getNumeroObligacion() == "" || dto.getNumeroObligacion() == null || dto.getCuotasDto() == null) {
            return null;
        }

        CuentasPorCobrar cpc = cpcr.findByNumeroObligacion(dto.getNumeroObligacion());
        if (Objects.isNull(cpc)) {
            return null;
        }

        List<Gestiones> ges = cpc.getGestiones();

        AcuerdoPago acuPag = null;

        for (Gestiones ge : ges) {

            if (ge.getClasificacionGestion() instanceof AcuerdoPago) {
                AcuerdoPago acu = (AcuerdoPago) ge.getClasificacionGestion();
                if (acu.isIsActive()) {
                    acuPag = acu;
                }

            }

        }

        Usuario usu = usuClient.getUserByUsername(dto.getUsername());
        if (Objects.isNull(usu)) {
            return null;
        }

        for (int i = 0; i < dto.getCuotasDto().size(); i++) {

            CuotasDto cuotasDto = dto.getCuotasDto().get(i);

            if (Objects.nonNull(cuotasDto.getPagosDto()) && !cuotasDto.getPagosDto().isExisted()
                    && cuotasDto.getPagosDto().getIdPago() == 0) {
                if (Objects.nonNull(acuPag.getCuotasList().get(i).getPagos())
                        && acuPag.getCuotasList().get(i).getPagos().getSaldoCuota() > 0) {

                    Pagos pagoExisting = acuPag.getCuotasList().get(i).getPagos();
                    Pagos pago = new Pagos();
                    pago.setFechaPago(cuotasDto.getPagosDto().getFechaPago());
                    pago.setSaldoCuota(cuotasDto.getPagosDto().getSaldoCuota());
                    pago.setValorPago(pagoExisting.getValorPago() + cuotasDto.getPagosDto().getValorPago());
                    pago.setValorIntereses(pagoExisting.getValorIntereses() + cuotasDto.getPagosDto().getIntereses());
                    pago.setValorHonorarios(
                            pagoExisting.getValorHonorarios() + cuotasDto.getPagosDto().getHonorarios());
                    pago.setValorCapital(pagoExisting.getValorCapital() + cuotasDto.getPagosDto().getCapital());
                    pago.setUsuarioId(usu.getIdUsuario());
                    pago.setDetalle(dto.getDetalle());

                    pago = pagosRespositoty.save(pago);

                    acuPag.getCuotasList().get(i).setPagos(pago);
                    acuPag.getCuotasList().get(i).setCapitalCuota(cuotasDto.getCapitalCuota());
                    acuPag.getCuotasList().get(i).setSaldoCapitalCuota(cuotasDto.getSaldoCapital());
                    acuPag.getCuotasList().get(i).setHonorarios(cuotasDto.getHonorarios());
                    acuPag.getCuotasList().get(i).setSaldoHonorarios(cuotasDto.getSaldoHonorario());
                    acuPag.getCuotasList().get(i).setInteresCuota(cuotasDto.getInteresCuota());
                    acuPag.getCuotasList().get(i).setSalodInteresCuota(cuotasDto.getSaldoIntereses());
                    acuPag.getCuotasList().get(i).setCumplio(cuotasDto.isCumplio());

                } else {
                    Pagos pago = new Pagos();
                    pago.setFechaPago(cuotasDto.getPagosDto().getFechaPago());
                    pago.setSaldoCuota(cuotasDto.getPagosDto().getSaldoCuota());
                    pago.setValorPago(cuotasDto.getPagosDto().getValorPago());
                    pago.setValorIntereses(cuotasDto.getPagosDto().getIntereses());
                    pago.setValorHonorarios(cuotasDto.getPagosDto().getHonorarios());
                    pago.setValorCapital(cuotasDto.getPagosDto().getCapital());
                    pago.setUsuarioId(usu.getIdUsuario());
                    pago.setDetalle(dto.getDetalle());

                    pago = pagosRespositoty.save(pago);

                    acuPag.getCuotasList().get(i).setPagos(pago);
                    acuPag.getCuotasList().get(i).setCapitalCuota(cuotasDto.getCapitalCuota());
                    acuPag.getCuotasList().get(i).setSaldoCapitalCuota(cuotasDto.getSaldoCapital());
                    acuPag.getCuotasList().get(i).setHonorarios(cuotasDto.getHonorarios());
                    acuPag.getCuotasList().get(i).setSaldoHonorarios(cuotasDto.getSaldoHonorario());
                    acuPag.getCuotasList().get(i).setInteresCuota(cuotasDto.getInteresCuota());
                    acuPag.getCuotasList().get(i).setSalodInteresCuota(cuotasDto.getSaldoIntereses());
                    acuPag.getCuotasList().get(i).setCumplio(cuotasDto.isCumplio());

                }

            }

        }

        acuPag.setValorTotalCapital(
                acuPag.getValorTotalAcuerdo() - acuPag.getValorInteresesMora() - acuPag.getHonorarioAcuerdo());
        acuPag.setIsCumpliendo(dto.isCumpliendo());
        acuPag.setSaldoCapital(dto.getSaldoCapital());
        acuPag.setSaldoHonorarios(dto.getSaldoHonorarios());
        acuPag.setSaldoInteresesMora(dto.getSaldoInteresesMora());
        try {
            String base64 = generarPdf.generarReportePagoCuotas(dto);

            if (Objects.isNull(base64)) {
                return null;
            }

            MultipartFile multipartFile = saveFiles.convertirFile(base64);
            if (Objects.isNull(multipartFile)) {
                return null;
            }

            String fileName = multipartFile.getOriginalFilename();

            // En la columna ruta se guarda el fileId de Drive
            String ruta = driveStorageService.subir(multipartFile.getBytes(), fileName, "application/pdf",
                    saveFiles.carpetaRecibos(cpc.getSede().getSede()));

            ReciboPago recibo = new ReciboPago();
            recibo.setNombreArchivo(fileName);
            recibo.setNumeroRecibo(dto.getNumeroRecibo());
            recibo.setFechaRecibo(Functions.obtenerFechaYhora());
            recibo.setValorRecibo(dto.getValorTotal());
            recibo.setRuta(ruta);
            recibo.setUsuarioId(usu.getIdUsuario());

            recibo = reciboPagoRepository.save(recibo);

            for (int i = 0; i < dto.getCuotasDto().size(); i++) {

                CuotasDto cuotasDto = dto.getCuotasDto().get(i);

                if (Objects.nonNull(cuotasDto.getPagosDto())
                        && Objects.nonNull(acuPag.getCuotasList().get(i).getPagos())
                        && !cuotasDto.getPagosDto().isExisted() && cuotasDto.getPagosDto().getIdPago() == 0) {
                    acuPag.getCuotasList().get(i).getPagos().setReciboPago(recibo);
                }
            }

            Date fechaCompromisoActualizada = obtenerFechaCompromiso(acuPag.getCuotasList());

            if (fechaCompromisoActualizada != null) {
                acuPag.setFechaCompromiso(fechaCompromisoActualizada);
            }

            if (acuPag.getCuotasList().get(acuPag.getCuotasList().size() - 1).isCumplio()) {
                acuPag.setIsActive(false);
            }

            acuPag = apr.save(acuPag);

            if (Objects.nonNull(dto.getNombreClasificacion())) {
                AsesorCartera asesor = asesorCarteraRepository.findByUsuarioId(usu.getIdUsuario());
                if (Objects.isNull(usu)) {
                    return null;
                }

                Gestiones gestion = new Gestiones();
                gestion.setAsesorCartera(asesor);
                gestion.setNumeroObligacion(cpc.getNumeroObligacion());
                gestion.setFechaGestion(Functions.obtenerFechaYhora());
                gestion.setCuentasPorCobrar(cpc);

                Nota nota = new Nota();
                nota.setClasificacion("NOTA");
                nota.setDetalleNota("ACUERDO DE PAGO CON ID:  ".concat(acuPag.getIdClasificacionGestion().toString())
                        .concat(" ").concat(dto.getDetalle()));
                nota.setFechaNota(Functions.obtenerFechaYhora());
                nota.setAsesor(asesor);

                NombresClasificacion nombre = nombresClasificacionRepository
                        .findFirstByNombre(dto.getNombreClasificacion());
                if (Objects.isNull(nombre)) {
                    return null;
                }
                nota.setNombresClasificacion(nombre);
                nota = notaRepository.save(nota);

                gestion.setClasificacion(nota);

                gestion = gr.save(gestion);

            }

            Notificaciones notificacion = notificacionesRepository
                    .findByGestionIdAndFechaCreacion(acuPag.getIdClasificacionGestion(), acuPag.getFechaAcuerdo());

            if (Objects.nonNull(notificacion)) {

                if (acuPag.isIsActive()) {
                    notificacion.setFechaFinalizacion(fechaCompromisoActualizada);

                } else {
                    notificacion.setIsActive(false);

                }
                notificacion = notificacionesRepository.save(notificacion);

            }

            PagosCuotasResponse pgr = new PagosCuotasResponse();
            pgr.setBase64(base64);
            pgr.setNombreArchivo(cpc.getCliente().concat(".pdf"));
            pgr.setReciboPago(recibo);
            return pgr;
        } catch (IOException ex) {
            Logger.getLogger(PagosServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PagosServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(PagosServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;

    }

    @Override
    public ReciboPago findReciboById(Long idRecibo) {
        if (idRecibo == null) {
            return null;
        }
        return reciboPagoRepository.findById(idRecibo).orElse(null);
    }

    private Date obtenerFechaCompromiso(List<Cuotas> cuotas) {
        Integer position = 0;

        for (int i = 0; i < cuotas.size(); i++) {
            if (!cuotas.get(i).isCumplio()) {
                position = i;
                break;
            }
        }

        return cuotas.get(position).getFechaVencimiento();
    }
}
