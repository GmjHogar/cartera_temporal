package com.cartera_temp.cartera_temp.ServiceImpl;

import GestionesDataDto.GestionesDataDto;
import com.cartera_temp.cartera_temp.Components.GenerarPdf;
import com.cartera_temp.cartera_temp.Dtos.AcuerdoPagoDto;
import com.cartera_temp.cartera_temp.Dtos.AlertsGestiones;
import com.cartera_temp.cartera_temp.Dtos.AsesorCarteraResponse;
import com.cartera_temp.cartera_temp.Dtos.ClientesDto;
import com.cartera_temp.cartera_temp.Dtos.CuentasPorCobrarResponse;
import com.cartera_temp.cartera_temp.Dtos.CuotaDto;
import com.cartera_temp.cartera_temp.Dtos.CuotasDto;
import com.cartera_temp.cartera_temp.Dtos.GestionResponse;
import com.cartera_temp.cartera_temp.Dtos.GestionToSaveDto;
import com.cartera_temp.cartera_temp.Dtos.GestionesDto;
import com.cartera_temp.cartera_temp.Dtos.LinkDto;
import com.cartera_temp.cartera_temp.Dtos.LinkToClient;
import com.cartera_temp.cartera_temp.Dtos.Telefono;
import com.cartera_temp.cartera_temp.FeignClients.AuthClient;
import com.cartera_temp.cartera_temp.FeignClients.ClientesClient;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.AsesorCartera;

import com.cartera_temp.cartera_temp.Models.ClasificacionGestion;

import com.cartera_temp.cartera_temp.Models.CuentasPorCobrar;
import com.cartera_temp.cartera_temp.Models.Cuotas;
import com.cartera_temp.cartera_temp.Models.Gestiones;
import com.cartera_temp.cartera_temp.Models.NombresClasificacion;
import com.cartera_temp.cartera_temp.Models.HistoricoAcuerdosPago;
import com.cartera_temp.cartera_temp.Models.Nota;
import com.cartera_temp.cartera_temp.Models.Notificaciones;
import com.cartera_temp.cartera_temp.Models.Tarea;
import com.cartera_temp.cartera_temp.ModelsClients.Usuario;
import com.cartera_temp.cartera_temp.Service.AsesorCarteraService;
import com.cartera_temp.cartera_temp.Service.FileService;
import com.cartera_temp.cartera_temp.Service.GestionesService;
import com.cartera_temp.cartera_temp.Service.NotificacionesService;
import com.cartera_temp.cartera_temp.Service.UsuarioClientService;
import com.cartera_temp.cartera_temp.Utils.Functions;
import com.cartera_temp.cartera_temp.Utils.SaveFiles;
import com.cartera_temp.cartera_temp.repository.AcuerdoPagoRepository;
import com.cartera_temp.cartera_temp.repository.BancoRepository;
import com.cartera_temp.cartera_temp.repository.ClasificacionGestionRepository;

