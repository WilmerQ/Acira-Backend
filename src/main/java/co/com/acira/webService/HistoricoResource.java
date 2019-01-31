/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.clases.objetoEventoHistorial;
import co.com.acira.logica.CommonsBean;
import co.com.acira.logica.LogicaHistorial;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("historico")
@Stateless
public class HistoricoResource {

    @Context
    private UriInfo context;
    @EJB
    CommonsBean cb;
    @EJB
    LogicaHistorial lh;

    public HistoricoResource() {
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{tipoEvento}/{usuario}")
    public Response eventosXsemana(@PathParam("tipoEvento") String tipoEvento,
            @PathParam("usuario") String usuario) {
        try {
            System.out.println("--------------------------------------------");
            System.out.println("aqui eventosXsemana");
            System.out.println("parametro recibido " + tipoEvento);
            System.out.println("parametro recibido " + usuario);
            Gson gson = new Gson();
            String s = gson.fromJson(tipoEvento, String.class);
            Long identificacion = gson.fromJson(usuario, Long.class);
            if (identificacion != null) {
                if (s.equals("panico")) {
                    List<EventoPanico> eventoPanicos = lh.getAllPanicoxBarrio(identificacion);
                    List<objetoEventoHistorial> panicos = new ArrayList<>();
                    Date d = new Date(System.currentTimeMillis() - 3600 * 1000 * 24 * 7);
                    for (EventoPanico eventoPanico : eventoPanicos) {
                        if (eventoPanico.getFechaCreacion().after(d)) {
                            objetoEventoHistorial p = new objetoEventoHistorial();
                            p.setId(eventoPanico.getId());
                            p.setAnonimo(eventoPanico.getAnonimo());
                            if (!eventoPanico.getAnonimo()) {
                                p.setUsuarioInformante(eventoPanico.getUsuarioInformante().getNombrecompleto());
                            } else {
                                p.setUsuarioInformante("Anónimo");
                            }
                            p.setExactitud(eventoPanico.getExactitud());
                            p.setLatitud(eventoPanico.getLatitud());
                            p.setLongitud(eventoPanico.getLongitud());
                            p.setMensaje(eventoPanico.getMensaje());
                            p.setFechaRecolecion(eventoPanico.getFechaCreacion());
                            p.setTipo("panico");
                            if (eventoPanico.getUuidImagen() != null) {
                                p.setUuidImagen(eventoPanico.getUuidImagen());
                            } else {
                                p.setUuidImagen(null);
                            }
                            if (eventoPanico.getUuidAudio() != null) {
                                p.setUuidAudio(eventoPanico.getUuidAudio());
                            } else {
                                p.setUuidAudio(null);
                            }
                            panicos.add(p);
                        }
                    }
                    return new ResponseMessenger().getResponseOk(gson.toJson(panicos));
                } else if (s.equals("sospecha")) {
                    List<EventoSospecha> eventoPanicos = lh.getAllSospechaxBarrio(identificacion);
                    List<objetoEventoHistorial> panicos = new ArrayList<>();
                    Date d = new Date(System.currentTimeMillis() - 3600 * 1000 * 24 * 7);
                    for (EventoSospecha eventoPanico : eventoPanicos) {
                        if (eventoPanico.getFechaCreacion().after(d)) {
                            objetoEventoHistorial p = new objetoEventoHistorial();
                            p.setId(eventoPanico.getId());
                            p.setAnonimo(eventoPanico.getAnonimo());
                            if (!eventoPanico.getAnonimo()) {
                                p.setUsuarioInformante(eventoPanico.getUsuarioInformante().getNombrecompleto());
                            } else {
                                p.setUsuarioInformante("Anónimo");
                            }
                            p.setExactitud(eventoPanico.getExactitud());
                            p.setLatitud(eventoPanico.getLatitud());
                            p.setLongitud(eventoPanico.getLongitud());
                            p.setMensaje(eventoPanico.getMensaje());
                            p.setFechaRecolecion(eventoPanico.getFechaCreacion());
                            p.setTipo("sospecha");
                            if (eventoPanico.getUuidImagen() != null) {
                                p.setUuidImagen(eventoPanico.getUuidImagen());
                            } else {
                                p.setUuidImagen(null);
                            }
                            if (eventoPanico.getUuidAudio() != null) {
                                p.setUuidAudio(eventoPanico.getUuidAudio());
                            } else {
                                p.setUuidAudio(null);
                            }
                            panicos.add(p);
                        }
                    }
                    return new ResponseMessenger().getResponseOk(gson.toJson(panicos));
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: eventosXsemana: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }

}
