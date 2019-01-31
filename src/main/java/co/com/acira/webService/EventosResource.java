/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.clases.Coordenadas;
import co.com.acira.clases.nuevoUsuario;
import co.com.acira.logica.CommonsBean;
import co.com.acira.logica.LogicaNotificacionEventos;
import co.com.acira.modelo.Adjunto;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("eventos")
public class EventosResource {

    @Context
    private UriInfo context;

    @EJB
    private CommonsBean cb;
    @EJB
    private LogicaNotificacionEventos lne;

    public EventosResource() {
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/panico/{usuario}/{mensaje}/{coordenadas}/{anonimato}/{imagen}/{audio}")
    public Response Panico(@PathParam("usuario") String usuario,
            @PathParam("mensaje") String mensaje,
            @PathParam("coordenadas") String coordenadas,
            @PathParam("anonimato") Boolean anonimato,
            @PathParam("imagen") String imagen,
            @PathParam("audio") String audio) {
        try {
            System.out.println("--------------------------------------------------------");
            System.out.println("metodo panico");
            System.out.println("parametros recibido");
            System.out.println("usuario: " + usuario);
            System.out.println("mensaje: " + mensaje);
            System.out.println("coordenadas: " + coordenadas);
            System.out.println("anonimato: " + anonimato);
            System.out.println("uuid imagen: " + imagen);
            System.out.println("uuid audio: " + audio);

            Gson gson = new Gson();
            nuevoUsuario usuario1 = gson.fromJson(usuario, nuevoUsuario.class);
            String mensaje1 = gson.fromJson(mensaje, String.class);
            Coordenadas coordenadas1 = gson.fromJson(coordenadas, Coordenadas.class);
            String imagen1 = gson.fromJson(imagen, String.class);
            String audio1 = gson.fromJson(audio, String.class);
            EventoPanico eventoPanico = new EventoPanico();

            Usuario u = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
            if ((u != null) && (coordenadas1 != null)) {
                eventoPanico.setUsuarioInformante(u);
                if (mensaje1.equals("undefined")) {
                    eventoPanico.setMensaje("");
                } else {
                    eventoPanico.setMensaje(mensaje1);
                }
                if (imagen1.equals("undefined")) {
                    eventoPanico.setUuidImagen(null);
                } else {
                    eventoPanico.setUuidImagen(imagen1);
                }
                if (audio1.equals("undefined")) {
                    eventoPanico.setUuidAudio(null);
                } else {
                    eventoPanico.setUuidAudio(audio1);
                }
                eventoPanico.setAnonimo(anonimato);
                eventoPanico.setExactitud(coordenadas1.getAccuracy());
                eventoPanico.setLatitud(coordenadas1.getLatitude());
                eventoPanico.setLongitud(coordenadas1.getLongitude());
                eventoPanico.setFechaRecolecion(new Date());
                eventoPanico.setHora(new Date());
                if (cb.guardar(eventoPanico)) {
                    System.out.println("guardado evento panico");
                    List<EventoPanico> temp = (List<EventoPanico>) cb.getByOneField(EventoPanico.class, "usuarioInformante", eventoPanico.getUsuarioInformante());
                    if (!temp.isEmpty()) {
                        System.out.println("evento temp != null");
                        System.out.println("" + temp.get(temp.size() - 1).getId());
                        final EventoPanico parametro = temp.get(temp.size() - 1);
                        new Thread(new Runnable() {
                            private EventoPanico p = parametro;

                            @Override
                            public void run() {
                                lne.enviarNotificacionPanico(p);
                            }
                        }).start();
                    }
                    return new ResponseMessenger().getResponseOk("{\"mensaje\":\"evento de panico registrado\"}");
                } else {
                    System.out.println("Error: Panico: no se guardo panico en la base de datos");
                    return new ResponseMessenger().getResponseError("Intente nuevamente");
                }
            } else {
                System.out.println("Error: Panico: usuario y/o conrdenadas null");
                return new ResponseMessenger().getResponseError("Se ha detectado un problema, por favor reinicie la aplicación");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: Panico " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        } catch (Exception ex) {
            System.out.println("Error: Panico " + ex.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/sospecha/{usuario}/{mensaje}/{coordenadas}/{anonimato}/{imagen}/{audio}")
    public Response Sospecha(@PathParam("usuario") String usuario,
            @PathParam("mensaje") String mensaje,
            @PathParam("coordenadas") String coordenadas,
            @PathParam("anonimato") Boolean anonimato,
            @PathParam("imagen") String imagen,
            @PathParam("audio") String audio) {
        try {
            System.out.println("--------------------------------------------------------");
            System.out.println("metodo sospecha ");
            System.out.println("parametros recibido");
            System.out.println("usuario: " + usuario);
            System.out.println("mensaje: " + mensaje);
            System.out.println("coodenadas: " + coordenadas);
            System.out.println("anonimato: " + anonimato);
            System.out.println("uuid imagen: " + imagen);
            System.out.println("uudi audio: " + audio);

            Gson gson = new Gson();
            nuevoUsuario usuario1 = gson.fromJson(usuario, nuevoUsuario.class);
            String mensaje1 = gson.fromJson(mensaje, String.class);
            String imagen1 = gson.fromJson(imagen, String.class);
            String audio1 = gson.fromJson(audio, String.class);
            Coordenadas coordenadas1 = gson.fromJson(coordenadas, Coordenadas.class);
            EventoSospecha eventoSospecha = new EventoSospecha();

            Usuario u = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
            if ((u != null) && (coordenadas1 != null)) {
                eventoSospecha.setUsuarioInformante(u);
                eventoSospecha.setMensaje(mensaje1);
                eventoSospecha.setAnonimo(anonimato);
                eventoSospecha.setExactitud(coordenadas1.getAccuracy());
                eventoSospecha.setLatitud(coordenadas1.getLatitude());
                eventoSospecha.setLongitud(coordenadas1.getLongitude());
                eventoSospecha.setFechaRecolecion(new Date());
                eventoSospecha.setHora(new Date());
                if (imagen1.equals("undefined")) {
                    eventoSospecha.setUuidImagen(null);
                } else {
                    eventoSospecha.setUuidImagen(imagen1);
                }
                if (audio1.equals("undefined")) {
                    eventoSospecha.setUuidAudio(null);
                } else {
                    eventoSospecha.setUuidAudio(audio1);
                }
                if (cb.guardar(eventoSospecha)) {
                    System.out.println("guardado evento sospecha");
                    List<EventoSospecha> temp = (List<EventoSospecha>) cb.getByOneField(EventoSospecha.class, "usuarioInformante", eventoSospecha.getUsuarioInformante());
                    if (!temp.isEmpty()) {
                        System.out.println("evento temp != null");
                        System.out.println("" + temp.get(temp.size() - 1).getId());
                        final EventoSospecha parametro = temp.get(temp.size() - 1);
                        new Thread(new Runnable() {
                            private EventoSospecha p = parametro;

                            @Override
                            public void run() {
                                lne.enviarNotificacionSospecha(p);
                            }
                        }).start();
                    }
                    return new ResponseMessenger().getResponseOk("{\"mensaje\":\"sospecha registrada\"}");
                } else {
                    System.out.println("Error: Sospecha: no se guardo sospecha en la base de datos");
                    return new ResponseMessenger().getResponseError("Intente nuevamente");
                }
            } else {
                System.out.println("Error: Sospecha: usuario y/o cordenadas null");
                return new ResponseMessenger().getResponseError("Se ha detectado un problema, por favor reinicie la aplicación");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: Sospecha " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        } catch (Exception ex) {
            System.out.println("Error: Sospecha " + ex.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @Path("/subirfoto")
    public Response subirAnexo2(MultipartFormDataInput input) {
        try {
            System.out.println("---------------------------");
            System.out.println("metodo subir Anexo");
            try {
                Map<String, List<InputPart>> temp = input.getFormDataMap();
                temp.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v.size()));
                List<InputPart> parts = temp.get("UUID");
                String uuid = parts.get(0).getBodyAsString();
                InputPart filePart = temp.get("file").get(0);
//                result = filePart.getBody(InputStream.class, null);
//                MultivaluedMap<String, String> header = filePart.getHeaders();
//                System.out.println("-------------------------------------------");
//                header.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
//                List<String> ContentDisposition = header.get("Content-Disposition");
//                ContentDisposition.forEach((a) -> {
//                    System.out.println(a);
//                });
                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String fileName = ConfiguracionGeneral.RUTA + "imagenes" + File.separator + uuid + ".jpg";
                writeFile(bytes, fileName);
                Adjunto adjunto = new Adjunto();
                adjunto.setTipo("imagen");
                adjunto.setRuta(fileName);
                adjunto.setUuid(uuid);
                if (cb.guardar(adjunto)) {
                    return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Carga Exitosa\"}");
                } else {
                    System.out.println("Error: subirAnexo2: problema al guardar en el modelo");
                    return new ResponseMessenger().getResponseError("Problema interno del servidor");
                }
            } catch (Exception ex) {
                System.out.println("Error: subirAnexo2 " + ex.getLocalizedMessage());
                return new ResponseMessenger().getResponseError("Problema interno del servidor");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: subirAnexo2 " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @Path("/subirAudio")
    public Response subirAnexo3(MultipartFormDataInput input) {
        try {
            System.out.println("---------------------------");
            System.out.println("metodo subir Anexo Audio");
            try {
                Map<String, List<InputPart>> temp = input.getFormDataMap();
                temp.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v.size()));
                List<InputPart> parts = temp.get("UUID");
                String uuid = parts.get(0).getBodyAsString();
                InputPart filePart = temp.get("file").get(0);
//                result = filePart.getBody(InputStream.class, null);
//                MultivaluedMap<String, String> header = filePart.getHeaders();
//                System.out.println("-------------------------------------------");
//                header.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
//                List<String> ContentDisposition = header.get("Content-Disposition");
//                ContentDisposition.forEach((a) -> {
//                    System.out.println(a);
//                });   
                InputStream inputStream = filePart.getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String fileName = ConfiguracionGeneral.RUTA + "audios" + File.separator + uuid + ".3gp";
                writeFile(bytes, fileName);
                Adjunto adjunto = new Adjunto();
                adjunto.setTipo("audio");
                adjunto.setRuta(fileName);
                adjunto.setUuid(uuid);
                if (cb.guardar(adjunto)) {
                    return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Carga Exitosa\"}");
                } else {
                    System.out.println("Error: subirAnexo3: problema al guardar en el modelo");
                    return new ResponseMessenger().getResponseError("Problema interno del servidor");
                }
            } catch (Exception ex) {
                System.out.println("Error: subirAnexo2 " + ex.getLocalizedMessage());
                return new ResponseMessenger().getResponseError("Problema interno del servidor");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: subirAnexo2 " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
    }

    private void writeFile(byte[] content, String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fop = new FileOutputStream(file);
            fop.write(content);
            fop.flush();
            fop.close();
            System.out.println("Guargo archivo");
        } catch (IOException e) {
            System.out.println("Error: writeFile: " + e.getLocalizedMessage());
        }
    }
}
