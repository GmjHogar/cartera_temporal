package com.cartera_temp.cartera_temp.Components.Impl;

import com.cartera_temp.cartera_temp.Components.GenerarPdf;
import com.cartera_temp.cartera_temp.Dtos.ClientesDto;
import com.cartera_temp.cartera_temp.Dtos.PagosCuotasDto;
import com.cartera_temp.cartera_temp.Models.AcuerdoPago;
import com.cartera_temp.cartera_temp.Models.CuentasPorCobrar;
import com.cartera_temp.cartera_temp.Models.Firmas;
import com.cartera_temp.cartera_temp.Models.Gestiones;
import com.cartera_temp.cartera_temp.ModelsClients.Usuario;
import com.cartera_temp.cartera_temp.Service.DriveStorageService;
import com.cartera_temp.cartera_temp.Service.FirmasService;
import com.cartera_temp.cartera_temp.Service.UsuarioClientService;
import com.cartera_temp.cartera_temp.Utils.Functions;

import com.cartera_temp.cartera_temp.repository.CuentasPorCobrarRepository;
import com.tenpisoft.n2w.MoneyConverters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class GenerarPdfImpl implements GenerarPdf {

    private final CuentasPorCobrarRepository cpcR;
    private final UsuarioClientService usuClient;
    private final FirmasService firmasService;
    private final DriveStorageService driveStorageService;

    public GenerarPdfImpl(CuentasPorCobrarRepository cpcR, UsuarioClientService usuClient, FirmasService firmasService, DriveStorageService driveStorageService) {
        this.cpcR = cpcR;
        this.usuClient = usuClient;
        this.firmasService = firmasService;
        this.driveStorageService = driveStorageService;
    }

    public static boolean palabraResaltada(String palabra, String[] palabrasResaltadas) {

        for (String palabrasResaltada : palabrasResaltadas) {

            if (palabra.equalsIgnoreCase(palabrasResaltada)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String generarReporteAcuerdoPagoToClient(CuentasPorCobrar cpc, ClientesDto cliente,String username) throws IOException, ClassNotFoundException {

        if (Objects.isNull(cpc)) {
            
            return null;
        }

        
        if (username == null || username == "") {
            return null;
        }

        List<Gestiones> gestion = cpc.getGestiones();

        // Mismo criterio que sendLinkAndPdfToClient: el texto de "clasificacion" lo envia el frontend y puede variar
        List<Gestiones> gestionList = gestion.stream().filter(g -> g.getClasificacion() instanceof AcuerdoPago && ((AcuerdoPago) g.getClasificacion()).isIsActive()).collect(Collectors.toList());
        if (gestionList.isEmpty()) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.WARNING, "La cuenta {0} no tiene acuerdo de pago activo", cpc.getNumeroObligacion());
            return null;
        }

        String titulo = "REPORTE ACUERDO DE PAGO";
        String fecha = "";
        try {
            fecha = Functions.dateToString(gestionList.get(0).getFechaGestion());
        } catch (ParseException ex) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        String sede = cpc.getSede().getNombreComercialSede();
        
        String nombreClienteSplit = cliente.getNombreTitular();
        String docCliente = cliente.getNumeroDocumento();
        String pagare = cpc.getPagare();
        Usuario usu = usuClient.obtenerUsuario(username);
        if (Objects.isNull(usu)) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.WARNING, "PDF acuerdo: no se encontro el usuario {0}", username);
            return null;
        }
        Firmas firma = firmasService.findFirmaByUsername(username);
        if (Objects.isNull(firma)) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.WARNING, "PDF acuerdo: el usuario {0} no tiene firma registrada", username);
            return null;
        }

        try {
            try (PDDocument doc = new PDDocument()) {
                PDPage letras = new PDPage();
                doc.addPage(letras);
                ClassPathResource resource = new ClassPathResource("electrohogarOpa.png");
                ClassPathResource resourceFY = new ClassPathResource("FIRMA_YEIMAR.png");

                InputStream inputStream = resource.getInputStream();
                InputStream inputStreamFY = resourceFY.getInputStream();

                PDImageXObject logoImage = PDImageXObject.createFromByteArray(doc, IOUtils.toByteArray(inputStream), "electrohogarOpa.png");
                PDImageXObject firmaYeimar = PDImageXObject.createFromByteArray(doc, IOUtils.toByteArray(inputStreamFY), "FIRMA_YEIMAR.png");
                // firma.getRuta() es un fileId de Drive (o una ruta antigua /uploads/...); el tipo de imagen se detecta por el contenido
                PDImageXObject firmaAsesorCartera = PDImageXObject.createFromByteArray(doc,
                        driveStorageService.descargar(firma.getRuta()), firma.getFilename());

                try (PDPageContentStream contens = new PDPageContentStream(doc, letras)) {
                    contens.drawImage(logoImage, 612 / 2 - 150, 680, 300, 100);
                    contens.drawImage(firmaYeimar, 80, 100, 200, 100);
                    contens.drawImage(firmaAsesorCartera, 320, 100, 200, 100);
                    nuevaLinea("Yeimar Fernando Sanchez Gomez", 83, 93, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea("Jefe de Cartera GMJ Hogar S.A.S", 83, 82, contens, PDType1Font.HELVETICA_BOLD, 12);

                    nuevaLinea(usu.getNombres().concat(" ").concat(usu.getApellidos()), 323, 93, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea("Analista de Cartera GMJHogar S.A.S", 323, 82, contens, PDType1Font.HELVETICA_BOLD, 12);

                    String ciudadHeader = "Medellín, ";

                    String fechaFormatHeader = "";

                    try {
                        fechaFormatHeader = Functions.formatearFecha(Functions.obtenerFechaYhora());
                    } catch (ParseException ex) {
                        System.out.println(ex);
                    }

                    String fechaConvenio = Functions.obtenerTextoFechaConvenio();

                    String tituloLetras = "Acuerdo de pago".toUpperCase();
                    String primeroLetras = "PRIMERO:".toUpperCase();
                    String segundoLetras = "SEGUNDO:".toUpperCase();
                    String terceraLetras = "TERCERA:".toUpperCase();
                    String gmj = "GMJ HOGAR SAS".toUpperCase();
                    String nit = "NIT 901056810-9";

                    Gestiones gesLetras = gestionList.get(0);
                    AcuerdoPago acuPagoLetras = new AcuerdoPago();

                    if (gesLetras.getClasificacion() instanceof AcuerdoPago) {
                        acuPagoLetras = (AcuerdoPago) gesLetras.getClasificacion();
                        
                        tituloLetras = tituloLetras.concat(" POR ").concat(acuPagoLetras.getTipoAcuerdo());
                    }

//                    tituloLetras = tituloLetras.concat(" POR ").concat(acuPagoLetras.getTipoAcuerdo());
                    nuevaLinea(ciudadHeader.concat(fechaFormatHeader), 72, 660, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea(tituloLetras, 200, 640, contens, PDType1Font.HELVETICA_BOLD, 12);

                    String valorAcuerdoLetras = formatNumber((int) acuPagoLetras.getValorTotalAcuerdo());
                    String valorCuotaAcuerdo = formatNumber((int) acuPagoLetras.getCuotasList().get(0).getValorCuota());
                    String inquietud = "Cualquier inquietud puede comunicarse al 5205330 ext. 1009.";

                    String mensajeLetras1 = "El deudor ".concat(nombreClienteSplit).concat(" con número de identificación ").concat(docCliente.concat(", a través de este documento me comprometo a cumplir el siguiente compromiso:"));

                    String mensajePrimero1 = primeroLetras.concat(" El deudor acepta y se compromete a pagar la deuda contraída en el almacén ").concat((sede).concat(" establecimiento comercial de ").concat(gmj).concat(" con ").concat(nit)).concat(", la cual asciende a la cantidad de $".concat(valorAcuerdoLetras).concat(" a la fecha en la que se genera el presente certificado."));

                    String mensajeSegundo1 = segundoLetras.concat(" De mutuo acuerdo se establece el siguiente plan de pagos donde el deudor se compromete a realizar pagos mensuales por el valor de $".concat(valorCuotaAcuerdo));

                    String mensajeTercera1 = terceraLetras.concat(" Si el deudor incumple en algún pago o no cancela en su totalidad la deuda contraída según el plazo estipulado, el acreedor puede iniciar inmediatamente las acciones legales que mejor considere pertinentes para cobrar el monto establecido sumando valor de cobranza jurídica y los intereses correspondientes a la fecha en que se incumpla este acuerdo");
                    
                    String[] negritaFecha = Functions.fechaDateToStringSinHora().split("-");

                    //String[] lineas = {mensajeLetras1, mensajePrimero1, mensajeSegundo1, mensajeTercera1, fechaConvenio, inquietud};
                    String variableConcat = nombreClienteSplit.concat(" ").concat(docCliente).concat(" ").concat(sede).concat(" ").concat(gmj)
                            .concat(" ").concat(" ").concat(valorAcuerdoLetras).concat(" ").concat(nit).concat(" ").concat(primeroLetras)
                            .concat(" ").concat(segundoLetras).concat(" ").concat(terceraLetras).concat(" ").concat(inquietud);

                    String[] variablesConcatSplit = variableConcat.split(" ");

                    String[] negritaNombreClienteSplit = nombreClienteSplit.split(" ");

                    float margin = 72;
                    float yStart = letras.getMediaBox().getHeight() - margin - 120;
                    float width = letras.getMediaBox().getWidth() - (margin * 2);
                    float yPosition = yStart;
                    float spaceWidth = 2; // Espaciado entre palabras

                    String delimitante = ";";

                    String mensajesConcat = mensajeLetras1.concat(delimitante).concat(mensajePrimero1).concat(delimitante).concat(mensajeSegundo1)
                            .concat(delimitante).concat(mensajeTercera1).concat(delimitante).concat(fechaConvenio).concat(delimitante).concat(inquietud);

                    String[] lineaSplit = mensajesConcat.split(delimitante);

                    List<List<String>> matriz = new ArrayList<>();

                    float espacioLinea = 0;

                    List<String> otroRenglon = new ArrayList<>();

                    for (String split : lineaSplit) {

                        List<String> nuevaLinea = null;

                        espacioLinea = 468;

                        float sizeLineaToCharacter = 0.0f;
                        String[] splitSplit = split.split(" ");
                        nuevaLinea = new ArrayList<>();
                        int tamanoParrafo = 0;
                        for (String string : splitSplit) {
                            float sizeSplit = 0.0f;
                            if (palabraResaltada(string, variablesConcatSplit)) {
                                contens.setFont(PDType1Font.HELVETICA_BOLD, 12);
                                sizeSplit = 12 * PDType1Font.HELVETICA_BOLD.getStringWidth(string) / 1000;
                            } else {
                                contens.setFont(PDType1Font.HELVETICA, 12);
                                sizeSplit = 12 * PDType1Font.HELVETICA.getStringWidth(string) / 1000;
                            }

                            if (espacioLinea > sizeSplit) {
                                sizeLineaToCharacter = sizeLineaToCharacter + sizeSplit;
                                espacioLinea = espacioLinea - sizeSplit - 5;
                                nuevaLinea.add(string);
                            } else {
                                matriz.add(nuevaLinea);
                                tamanoParrafo += nuevaLinea.size();
                                nuevaLinea = new ArrayList<>();
                                sizeLineaToCharacter = sizeLineaToCharacter + sizeSplit;
                                espacioLinea = width;
                                espacioLinea = espacioLinea - sizeSplit - 5;
                                nuevaLinea.add(string);
                            }
                        }

                        if (splitSplit.length > tamanoParrafo) {

                            float sizeSplit = 0.0f;
                            nuevaLinea = new ArrayList<>();
                            for (int i = tamanoParrafo; i < splitSplit.length; i++) {

                                String string = splitSplit[i];

                                if (palabraResaltada(split, variablesConcatSplit)) {
                                    contens.setFont(PDType1Font.HELVETICA_BOLD, 12);
                                    sizeSplit = 12 * PDType1Font.HELVETICA_BOLD.getStringWidth(string) / 1000;
                                } else {
                                    contens.setFont(PDType1Font.HELVETICA, 12);
                                    sizeSplit = 12 * PDType1Font.HELVETICA.getStringWidth(string) / 1000;
                                }
                                nuevaLinea.add(string);
                            }
                            matriz.add(nuevaLinea);
                            tamanoParrafo += nuevaLinea.size();
                            nuevaLinea = new ArrayList<>();
                            sizeLineaToCharacter = sizeLineaToCharacter + sizeSplit;
                            espacioLinea = width;
                            espacioLinea = espacioLinea - sizeSplit - 5;
                        }
                    }

                    var contador = 0;
                    for (List<String> list : matriz) {
                        int acumuladoEspacios = 0;

                        for (String string : list) {
                            if (palabraResaltada(string, variablesConcatSplit)) {
                                var tamanioPalabraNegrita = (12 * PDType1Font.HELVETICA_BOLD.getStringWidth(string) / 1000);
                                acumuladoEspacios += tamanioPalabraNegrita;
                            } else {
                                var tamanioPalabra = (12 * PDType1Font.HELVETICA.getStringWidth(string) / 1000);
                                acumuladoEspacios += tamanioPalabra;
                            }

                        }

                        for (String string : list) {
                            int diferencia = (int) width - acumuladoEspacios;
                            int aumentoEspacions = 0;
                            if (list.size() == 1) {
                                aumentoEspacions = 0;
                            } else {
                                if (diferencia == 0) {
                                    aumentoEspacions = 5;
                                } else {
                                    aumentoEspacions = diferencia / (list.size() - 1);

                                    String[] nombreSplit = nombreClienteSplit.split(" ");

                                    if (nombreSplit.length >= 4) {
                                        switch (contador) {
                                            case 2:
                                                aumentoEspacions = 10;
                                                break;
                                            case 6:
                                                aumentoEspacions = 5;
                                                break;
                                            case 8:
                                                aumentoEspacions = 10;
                                                break;
                                            case 13:
                                                aumentoEspacions = 5;
                                                break;
                                            case 15:
                                                aumentoEspacions = 5;

                                                break;
                                            case 16:
                                                aumentoEspacions = 5;

                                                break;
                                            default:
                                                aumentoEspacions = diferencia / (list.size() - 1);

                                        }
                                    } else {
                                        if (nombreClienteSplit.replace(" ", "").length() < 26) {
                                            switch (contador) {
                                                case 2:
                                                    aumentoEspacions = 10;
                                                    break;
                                                case 6:
                                                    aumentoEspacions = 0;
                                                    break;
                                                case 8:
                                                    aumentoEspacions = 5;
                                                    break;
                                                case 13:
                                                    aumentoEspacions = 5;
                                                    break;
                                                case 15:
                                                    aumentoEspacions = 0;

                                                    break;
                                                case 16:
                                                    aumentoEspacions = 5;

                                                    break;
                                                default:
                                                    aumentoEspacions = diferencia / (list.size() - 1);

                                            }
                                        } else {
                                            switch (contador) {
                                                case 1:
                                                    aumentoEspacions = 10;
                                                    break;
                                                case 5:
                                                    aumentoEspacions = 10;
                                                    break;
                                                case 7:
                                                    aumentoEspacions = 10;
                                                    break;
                                                case 12:
                                                    aumentoEspacions = 10;
                                                    break;
                                                case 14:
                                                    aumentoEspacions = 10;

                                                    break;
                                                case 15:
                                                    aumentoEspacions = 10;

                                                    break;
                                                default:
                                                    aumentoEspacions = diferencia / (list.size() - 1);

                                            }
                                        }

                                    }
                                }

                            }

                            if (palabraResaltada(string, variablesConcatSplit)) {

                                nuevaLinea(string, (int) margin, (int) yStart, contens, PDType1Font.HELVETICA_BOLD, 12);

                                margin += (12 * PDType1Font.HELVETICA_BOLD.getStringWidth(string) / 1000) + aumentoEspacions;
                            } else {

                                nuevaLinea(string, (int) margin, (int) yStart, contens, PDType1Font.HELVETICA, 12);
                                margin += (12 * PDType1Font.HELVETICA.getStringWidth(string) / 1000) + aumentoEspacions;
                            }
                        }
                        String[] nombreSplit = nombreClienteSplit.split(" ");
                        switch (contador) {
                            case 2:
                                yStart = yStart - 30;
                                break;
                            case 6:
                                yStart = yStart - 30;
                                break;
                            case 8:
                                yStart = yStart - 30;
                                break;
                            case 13:
                                yStart = yStart - 30;
                                break;
                            case 15:
                                yStart = yStart - 30;
                                break;
                            default:
                                yStart = yStart - 15;

                        }

                        margin = 72;
                        contador++;
                    }

                }

                PDPage page = new PDPage();
                doc.addPage(page);
                int cellHeight = 20;
                int cellWidth = 95;
                int height = (int) page.getTrimBox().getHeight();//792
                int width = (int) page.getTrimBox().getWidth();//612

                ClassPathResource resourceAcu = new ClassPathResource("electrohogarOpa.png");
                InputStream inputStreamAcu = resourceAcu.getInputStream();
                PDImageXObject logoImageAcu = PDImageXObject.createFromByteArray(doc, IOUtils.toByteArray(inputStreamAcu), "electrohogarOpa.png");

                try (PDPageContentStream contens = new PDPageContentStream(doc, page)) {

                    contens.drawImage(logoImageAcu, width / 2 - 150, height / 2 - 30, 300, 100);
                    //CREACION DEL TITULO
                    nuevaLinea(titulo, 160, 750, contens, PDType1Font.HELVETICA_BOLD, 18);

                    //CREACION DE LAS VARIABLES SUBTITULOS ANTES DE LAS TABLAS
                    String fechaDoc = "Fecha: ".toUpperCase();
                    String sedeDoc = "Sede: ".toUpperCase();
                    String nombreClienteDoc = "Nombre del cliente: ".toUpperCase();
                    String numeroDocumentoDoc = "Documento del cliente: ".toUpperCase();
                    String numeroObligacionDoc = "Pagare: ".toUpperCase();

                    //CREACION DE SUBTITULOS
                    nuevaLinea(nombreClienteDoc, 70, 720, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(nombreClienteSplit, 210, 720, contens, PDType1Font.HELVETICA, 12);

                    nuevaLinea(numeroDocumentoDoc, 70, 700, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(docCliente, 238, 700, contens, PDType1Font.HELVETICA, 12);

                    nuevaLinea(sedeDoc, 70, 680, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(sede, 110, 680, contens, PDType1Font.HELVETICA, 12);

                    nuevaLinea(fechaDoc, 70, 660, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(fecha, 117, 660, contens, PDType1Font.HELVETICA, 12);

                    nuevaLinea(numeroObligacionDoc, 70, 640, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(pagare, 130, 640, contens, PDType1Font.HELVETICA, 12);

                    String[] cabecerosList = {"#Cuotas", "F.Vencimiento", "Valor cuota", "Capital", "Honorarios", "Intereses"};

                    int inicioTablaX = 40;
                    int inicioTablaY = 590;

                    Gestiones ges = gestionList.get(0);

                    AcuerdoPago acuPago = new AcuerdoPago();

                    if (ges.getClasificacion() instanceof AcuerdoPago) {
                        acuPago = (AcuerdoPago) ges.getClasificacion();
                    }

                    double totalCuotas = 0.0;

                    for (int i = 0; i < acuPago.getCuotasList().size() + 1; i++) {
                        if (i == 0) {
                            for (int j = 0; j < cabecerosList.length; j++) {

                                if (j == 0 || j == 3) {
                                    cellWidth = 70;
                                    contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                    contens.stroke();
                                    nuevaLinea(cabecerosList[j], inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA_BOLD, 14);

                                    inicioTablaX = inicioTablaX + cellWidth;
                                } else {

                                    if (j == 1) {
                                        cellWidth = 110;
                                        contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                        contens.stroke();
                                        nuevaLinea(cabecerosList[j], inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA_BOLD, 14);

                                        inicioTablaX = inicioTablaX + cellWidth;
                                    } else {
                                        cellWidth = 95;
                                        contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                        contens.stroke();
                                        nuevaLinea(cabecerosList[j], inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA_BOLD, 14);

                                        inicioTablaX = inicioTablaX + cellWidth;
                                    }
                                }

                            }
                        }
                        if (i > 0) {
                            inicioTablaX = 40;
                            inicioTablaY = inicioTablaY - cellHeight;
                            List<String> body = new ArrayList<>();
                            body.add(Integer.toString(acuPago.getCuotasList().get(i - 1).getNumeroCuota()));
                            try {
                                body.add(Functions.dateToString(acuPago.getCuotasList().get(i - 1).getFechaVencimiento()));
                            } catch (ParseException ex) {
                                Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            body.add(formatNumber((int) acuPago.getCuotasList().get(i - 1).getValorCuota()));
                            body.add(formatNumber((int) acuPago.getCuotasList().get(i - 1).getCapitalCuota()));
                            totalCuotas = totalCuotas + acuPago.getCuotasList().get(i - 1).getCapitalCuota();
                            body.add(formatNumber((int) acuPago.getCuotasList().get(i - 1).getHonorarios()));
                            body.add(formatNumber((int) acuPago.getCuotasList().get(i - 1).getInteresCuota()));


                            for (int j = 0; j < body.size(); j++) {
                                if (j == 0 || j == 3) {
                                    cellWidth = 70;
                                    contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                    contens.stroke();
                                    nuevaLinea(body.get(j), inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA, 14);

                                    inicioTablaX = inicioTablaX + cellWidth;
                                } else {

                                    if (j == 1) {
                                        cellWidth = 110;
                                        contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                        contens.stroke();
                                        nuevaLinea(body.get(j), inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA, 14);

                                        inicioTablaX = inicioTablaX + cellWidth;
                                    } else {
                                        cellWidth = 95;
                                        contens.addRect(inicioTablaX, inicioTablaY, cellWidth, cellHeight);
                                        contens.stroke();
                                        nuevaLinea(body.get(j), inicioTablaX + 5, inicioTablaY + 3, contens, PDType1Font.HELVETICA, 14);

                                        inicioTablaX = inicioTablaX + cellWidth;
                                    }
                                }

                            }
                        }
                        if (i == acuPago.getCuotasList().size()) {

                            for (int j = 0; j < 6; j++) {
                                inicioTablaX = 40;
                                if (j == 0) {
                                    contens.addRect(inicioTablaX, inicioTablaY - cellHeight, 180, cellHeight);
                                    contens.stroke();
                                    nuevaLinea("Valor total del acuerdo", inicioTablaX + 5, inicioTablaY + 3 - cellHeight, contens, PDType1Font.HELVETICA_BOLD, 13);
                                }
                                if (j == 2) {
                                    cellWidth = 95;

                                    contens.addRect(inicioTablaX + 180, inicioTablaY - cellHeight, cellWidth * 4 - 25, cellHeight);
                                    contens.stroke();
                                    nuevaLinea(formatNumber((int) acuPago.getValorTotalAcuerdo()), inicioTablaX + 185, inicioTablaY + 3 - cellHeight, contens, PDType1Font.HELVETICA, 13);

                                }
                            }

                        }
                    }

                    contens.close();
                    String base64 = convertPdfToBase64(doc);
                    return base64;
                }
            }

        } catch (IOException e) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.WARNING, "PDF acuerdo: no se pudo generar para " + cpc.getNumeroObligacion(), e);
            return null;
        }

    }

    @Override
    public String generarReportePagoCuotas(PagosCuotasDto dto) throws IOException, ClassNotFoundException {

        CuentasPorCobrar cpc = cpcR.findByNumeroObligacion(dto.getNumeroObligacion());

        System.out.println(cpc.toString());

        System.out.println(dto.toString());
        

        if (Objects.isNull(cpc)) {
            return null;
        }

        String titulo = "ESTADO DE CUENTA";

        String sedeComercial = cpc.getSede().getNombreComercialSede().toUpperCase();
        String direccionSede = cpc.getSede().getDireccionSede().toUpperCase();
        String numeroSede = cpc.getSede().getTelefonoSede();
        //YEIMAR DIJO QUE EL NIT ES IGUAL PARA TODOS(SI NO ES ASI LO ACABO A PALO)
        String NIT = "NIT: 901056810-9 REGIMEN COMUN".toUpperCase();
        String idPago = "#".concat(dto.getNumeroRecibo());
        String cedulaDoc = "C.C: ".concat(cpc.getDocumentoCliente());
        String fechaRecibo = "";
        try {
            fechaRecibo = "Fecha: ".concat(Functions.dateToString(Functions.obtenerFechaYhora()));
        } catch (ParseException ex) {
            Logger.getLogger(GenerarPdfImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        //DECLARACION DE VARIABLES PARA EL BODY DE LA TABLA
        String[] clientePago = cpc.getCliente().split("-");
        for (String string : clientePago) {
            System.out.println(string);
        }

        System.out.println(clientePago[1]);
        int valorPago = dto.getValorTotal();
        MoneyConverters converter1 = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
        String valorEnPalabras = converter1.asWords(new BigDecimal(valorPago)).toUpperCase();

        String[] splitPalabras = valorEnPalabras.split(" ");

        if (splitPalabras[0].equals("UNO") || splitPalabras[0].equals("uno")) {
            splitPalabras[0] = "UN";
        }

        String valorDocumentoPalabras = "";

        for (String splitPalabra : splitPalabras) {
            valorDocumentoPalabras = valorDocumentoPalabras.concat(" ").concat(splitPalabra);
        }

        Usuario usu = usuClient.obtenerUsuario(dto.getUsername());
        if (Objects.isNull(usu)) {
            return null;
        }

        String tiposPago = dto.getMetodoPago();

        int pagoEfectivo = 0, pagoCheque = 0, pagoDebito = 0, pagoCredito = 0, adelantos = 0, consignacion = 0;
        int saldoTotalAcuerdo = dto.getAcuerdoTotal();
        int saldoTotalCapital = (int) dto.getSaldoCapital();
        int saldoTotalHonorarios =(int) dto.getSaldoHonorarios();
        int saldoTotalIntereses =(int) dto.getSaldoInteresesMora();

        switch (tiposPago) {
            case "EFECTIVO":
                pagoEfectivo = dto.getValorTotal();
                break;
            case "CHEQUE":
                pagoCheque = dto.getValorTotal();
                break;
            case "T. DEBITO":
                pagoDebito = dto.getValorTotal();
                break;
            case "T. CREDITO":
                pagoCredito = dto.getValorTotal();
                break;
            case "ADELANTOS APLICADOS":
                adelantos = dto.getValorTotal();
                break;
            case "CONSIGNACION":
                consignacion = dto.getValorTotal();
                break;
            default:
                throw new AssertionError();
        }

        String detallePago = dto.getDetalle().toUpperCase();
        String usuarioPago = usu.getNombres().toUpperCase().concat(" ").concat(usu.getApellidos()).toUpperCase();
        String saldo = "SALDO: ".concat(Integer.toString(dto.getSaldo()));
        String footer = "GMJ HOGAS S.A.S.";
        String firma = "Recibi conforme: __________________________";

        try {
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                int cellHeight = 90;
                int cellWidth = 550;
                int height = (int) page.getTrimBox().getHeight() / 2;//346
                int width = (int) page.getTrimBox().getWidth();//612

                ClassPathResource resource = new ClassPathResource("electrohogarOpa.png");
                InputStream inputStream = resource.getInputStream();
                PDImageXObject logoImage = PDImageXObject.createFromByteArray(doc, IOUtils.toByteArray(inputStream), "electrohogarOpa.png");

                try (PDPageContentStream contens = new PDPageContentStream(doc, page)) {

                    contens.drawImage(logoImage, 200, 700, 200, 80);

                    //TITULO PARTE SUPERIOR DERECHA NUEVO CAMBIO
                    nuevaLinea(titulo, 50, 740, contens, PDType1Font.HELVETICA_BOLD, 12);
                    nuevaLinea(idPago, 50, 725, contens, PDType1Font.HELVETICA, 12);

                    //PARTE SUPERIOR DERECHA NUEVO CAMBIO
                    nuevaLinea(sedeComercial, 400, 750, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea(direccionSede, 400, 735, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea(numeroSede, 400, 720, contens, PDType1Font.HELVETICA, 12);
                    nuevaLinea(NIT, 400, 705, contens, PDType1Font.HELVETICA, 12);

                    int inicioTablaX = 30;
                    int inicioTablaY = 620;

                    for (int i = 0; i < 2; i++) {
                        if (i == 0) {
                            contens.addRect(inicioTablaX, inicioTablaY + 20, cellWidth, cellHeight - 40);
                            contens.stroke();
                            nuevaLinea("Hemos recibido de: ".concat(clientePago[1]), inicioTablaX + 5, inicioTablaY + 55, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("La cantidad de: ".concat("$").concat(formatNumber(valorPago)), inicioTablaX + 5, inicioTablaY + 40, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Son: ".concat(valorDocumentoPalabras.trim()).toUpperCase(), inicioTablaX + 5, inicioTablaY + 25, contens, PDType1Font.HELVETICA, 9);

                            //PARTE SUPERIOR DERECHA PRIMERA FILA DE LA TABLA NUEVO CAMBIO
                            nuevaLinea(fechaRecibo, inicioTablaX + 400, inicioTablaY + 45, contens, PDType1Font.HELVETICA, 12);
                            nuevaLinea(cedulaDoc, inicioTablaX + 400, inicioTablaY + 30, contens, PDType1Font.HELVETICA, 12);
                        }

                        if (i == 1) {
                            contens.addRect(inicioTablaX, inicioTablaY - cellHeight - 50, cellWidth, 160);
                            contens.stroke();
                            nuevaLinea("Por concepto de: ".concat(detallePago), inicioTablaX + 5, inicioTablaY + 5, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Total efectivo: ", inicioTablaX + 5, inicioTablaY - 25, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Total cheque: ", inicioTablaX + 5, inicioTablaY - 40, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Total tarjeta débito: ", inicioTablaX + 5, inicioTablaY - 55, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Total tarjeta crédito: ", inicioTablaX + 5, inicioTablaY - 70, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Adelantos aplicados: ", inicioTablaX + 5, inicioTablaY - 85, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Consignacion: ", inicioTablaX + 5, inicioTablaY - 100, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(pagoEfectivo)), inicioTablaX + 120, inicioTablaY - 25, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(pagoCheque)), inicioTablaX + 120, inicioTablaY - 40, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(pagoDebito)), inicioTablaX + 120, inicioTablaY - 55, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(pagoCredito)), inicioTablaX + 120, inicioTablaY - 70, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(adelantos)), inicioTablaX + 120, inicioTablaY - 85, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("$".concat(formatNumber(consignacion)), inicioTablaX + 120, inicioTablaY - 100, contens, PDType1Font.HELVETICA, 11);

                            nuevaLinea("Saldo acuerdo: ".concat("$").concat(formatNumber(saldoTotalAcuerdo)), inicioTablaX + 350, inicioTablaY - 30, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Saldo capital: ".concat("$").concat(formatNumber(saldoTotalCapital)), inicioTablaX + 350, inicioTablaY - 45, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Saldo intereses: ".concat("$").concat(formatNumber(saldoTotalIntereses)), inicioTablaX + 350, inicioTablaY - 60, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea("Saldo honorarios: ".concat("$").concat(formatNumber(saldoTotalHonorarios)), inicioTablaX + 350, inicioTablaY - 75, contens, PDType1Font.HELVETICA, 11);

                            nuevaLinea(firma, inicioTablaX + 5, inicioTablaY - 125, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea(usuarioPago, inicioTablaX + 330, inicioTablaY - 125, contens, PDType1Font.HELVETICA, 11);
                            nuevaLinea(footer, 270, inicioTablaY - 137, contens, PDType1Font.HELVETICA, 8);

                        }

                    }

                    contens.close();
                    String base64 = convertPdfToBase64(doc);
                    return base64;

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertPdfToBase64(PDDocument document) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (document) {
                document.save(byteArrayOutputStream);
            }
            // Codificar en Base64
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    public static void nuevaLinea(String linea, int x, int y, PDPageContentStream contens, PDFont fuente, int tamañoFont) throws IOException {
        contens.beginText();
        PDFont font = fuente;
        contens.setFont(font, tamañoFont);
        contens.newLineAtOffset(x, y);

        int caracteresPorLinea = 80;
        if (linea.length() > caracteresPorLinea) {
            for (int i = 0; i < linea.length(); i += caracteresPorLinea) {
                int finIndice = Math.min(i + caracteresPorLinea, linea.length());
                String segmento = linea.substring(i, finIndice);
                contens.showText(segmento);
                contens.newLineAtOffset(0, -tamañoFont); // Salto de línea
            }
            contens.endText();
        } else {
            contens.showText(linea);
            contens.endText();
        }
    }

    public static void nuevaLineaLetras(String linea, int x, int y, PDPageContentStream contens, PDFont fuente, int tamañoFont) throws IOException {
        contens.beginText();
        PDFont font = fuente;
        contens.setFont(font, tamañoFont);
        contens.newLineAtOffset(x, y);

        int caracteresPorLinea = 90;
        if (linea.length() > caracteresPorLinea) {
            for (int i = 0; i < linea.length(); i += caracteresPorLinea) {
                int finIndice = Math.min(i + caracteresPorLinea, linea.length());
                String segmento = linea.substring(i, finIndice);
                contens.showText(segmento);
                contens.newLineAtOffset(0, -tamañoFont); // Salto de línea
            }
            contens.endText();
        } else {
            contens.showText(linea);
            contens.endText();
        }
    }

    public static String formatNumber(int numero1) {
        String numeroString = Integer.toString(numero1);
        int maxNumber = 3;

        if (numeroString.length() > maxNumber) {
            StringBuilder formattedNumber = new StringBuilder();
            int count = 0;

            for (int i = numeroString.length() - 1; i >= 0; i--) {
                char digit = numeroString.charAt(i);
                formattedNumber.insert(0, digit);

                count++;
                if (count == maxNumber && i > 0) {
                    formattedNumber.insert(0, '.');
                    count = 0;
                }
            }

            return formattedNumber.toString();
        }

        return numeroString;
    }

}