import com.cartera_temp.cartera_temp.repository.CuentasPorCobrarRepository;
import com.cartera_temp.cartera_temp.repository.CuotaRepository;
import com.cartera_temp.cartera_temp.repository.GestionesRepository;
import com.cartera_temp.cartera_temp.repository.NombresClasificacionRepository;
import com.cartera_temp.cartera_temp.repository.HistoricoAcuerdoPagoRepository;
import com.cartera_temp.cartera_temp.repository.NotaRepository;
import com.cartera_temp.cartera_temp.repository.NotificacionesRepository;
import com.cartera_temp.cartera_temp.repository.SedeRepository;
import com.cartera_temp.cartera_temp.repository.TareaRepository;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GestionesServiceImpl implements GestionesService {

    private final Long adminUserId = 1L;

    private final GestionesRepository gestionesRepository;
    private final CuentasPorCobrarRepository cuentaCobrarRepository;
    private final UsuarioClientService usuarioClientService;

    private final AsesorCarteraService asesorCartera;
    private final FileService fileService;
    private final SedeRepository sedeRepository;
    private final BancoRepository bancoRepository;
    private final SaveFiles saveFiles;
    private final NotificacionesService notificacionesService;
    private final AcuerdoPagoRepository acuerdoPagoRepository;
    private final ClasificacionGestionRepository clasificacionGestionRepository;
    private final NotaRepository notaRepository;
    private final TareaRepository tareaRepository;
    private final NombresClasificacionRepository nombresClasificacionRepository;
    private final CuotaRepository cuotaRepository;
    private final HistoricoAcuerdoPagoRepository historicoAcuerdoPagoRepository;
    private final ClientesClient clientesClient;
    private final HttpServletRequest request;
    private final GenerarPdf pdf;
    private final NotificacionesRepository notificacionesRepository;
    private final AuthClient authClient;
    private final ModelMapper modelMapper;
    private final HttpServletRequest httpServletRequest;



    public GestionesServiceImpl(GestionesRepository gestionesRepository,
            CuentasPorCobrarRepository cuentaCobrarRepository, UsuarioClientService usuarioClientService,
            AsesorCarteraService asesorCartera, FileService fileService, SedeRepository sedeRepository,
            BancoRepository bancoRepository, SaveFiles saveFiles, NotificacionesService notificacionesService,
            AcuerdoPagoRepository acuerdoPagoRepository, ClasificacionGestionRepository clasificacionGestionRepository,
            NotaRepository notaRepository, TareaRepository tareaRepository,
            NombresClasificacionRepository nombresClasificacionRepository, CuotaRepository cuotaRepository,
            HistoricoAcuerdoPagoRepository historicoAcuerdoPagoRepository, ClientesClient clientesClient,
            HttpServletRequest request, GenerarPdf pdf, NotificacionesRepository notificacionesRepository,
            AuthClient authClient,  ModelMapper modelMapper, HttpServletRequest httpServletRequest) {
        this.gestionesRepository = gestionesRepository;
        this.cuentaCobrarRepository = cuentaCobrarRepository;
        this.usuarioClientService = usuarioClientService;
        this.asesorCartera = asesorCartera;
        this.fileService = fileService;
        this.sedeRepository = sedeRepository;
        this.bancoRepository = bancoRepository;
        this.saveFiles = saveFiles;
        this.notificacionesService = notificacionesService;
        this.acuerdoPagoRepository = acuerdoPagoRepository;
        this.clasificacionGestionRepository = clasificacionGestionRepository;
        this.notaRepository = notaRepository;
        this.tareaRepository = tareaRepository;
        this.nombresClasificacionRepository = nombresClasificacionRepository;
        this.cuotaRepository = cuotaRepository;
        this.historicoAcuerdoPagoRepository = historicoAcuerdoPagoRepository;
        this.clientesClient = clientesClient;
        this.request = request;
        this.pdf = pdf;
        this.notificacionesRepository = notificacionesRepository;
        this.authClient = authClient;
        this.modelMapper = modelMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public GestionResponse saveOneGestion(GestionToSaveDto dto) {

        if (dto.getNumeroObligacion() == null || dto.getNumeroObligacion().equals("") || dto.getClasificacion() == null
                || dto.getClasificacion().equals("")) {
            return null;
        }

        CuentasPorCobrar cpc = cuentaCobrarRepository.findByNumeroObligacion(dto.getNumeroObligacion());
        if (Objects.isNull(cpc)) {
            return null;
        }

        Usuario usuDesignated = usuarioClientService.obtenerUsuario(dto.getUsernameToSetNotificacion());
        if (Objects.isNull(usuDesignated)) {
            return null;
        }

        Usuario userNotifying = usuarioClientService.obtenerUsuario(dto.getUserNotifying());
        if (Objects.isNull(userNotifying)) {
            return null;
        }

        AsesorCartera asesor = asesorCartera.findAsesor(userNotifying.getIdUsuario());
        if (Objects.isNull(asesor)) {
            return null;
        }

        Gestiones gestion = new Gestiones();

        gestion.setAsesorCartera(asesor);

        gestion.setNumeroObligacion(cpc.getNumeroObligacion());

        gestion.setCuentasPorCobrar(cpc);
        gestion.setDetallesAdicionales(dto.getDetallesAdicionales());

        try {
            gestion.setFechaGestion(Functions.obtenerFechaYhora());
        } catch (ParseException ex) {
            Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        NombresClasificacion clasificacion = nombresClasificacionRepository.findByNombreAndTipo(
                dto.getClasificacion().getNombreClasificacion(), dto.getClasificacion().getTipoClasificacion());
        if (Objects.isNull(clasificacion)) {
            return null;
        }

        // ACUERDO DE PAGO
        if (clasificacion.getTipo().equals("Acuerdo de Pago".toUpperCase())) {

            if (Objects.nonNull(dto.getClasificacionId())) {
                Tarea tarea = tareaRepository.findById(dto.getClasificacionId()).orElse(null);
                if (Objects.isNull(tarea)) {
                    return null;
                }
                tarea.setIsActive(false);
                tarea = tareaRepository.save(tarea);

                if (Objects.nonNull(dto.getNotificacionId())) {
                    Notificaciones notificacion = notificacionesService.getById(dto.getNotificacionId());

                    if (Objects.nonNull(notificacion)) {
                        notificacion.setIsActive(false);

                        notificacion = notificacionesService.crearNotificaciones(notificacion);

                    }
                }
            }

            AcuerdoPago acuerdoPago = new AcuerdoPago();

            acuerdoPago.setAsesor(asesor);
            acuerdoPago.setDetalle(dto.getClasificacion().getAcuerdoPago().getDetalle());
            try {
                acuerdoPago.setFechaAcuerdo(Functions.obtenerFechaYhora());
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                acuerdoPago.setFechaCompromiso(
                        Functions.stringToDateAndFormat(dto.getClasificacion().getAcuerdoPago().getFechaCompromiso()));
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            acuerdoPago.setTipoAcuerdo(dto.getClasificacion().getAcuerdoPago().getTipoAcuerdo());
            acuerdoPago.setClasificacion(dto.getClasificacion().getTipoClasificacion());
            acuerdoPago.setValorCuotaMensual(dto.getClasificacion().getAcuerdoPago().getValorCuotaMensual());
            acuerdoPago.setHonorarioAcuerdo(dto.getClasificacion().getAcuerdoPago().getHonoriarioAcuerdo());
            acuerdoPago.setSaldoHonorarios(dto.getClasificacion().getAcuerdoPago().getHonoriarioAcuerdo());
            acuerdoPago.setValorInteresesMora(dto.getClasificacion().getAcuerdoPago().getValorInteresesMora());
            acuerdoPago.setSaldoInteresesMora(dto.getClasificacion().getAcuerdoPago().getValorInteresesMora());
            acuerdoPago.setValorTotalAcuerdo(dto.getClasificacion().getAcuerdoPago().getValorTotalAcuerdo());
            acuerdoPago.setIsActive(true);

            for (CuotasDto cuotas : dto.getClasificacion().getAcuerdoPago().getCuotasList()) {
                Cuotas couta = new Cuotas();
                couta.setCapitalCuota(cuotas.getCapitalCuota());
                couta.setCumplio(false);
                try {

                    if (Objects.isNull(cuotas.getFechaVencimiento())) {
                        couta.setFechaVencimiento(cpc.getFechaVencimiento());
                    } else {
                        couta.setFechaVencimiento(Functions.stringToDate(cuotas.getFechaVencimiento()));
                    }

                } catch (ParseException ex) {
                    Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                couta.setHonorarios(cuotas.getHonorarios());
                couta.setInteresCuota(cuotas.getInteresCuota());
                couta.setNumeroCuota(cuotas.getNumeroCuota());
                couta.setValorCuota(cuotas.getValorCuota());
                couta.setSaldoCapitalCuota(cuotas.getCapitalCuota());
                couta.setSaldoHonorarios(cuotas.getHonorarios());
                couta.setSalodInteresCuota(cuotas.getInteresCuota());

                acuerdoPago.agregarCuota(couta);
            }

            NombresClasificacion nombre = nombresClasificacionRepository.findFirstByNombre(clasificacion.getNombre());
            if (Objects.isNull(nombre)) {
                return null;
            }

            acuerdoPago.setNombresClasificacion(nombre);

            Notificaciones notificacion = new Notificaciones();
            notificacion.setTipoGestion("ACUERDO DE PAGO");
            try {
                notificacion.setFechaCreacion(Functions.obtenerFechaYhora());
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            notificacion.setFechaFinalizacion(acuerdoPago.getFechaCompromiso());
            notificacion.setNumeroObligacion(cpc.getNumeroObligacion());
            notificacion.setDesignatedTo(usuDesignated.getIdUsuario());
            notificacion.setIsActive(true);
            notificacion.setDesignatedBy(userNotifying.getNombres().toUpperCase().concat(" ")
                    .concat(userNotifying.getApellidos().toUpperCase()));
            notificacion.setVerRealizadas("VER");
            notificacion.setCliente(cpc.getCliente());

            acuerdoPago = acuerdoPagoRepository.save(acuerdoPago);
            notificacion.setGestionId(acuerdoPago.getIdClasificacionGestion());
            notificacion = notificacionesService.crearNotificaciones(notificacion);

            gestion.setClasificacion(acuerdoPago);

        }

        // NOTA
        if (clasificacion.getTipo().equals("Nota".toUpperCase())) {

            if (Objects.nonNull(dto.getClasificacionId())) {
                Tarea tarea = tareaRepository.findById(dto.getClasificacionId()).orElse(null);
                if (Objects.isNull(tarea)) {
                    return null;
                }
                tarea.setIsActive(false);
                tarea = tareaRepository.save(tarea);

                if (Objects.nonNull(dto.getNotificacionId())) {
                    Notificaciones notificacion = notificacionesService.getById(dto.getNotificacionId());

                    if (Objects.nonNull(notificacion)) {
                        notificacion.setIsActive(false);

                        notificacion = notificacionesService.crearNotificaciones(notificacion);

                    }
                }
            }

            Nota nota = new Nota();

            nota.setAsesor(asesor);
            try {
                nota.setFechaNota(Functions.obtenerFechaYhora());
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            nota.setDetalleNota(dto.getClasificacion().getNota().getDetalle());
            nota.setClasificacion(dto.getClasificacion().getTipoClasificacion());

            NombresClasificacion nombre = nombresClasificacionRepository.findFirstByNombre(clasificacion.getNombre());
            if (Objects.isNull(nombre)) {
                return null;
            }

            nota.setNombresClasificacion(nombre);

            nota = notaRepository.save(nota);

            gestion.setClasificacion(nota);

        }

        // TAREA
        if (clasificacion.getTipo().equals("Tarea".toUpperCase())) {

            Tarea tarea = null;
            if (Objects.nonNull(dto.getClasificacionId())) {
                tarea = tareaRepository.findById(dto.getClasificacionId()).orElse(null);
                if (Objects.isNull(tarea)) {
                    return null;
                }
                tarea.setIsActive(false);
            }

            tarea = new Tarea();
            tarea.setAsesor(asesor);
            tarea.setIsParteOfRecaudo(dto.getClasificacion().getTarea().getIsPartOfRecaudo());
            tarea.setDetalleTarea(dto.getClasificacion().getTarea().getDetalleTarea());
            tarea.setIsActive(true);
            try {
                tarea.setFechaFinTarea(
                        Functions.stringToDateAndFormat(dto.getClasificacion().getTarea().getFechaFinTarea()));
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                tarea.setFechaTarea(Functions.obtenerFechaYhora());
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            tarea.setClasificacion(dto.getClasificacion().getTipoClasificacion());
            tarea.setDesignatedTo(usuDesignated.getIdUsuario());

            NombresClasificacion nombre = nombresClasificacionRepository.findFirstByNombre(clasificacion.getNombre());
            if (Objects.isNull(nombre)) {
                return null;
            }
            tarea.setNombresClasificacion(nombre);

            // guardar en tabla notificaciones
            if (Objects.nonNull(dto.getNotificacionId())) {
                Notificaciones notificacion = notificacionesService.getById(dto.getNotificacionId());

                if (Objects.nonNull(notificacion)) {
                    notificacion.setIsActive(false);

                    notificacion = notificacionesService.crearNotificaciones(notificacion);

                }
            }

            Notificaciones notificacion = new Notificaciones();
            notificacion.setTipoGestion("TAREA");
            notificacion.setFechaCreacion(tarea.getFechaTarea());
            notificacion.setFechaFinalizacion(tarea.getFechaFinTarea());
            notificacion.setNumeroObligacion(cpc.getNumeroObligacion());
            notificacion.setDesignatedTo(tarea.getDesignatedTo());
            notificacion.setDesignatedBy(
                    userNotifying.getNombres().toUpperCase().concat(userNotifying.getApellidos().toUpperCase()));
            notificacion.setVerRealizadas("VER");
            notificacion.setIsActive(true);
            notificacion.setCliente(cpc.getCliente());
            tarea = tareaRepository.save(tarea);

            notificacion.setGestionId(tarea.getIdClasificacionGestion());
            gestion.setClasificacion(tarea);

            notificacion = notificacionesService.crearNotificaciones(notificacion);
        }

        gestion = gestionesRepository.save(gestion);

        ModelMapper map = new ModelMapper();
        GestionResponse gesRes = map.map(gestion, GestionResponse.class);

        Usuario usu = null;
        if (!cpc.getAsesor().getUsuarioId().equals(adminUserId)) {
            usu = usuarioClientService.obtenerUsuarioById(cpc.getAsesor().getUsuarioId());
            if (Objects.isNull(usu)) {
                return null;
            }
            gesRes.setAsesorCartera(usu.getNombres() + usu.getApellidos());
        } else {
            String token = request.getAttribute("token").toString();
            String username = authClient.extractUsername(token.split(" ")[1]);
            if (Objects.isNull(username)) {
                return null;
            }

            Usuario user = usuarioClientService.obtenerUsuario(username);
            if (Objects.isNull(user)) {
                return null;
            }

            AsesorCartera aseserNuevo = asesorCartera.findAsesor(user.getIdUsuario());
            if (Objects.isNull(aseserNuevo)) {
                return null;
            }

            cpc.setAsesor(aseserNuevo);
            cpc.setIsBlocked(false);
            cpc = cuentaCobrarRepository.save(cpc);
            gesRes.setAsesorCartera(user.getNombres() + user.getApellidos());
        }

        return gesRes;

    }

    @Override
    public List<Gestiones> saveMultipleGestiones(GestionesDataDto dataDto) {

        MultipartFile multipartFile = saveFiles.convertirFile(dataDto.getMultipartFile());

        List<GestionesDto> gestiones = fileService.readFileGestiones(multipartFile, dataDto.getDelimitante());
        List<Gestiones> gestionesSaved = guardarGestiones(gestiones);

        return gestionesSaved;
    }

    @Override
    public List<GestionResponse> findHistoricoGestiones(String numeroObligacion) {

        if ("".equals(numeroObligacion) || numeroObligacion == null) {
            return null;
        }

        List<Gestiones> gestion = gestionesRepository.findByNumeroObligacionOrderByFechaGestionDesc(numeroObligacion);
        List<GestionResponse> gesResList = new ArrayList<>();
        if (Objects.isNull(gestion)) {
            return gesResList;
        }

        for (Gestiones gestiones : gestion) {

            if (gestiones.getClasificacionGestion() instanceof AcuerdoPago) {
                AcuerdoPago acuerdo = (AcuerdoPago) gestiones.getClasificacionGestion();
                for (Cuotas cuotas : acuerdo.getCuotasList()) {
                    if (Objects.nonNull(cuotas.getPagos())) {
                        try {
                            String base64 = saveFiles.pdfToBase64(cuotas.getPagos().getReciboPago().getRuta());
                            cuotas.getPagos().getReciboPago().setRuta(base64);
                        } catch (IOException ex) {
                            Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                        }
                    }
                }
            }

            ModelMapper map = new ModelMapper();
            GestionResponse gesRes = map.map(gestiones, GestionResponse.class);

            Usuario usu = usuarioClientService.obtenerUsuarioById(gestiones.getAsesorCartera().getUsuarioId());
            if (Objects.isNull(usu)) {
                return gesResList;
            }

            gesRes.setAsesorCartera(usu.getNombres().concat(" ".concat(usu.getApellidos())));

            gesResList.add(gesRes);
        }
        return gesResList;

    }

    @Override
    public List<Gestiones> guardarGestiones(List<GestionesDto> gestiones) {

        List<Gestiones> gestionesSaved = new ArrayList<>();

        for (GestionesDto gestione : gestiones) {
            Gestiones newGestion = new Gestiones();

            CuentasPorCobrar cuenta = cuentaCobrarRepository.findByNumeroObligacion(gestione.getNumeroObligacion());
            if (Objects.isNull(cuenta)) {
                continue;

            }
            newGestion.setAsesorCartera(cuenta.getAsesor());
            newGestion.setNumeroObligacion(gestione.getNumeroObligacion());
            newGestion.setFechaGestion(gestione.getFechaGestion());

            Nota nota = new Nota();
            nota.setDetalleNota(gestione.getDetallesAdicionales());
            try {
                nota.setFechaNota(Functions.obtenerFechaYhora());
            } catch (ParseException ex) {
                Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            nota.setAsesor(cuenta.getAsesor());

            ClasificacionGestion clasificacion = clasificacionGestionRepository.save(nota);

            newGestion.setClasificacion(clasificacion);

            cuenta.agregarGestion(newGestion);
            gestionesSaved.add(newGestion);

        }

        return gestionesRepository.saveAll(gestionesSaved);
    }

    @Override
    public String sendLastDatoAdicional(String numeroObligacion) {

        if ("".equals(numeroObligacion) || numeroObligacion == null) {
            return null;
        }

        Gestiones ultimaGestion = gestionesRepository
                .findTopByNumeroObligacionOrderByFechaGestionDesc(numeroObligacion);
        if (Objects.isNull(ultimaGestion)) {
            return null;
        }

        String datoAdicionalUltimaGestion = ultimaGestion.getDetallesAdicionales();
        return datoAdicionalUltimaGestion;

    }

    @Override
    @Transactional
    public void desactivateAcuerdoPago(Long idAcuerdoPago) {

        Gestiones gestion = gestionesRepository.findById(idAcuerdoPago).orElse(null);
        if (Objects.isNull(gestion)) {

            return;
        }

        AcuerdoPago acuerdo = new AcuerdoPago();

        List<Cuotas> cuotas = null;
        if (gestion.getClasificacionGestion() instanceof AcuerdoPago) {
            acuerdo = (AcuerdoPago) gestion.getClasificacionGestion();
            cuotas = new ArrayList<>(acuerdo.getCuotasList());

        } else {
            return;
        }

        HistoricoAcuerdosPago hap = new HistoricoAcuerdosPago();

        Usuario usuario = usuarioClientService.obtenerUsuarioById(acuerdo.getAsesor().getUsuarioId());
        if (Objects.isNull(usuario)) {
            return;
        }

        CuentasPorCobrar cpc = cuentaCobrarRepository
                .findByNumeroObligacion(acuerdo.getGestiones().getNumeroObligacion());

        String mora_total = acuerdo.getTipoAcuerdo();
        String valorAcuerdo = Double.toString(acuerdo.getValorTotalAcuerdo());
        String intMora = Double.toString(acuerdo.getValorInteresesMora());
        String valorCuotaMes = Double.toString(acuerdo.getValorCuotaMensual());
        String fechaCorte = acuerdo.getFechaAcuerdo().toString();
        double valorTotalCuotasPagadas = 0;
        int totalCuotasPagadas = 0;
        String cuotasTosave = "Cliente: ".concat(cpc.getCliente().concat("\n Numero de Obligacion: ")
                .concat(cpc.getNumeroObligacion())
                .concat("\n Asesor Cartera: ").concat(usuario.getNombres()).concat(usuario.getApellidos())
                .concat("\n Acuerdo pactuado por mora o total: ").concat(mora_total)
                .concat("\n Valor del acuerdo de pago: $").concat(String.valueOf(valorAcuerdo))
                .concat("\n Intereses por mora: $").concat(String.valueOf(intMora))
                .concat("\n Valor de la cuota mensual: $").concat(String.valueOf(valorCuotaMes))
                .concat("\n Fecha de corte del acuerdo de pago: ").concat(String.valueOf(fechaCorte)).concat("\n"));

        acuerdo.setIsActive(false);

        for (Cuotas cuota : cuotas) {
            cuotasTosave = cuotasTosave.concat(
                    Integer.toString(cuota.getNumeroCuota()).concat(" ").concat(Double.toString(cuota.getValorCuota()))
                            .concat(" ").concat(Double.toString(cuota.getCapitalCuota())).concat(" ")
                            .concat(cuota.getFechaVencimiento().toString()).concat(" ")
                            .concat(Double.toString(cuota.getHonorarios())))
                    .concat("\n");

            if (cuota.isCumplio()) {
                valorTotalCuotasPagadas = valorTotalCuotasPagadas + cuota.getValorCuota();
                totalCuotasPagadas += 1;
            }

        }

        hap.setAsesorCartera(usuario.getNombres().concat(usuario.getApellidos()));
        hap.setFechaCreacionAcuerdo(acuerdo.getFechaAcuerdo());
        hap.setHistorico(cuotasTosave);
        hap.setNumeroObligacion(cpc.getNumeroObligacion());
        hap.setTotalValorCuotasPagadas(valorTotalCuotasPagadas);
        hap.setTotalCuotasPagadas(totalCuotasPagadas);
        hap.setTotalCuotasAcuerdo(cuotas.size());
        hap.setValorTotalAcuerdo(acuerdo.getValorTotalAcuerdo());

        hap = historicoAcuerdoPagoRepository.save(hap);

        if (Objects.nonNull(acuerdo)) {
            acuerdo.getCuotasList().clear();
            acuerdoPagoRepository.save(acuerdo);
        }

        List<Notificaciones> notificaciones = notificacionesRepository
                .findByNumeroObligacionAndFechaCreacionGreaterThanEqualAndTipoGestionOrderByFechaCreacionDesc(
                        gestion.getNumeroObligacion(), gestion.getFechaGestion(), acuerdo.getClasificacion());

        if (!CollectionUtils.isEmpty(notificaciones)) {
            for (Notificaciones notificacione : notificaciones) {
                notificacione.setIsActive(false);
                notificacione.setVerRealizadas("HIDE");
                notificacione = notificacionesRepository.save(notificacione);
            }
        }
    }

    @Override
    public LinkToClient sendLinkAndPdfToClient(LinkDto dto) {

        if (dto.getNumeroObligacion() == "" || dto.getNumeroObligacion() == null || dto.getCedula() == ""
                || dto.getCedula() == null) {

            return null;
        }

        CuentasPorCobrar cpc = cuentaCobrarRepository.findByNumeroObligacion(dto.getNumeroObligacion());
        if (Objects.isNull(cpc)) {

            return null;
        }

        String moraTotal = "";

        for (Gestiones gestione : cpc.getGestiones()) {
            if (gestione.getClasificacionGestion() instanceof AcuerdoPago) {
                AcuerdoPago acuerdo = (AcuerdoPago) gestione.getClasificacionGestion();
                if (acuerdo.isIsActive()) {
                    moraTotal = acuerdo.getTipoAcuerdo();
                    break;
                }

            }

        }

        String token = request.getAttribute("token").toString();

        List<ClientesDto> client = new ArrayList<>();
        if (Objects.isNull(dto.getCedulaArchivo())) {
            client = clientesClient.buscarClientesByNumeroObligacion(dto.getCedula(), token);
            if (client.isEmpty()) {

                return null;
            }
        } else {
            client.add(clientesClient.buscarClientesByNumDoc(dto.getCedulaArchivo(), token));
            if (client.isEmpty()) {

                return null;
            }
        }

        Usuario usu = usuarioClientService.obtenerUsuarioById(cpc.getAsesor().getUsuarioId());
        if (Objects.isNull(usu)) {

            return null;
        }

        ClientesDto clientToSend = client.get(0);
        if (Objects.isNull(clientToSend)) {
            return null;
        }

        List<Telefono> telefono = clientToSend.getTelefonos().stream().filter(t -> t.isIsCurrent() == true)
                .collect(Collectors.toList());

        String telToMessage;
        if (Objects.nonNull(dto.getNumeroAlterno())) {
            telToMessage = "57 ".concat(dto.getNumeroAlterno());
        } else {
            telToMessage = telefono.get(0).getIndicativo().concat(" ").concat(telefono.get(0).getNumero());
        }

        LinkToClient link = new LinkToClient();

        String nombreTitular = clientToSend.getNombreTitular().replaceAll(" ", "%20").toUpperCase();
        String asesorCartera = usu.getNombres().replaceAll(" ", "%20").concat("%20")
                .concat(usu.getApellidos().replaceAll(" ", "%20")).toUpperCase();

        String message = "&text=Buen%20día%20señor/a%20".concat(nombreTitular)
                .concat(",%20se%20comunica%20con%20GMJ%20hogar;%20por%20medio%20de%20este%20mensaje%20le%20notificamos")
                .concat("%20que%20su%20acuerdo%20de%20pago%20").concat("por%20el/la%20").concat(moraTotal)
                .concat("%20ha%20sido%20efectuado%20exitosamente,%20a%20continuación%20enviaremos%20un%20PDF%20con%20la%20")
                .concat("información%20de%20su%20acuerdo%20de%20pago,%20este%20contiene%20las%20fechas%20de%20pago%20y%20los%20valores%20de%20las%20cuotas%20")
                .concat("mensuales%20acordadas%20con%20nuestro%20asesor/a%20de%20cartera%20".concat(asesorCartera)
                        .concat(",%20si%20tiene%20alguna%20duda%20por%20favor%20ponerse%20"))
                .concat("en%20contacto%20por%20este%20mismo%20medio,%20muchas%20gracias");

        link.setMessageToWpp("https://api.whatsapp.com/send?phone=".concat("+").concat(telToMessage).concat(message));
        try {
            link.setBase64(pdf.generarReporteAcuerdoPagoToClient(cpc, client.get(0), dto.getUsername()));
        } catch (IOException ex) {
            System.out.println(ex);
            Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
            Logger.getLogger(GestionesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return link;

    }

    @Override
    public List<Cuotas> cuotaCumplio(List<Long> idCuota) {
        List<Cuotas> cuota = cuotaRepository.findAllById(idCuota);
        if (CollectionUtils.isEmpty(cuota)) {
            return null;
        }
        for (Cuotas cuotas : cuota) {
            cuotas.setCumplio(true);
        }
        return cuotaRepository.saveAll(cuota);
    }

    @Override
    public AlertsGestiones alertasDeGestiones(String username, String fecha) {

        Date fechaInicialMes = Functions.obtenerFechaInicialFinalMes(true, "MES");
        Date fechaFinalMes = Functions.obtenerFechaInicialFinalMes(false, "MES");

        Date fechaInicialDia = Functions.obtenerFechaInicialFinalMes(true, "DIA");
        Date fechaFinalDia = Functions.obtenerFechaInicialFinalMes(false, "DIA");

        Usuario usuario = usuarioClientService.obtenerUsuario(username);
        if (Objects.isNull(usuario)) {
            return null;
        }

        AsesorCartera asesor = asesorCartera.findAsesor(usuario.getIdUsuario());
        if (Objects.isNull(asesor)) {
            return null;
        }

        AlertsGestiones alerts = new AlertsGestiones();

        List<Gestiones> gestionesByAsesor = gestionesRepository.gestionesByAsesor(fechaInicialMes, fechaFinalMes,
                asesor.getIdAsesorCartera());
        alerts.setGestionesRealizadas(gestionesByAsesor.size());
        alerts.setAcuerdosDePagosRealizados(gestionesRepository
                .acuerdosPagoRealizados(asesor.getIdAsesorCartera(), "ACUERDO DE PAGO", fechaInicialMes, fechaFinalMes)
                .size());
        alerts.setAcuerdosDePagosActivos(gestionesRepository
                .acuerdoPagoActivos(asesor.getIdAsesorCartera(), "ACUERDO DE PAGO", fechaInicialMes, fechaFinalMes)
                .size());
        alerts.setGestionesDia(gestionesRepository
                .gestionesDiaByAsesor(fechaInicialDia, fechaFinalDia, asesor.getIdAsesorCartera()).size());
        alerts.setAcuerdoPagoDia(gestionesRepository
                .acuerdosPagoRealizados(asesor.getIdAsesorCartera(), "ACUERDO DE PAGO", fechaInicialDia, fechaFinalDia)
                .size());
        alerts.setCuentasAsignadas(
                cuentaCobrarRepository.gestionesAsignadasByAsesorCount(asesor.getIdAsesorCartera()).size());
        alerts.setCuentasSinGestion(
                cuentaCobrarRepository.gestionesSinGestion(asesor.getIdAsesorCartera(), fechaInicialMes).size());
        alerts.setCuentasTotales(
                cuentaCobrarRepository.gestionesAsignadasByAsesorCountTotal(asesor.getIdAsesorCartera()).size());
        alerts.setAcuerdosPagoVencidos(cuentaCobrarRepository.obtenerAcuerdosPagoActivosVencidosCount(asesor.getIdAsesorCartera()));

        return alerts;
    }

    @Override
    public boolean desactivarGestiones(Long idGestion) {
        Gestiones gestion = gestionesRepository.findById(idGestion).orElse(null);
        if (Objects.isNull(gestion)) {
            return false;
        }

        if (gestion.getClasificacionGestion() instanceof Tarea) {
            Tarea tarea = (Tarea) gestion.getClasificacionGestion();

            tarea.setIsActive(false);

            Notificaciones notificacion = notificacionesRepository.findByGestionIdAndFechaCreacion(
                    gestion.getClasificacionGestion().getIdClasificacionGestion(), tarea.getFechaTarea());
            if (Objects.isNull(notificacion)) {
                return false;
            }

            notificacion.setIsActive(false);
            notificacion.setVerRealizadas("HIDE");

            tarea = tareaRepository.save(tarea);
            notificacion = notificacionesRepository.save(notificacion);

            return true;

        }

        return false;

    }

    @Override
    public ResponseEntity<Object> obtenerCuentasSinGestion(String username,Pageable pageable) {

        String token = httpServletRequest.getAttribute("token").toString();

        Usuario usuario = usuarioClientService.obtenerUsuario(username);
        if (Objects.isNull(usuario)) {
            return null;
        }

        AsesorCartera asesor = asesorCartera.findAsesor(usuario.getIdUsuario());
        if (Objects.isNull(asesor)) {
            return null;
        }
        Date fechaInicialMes = Functions.obtenerFechaInicialFinalMes(true, "MES");

        Page<CuentasPorCobrar> cuentas = cuentaCobrarRepository.gestionesSinGestionPage(asesor.getIdAsesorCartera(), fechaInicialMes, pageable);


        List<CuentasPorCobrarResponse> cuentasResponse = new ArrayList<>();

        for (CuentasPorCobrar cuentasPorCobrar : cuentas.getContent()) {
            // calcular nuevos dias vencidos
            int diasVecidos = Functions.diferenciaFechas(cuentasPorCobrar.getFechaVencimiento());
            CuentasPorCobrarResponse c = modelMapper.map(cuentasPorCobrar, CuentasPorCobrarResponse.class);
            if (diasVecidos <= 0) {
                c.setDiasVencidos(0);
            } else {
                c.setDiasVencidos(diasVecidos);
            }

            c.setTiposVencimiento(cuentasPorCobrar.getTiposVencimiento());
            AsesorCarteraResponse asesorResponse = new AsesorCarteraResponse();
            asesorResponse.setIdAsesorCartera(cuentasPorCobrar.getAsesor().getIdAsesorCartera());
            asesorResponse.setUsuario(usuario);
            c.setAsesorCarteraResponse(asesorResponse);

            c.setGestion(cuentasPorCobrar.getGestiones());

            c.setGestion(cuentasPorCobrar.getGestiones());

            String obligacion = cuentasPorCobrar.getDocumentoCliente().concat(cuentasPorCobrar.getSede().getSede())
                    .concat(cuentasPorCobrar.getBanco().getBanco());

            List<ClientesDto> clientes = clientesClient.buscarClientesByNumeroObligacion(obligacion, token);
            c.setClientes(clientes);

            if (cuentasPorCobrar.getGestiones().size() > 0) {
                for (Gestiones gestione : cuentasPorCobrar.getGestiones()) {
                    if (gestione.getClasificacionGestion() instanceof AcuerdoPago) {
                        AcuerdoPago acuPago = (AcuerdoPago) gestione.getClasificacionGestion();
                        for (Cuotas cuotas : acuPago.getCuotasList()) {
                            if (Objects.nonNull(cuotas.getPagos())) {
                                String base = null;
                                try {
                                    base = saveFiles.pdfToBase64(cuotas.getPagos().getReciboPago().getRuta());
                                } catch (IOException ex) {
                                    Logger.getLogger(CuentaPorCobrarServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                                    continue;
                                }
                                System.out.println(cuotas.getIdCuota());
                                cuotas.getPagos().getReciboPago().setRuta(base);
                            }
                        }

                    }
                }
            }

            cuentasResponse.add(c);
            
        }


        Page<CuentasPorCobrarResponse> cuentasPage = new PageImpl(cuentasResponse, pageable,cuentas.getTotalElements());
                return ResponseEntity.status(HttpStatus.OK).body(cuentasPage);

    }

    @Override
    public ResponseEntity<Object> obtenerAcuerdosPagoActivosVencidos(String username, Pageable pageable) {
        String token = httpServletRequest.getAttribute("token").toString();

        Usuario usuario = usuarioClientService.obtenerUsuario(username);
        if (Objects.isNull(usuario)) {
            return null;
        }

        AsesorCartera asesor = asesorCartera.findAsesor(usuario.getIdUsuario());
        if (Objects.isNull(asesor)) {
            return null;
        }


        Page<CuentasPorCobrar> cuentas = cuentaCobrarRepository.obtenerAcuerdosPagoActivosVencidos(asesor.getIdAsesorCartera(), pageable);
        List<CuentasPorCobrarResponse> cuentasResponse = new ArrayList<>();

        for (CuentasPorCobrar cuentasPorCobrar : cuentas.getContent()) {
            // calcular nuevos dias vencidos
            int diasVecidos = Functions.diferenciaFechas(cuentasPorCobrar.getFechaVencimiento());
            CuentasPorCobrarResponse c = modelMapper.map(cuentasPorCobrar, CuentasPorCobrarResponse.class);
            if (diasVecidos <= 0) {
                c.setDiasVencidos(0);
            } else {
                c.setDiasVencidos(diasVecidos);
            }

            c.setTiposVencimiento(cuentasPorCobrar.getTiposVencimiento());
            AsesorCarteraResponse asesorResponse = new AsesorCarteraResponse();
            asesorResponse.setIdAsesorCartera(cuentasPorCobrar.getAsesor().getIdAsesorCartera());
            asesorResponse.setUsuario(usuario);
            c.setAsesorCarteraResponse(asesorResponse);

            c.setGestion(cuentasPorCobrar.getGestiones());

            c.setGestion(cuentasPorCobrar.getGestiones());

            String obligacion = cuentasPorCobrar.getDocumentoCliente().concat(cuentasPorCobrar.getSede().getSede())
                    .concat(cuentasPorCobrar.getBanco().getBanco());

            List<ClientesDto> clientes = clientesClient.buscarClientesByNumeroObligacion(obligacion, token);
            c.setClientes(clientes);

            

            cuentasResponse.add(c);
            
        }


        Page<CuentasPorCobrarResponse> cuentasPage = new PageImpl(cuentasResponse, pageable,
                cuentas.getTotalElements());
                return ResponseEntity.status(HttpStatus.OK).body(cuentasPage);
    }



    
}
