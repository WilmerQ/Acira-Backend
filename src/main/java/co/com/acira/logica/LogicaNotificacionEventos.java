package co.com.acira.logica;

import co.com.acira.base.ConfiguracionGeneral;
import static co.com.acira.base.ConfiguracionGeneral.SMS;
import co.com.acira.clases.objetoDistanciaZona;
import co.com.acira.clases.objetoFcm;
import co.com.acira.clases.objetoJsonAccionNoti;
import co.com.acira.clases.objetoXmpp;

import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.GrupoFamiliar;
import co.com.acira.modelo.InformadosEventoSucedido;
import co.com.acira.modelo.PersonaSeguridadXZona;
import co.com.acira.modelo.Usuario;
import co.com.acira.modelo.Zona;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wedevol.xmpp.server.CcsClient;
import com.wedevol.xmpp.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.simple.JSONObject;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

/**
 *
 * @author wilme
 */
@Stateless
@LocalBean
public class LogicaNotificacionEventos implements Serializable {

    @PersistenceContext(unitName = "Acirav1PU")
    private EntityManager em;

    @EJB
    private CommonsBean cb;

    List<objetoXmpp> listToSend = new ArrayList<>();
    CcsClient cc;

    public void enviarNotificacionPanico(EventoPanico eventoPanico) {
        System.out.println("----------------------------------------------");
        System.out.println("LogicaNotificacionEventos enviar Notificacion de panico");
        try {
            objetoDistanciaZona zonaCercana = null;
            List<Zona> zonas = cb.getAll(Zona.class);
            if (!zonas.isEmpty()) {
                List<objetoDistanciaZona> dbs = new ArrayList<>();
                for (Zona b : zonas) {
                    dbs.add((objetoDistanciaZona) new objetoDistanciaZona(b, distanciaCoord(b.getLatitud(), b.getLongitud(), eventoPanico.getLatitud(), eventoPanico.getLongitud())));
                }
                if (!dbs.isEmpty()) {
                    dbs.forEach((db) -> {
                        System.out.println("distancia entre el evento de panico " + db.getDistancia() + " nombre de bario " + db.getZona().getBarrio().getNombre() + " nombre de la zona " + db.getZona().getNombre());
                    });
                }
                zonaCercana = dbs.stream().min(Comparator.comparing(objetoDistanciaZona::getDistancia)).orElseThrow(NoSuchElementException::new);
                System.out.println("barrio cercano " + zonaCercana.getZona().getBarrio().getNombre() + " distancia cercano " + zonaCercana.getDistancia());
            }

            if (zonaCercana.getDistancia() * 1000 < zonaCercana.getZona().getRangoResguardado() * 1.20) {
                System.out.println("alerta sucede dentro de un rango aceptable de la zona");
                NotificationPush();
                EncenderAlarmas(zonaCercana.getZona().getBarrio());
                try {
                    eventoPanico.setZonaRespondiente(zonaCercana.getZona());
                    em.merge(eventoPanico);
                } catch (Exception e) {
                    System.out.println("Error:  " + e.getLocalizedMessage());
                }
                String sql = "SELECT\n"
                        + "grupofamiliar.id,\n"
                        + "grupofamiliar.estado,\n"
                        + "grupofamiliar.fechaanulacion,\n"
                        + "grupofamiliar.fechacreacion,\n"
                        + "grupofamiliar.usuarioanulacion,\n"
                        + "grupofamiliar.usuariocreacion,\n"
                        + "grupofamiliar.version,\n"
                        + "grupofamiliar.numerousuarios,\n"
                        + "grupofamiliar.codigoasignado_id,\n"
                        + "grupofamiliar.usuarioprincipal_id\n"
                        + "FROM\n"
                        + "public.codigoseguridad,\n"
                        + "public.grupofamiliar,\n"
                        + "public.barrio\n"
                        + "WHERE \n"
                        + "codigoseguridad.id = grupofamiliar.codigoasignado_id AND\n"
                        + "barrio.id = codigoseguridad.barrio_id AND\n"
                        + "barrio.codigo =" + zonaCercana.getZona().getBarrio().getCodigo() + ";";

                Query query = em.createNativeQuery(sql, GrupoFamiliar.class);
                List<GrupoFamiliar> familiars = (List<GrupoFamiliar>) query.getResultList();
                if (!familiars.isEmpty()) {
                    System.out.println("familiars " + familiars.size());
                    Boolean agregrar = Boolean.TRUE;
                    for (GrupoFamiliar familiar : familiars) {
                        if (familiar.getId() == eventoPanico.getUsuarioInformante().getGrupoFamiliar().getId()) {
                            agregrar = Boolean.FALSE;
                        }
                    }
                    if (agregrar) {
                        familiars.add(eventoPanico.getUsuarioInformante().getGrupoFamiliar());
                    }
                    System.out.println("familiars " + familiars.size());
                    List<Usuario> usuariosParaNotificar = new ArrayList();
                    for (GrupoFamiliar gf : familiars) {
                        usuariosParaNotificar.addAll((List<Usuario>) cb.getByOneField(Usuario.class, "grupoFamiliar", gf));
                    }

                    if (!usuariosParaNotificar.isEmpty()) {
                        List<Usuario> us1 = new ArrayList<>();
                        for (Usuario u2 : usuariosParaNotificar) {
                            if (u2.getNumeroIdentificacion().equals(eventoPanico.getUsuarioInformante().getNumeroIdentificacion())) {
                                us1.add(u2);
                            }
                        }
                        System.out.println("remover usuario origen " + usuariosParaNotificar.removeAll(us1));
                        List<Usuario> usuariosParaNotificarXsms = new ArrayList<>();
                        for (Usuario u : usuariosParaNotificar) {
                            if (u.getTokenFCM() == null) {
                                usuariosParaNotificarXsms.add(u);
                            }
                        }
                        usuariosParaNotificar.removeAll(usuariosParaNotificarXsms);
                        System.out.println("usuarios para notificar " + usuariosParaNotificar.size());
                        System.out.println("usuarios que no tienen token asignados - se envianoticiacion sms " + usuariosParaNotificarXsms.size());
                        if (!usuariosParaNotificar.isEmpty()) {
                            listToSend.clear();
                            cc = CcsClient.prepareClient(ConfiguracionGeneral.idProjectFirebase, ConfiguracionGeneral.AuthKey, false);
                            try {
                                cc.connect();
                            } catch (XMPPException | InterruptedException e) {
                                e.printStackTrace();
                            } catch (SmackException | IOException ex) {
                                Logger.getLogger(LogicaNotificacionEventos.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            usuariosParaNotificar.forEach((xUsuario) -> {
                                SendFcmXmppPanico(xUsuario.getTokenFCM(), eventoPanico, xUsuario);
                            });
                            if (!listToSend.isEmpty()) {
                                cc.sendBroadcast(listToSend);
                            }
                        }

                        if (!usuariosParaNotificarXsms.isEmpty()) {
                            if (SMS) {
                                System.out.println("enviando usuariosParaNotificarXsms ");
                                usuariosParaNotificarXsms.forEach((u) -> {
                                    SendSMSPanico(eventoPanico, u);
                                });
                            }
                        }
                    }
                }
                List<PersonaSeguridadXZona> personaSeguridadXZonas = (List<PersonaSeguridadXZona>) cb.getByOneField(PersonaSeguridadXZona.class, "barrio", zonaCercana.getZona().getBarrio());
                if (!personaSeguridadXZonas.isEmpty()) {
                    personaSeguridadXZonas.forEach((psxz) -> {
                        SendSMSP(eventoPanico, psxz);
                    });
                }
            } else {
                System.out.println("alerta fuera del alcance de cualquier zona");
                NotificationPush();
                List<Usuario> usuariosFamiliar = (List<Usuario>) cb.getByOneField(Usuario.class, "grupoFamiliar", eventoPanico.getUsuarioInformante().getGrupoFamiliar());
                if (!usuariosFamiliar.isEmpty()) {
                    List<Usuario> us1 = new ArrayList<>();
                    usuariosFamiliar.stream().filter((u2) -> (u2.getNumeroIdentificacion().equals(eventoPanico.getUsuarioInformante().getNumeroIdentificacion()))).forEachOrdered((u2) -> {
                        us1.add(u2);
                    });
                    System.out.println("remover usuario origen " + usuariosFamiliar.removeAll(us1));
                    List<Usuario> usuariosParaNotificarXsms = new ArrayList<>();
                    for (Usuario u : usuariosFamiliar) {
                        if (u.getTokenFCM() == null) {
                            usuariosParaNotificarXsms.add(u);
                        }
                    }
                    usuariosFamiliar.removeAll(usuariosParaNotificarXsms);
                    System.out.println("usuarios para notificar " + usuariosFamiliar.size());
                    System.out.println("usuarios que no tienen token asignados - se envianoticiacion sms " + usuariosParaNotificarXsms.size());
                    if (!usuariosFamiliar.isEmpty()) {
                        listToSend.clear();
                        cc = CcsClient.prepareClient(ConfiguracionGeneral.idProjectFirebase, ConfiguracionGeneral.AuthKey, true);
                        try {
                            cc.connect();
                        } catch (XMPPException | InterruptedException e) {
                            e.printStackTrace();
                        } catch (SmackException | IOException ex) {
                            Logger.getLogger(LogicaNotificacionEventos.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        usuariosFamiliar.forEach((xUsuario) -> {
                            SendFcmXmppPanicoFueraZona(xUsuario.getTokenFCM(), eventoPanico, xUsuario);
                        });
                        if (!listToSend.isEmpty()) {
                            cc.sendBroadcast(listToSend);
                        }
                    }

                    if (!usuariosParaNotificarXsms.isEmpty()) {
                        if (SMS) {
                            for (Usuario u : usuariosParaNotificarXsms) {
                                System.out.println("enviando sms ");
                                SendSMSPanico(eventoPanico, u);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: enviarNotificacionPanico: " + e.getLocalizedMessage());
        }
    }

    public void enviarNotificacionSospecha(EventoSospecha eventoSospecha) {
        System.out.println("----------------------------------------------");
        System.out.println("LogicaNotificacionEventos enviar Notificacion de sospecha");
        try {
            objetoDistanciaZona zonaCercana = null;
            List<Zona> zonas = cb.getAll(Zona.class);
            if (!zonas.isEmpty()) {
                List<objetoDistanciaZona> dbs = new ArrayList<>();
                for (Zona b : zonas) {
                    dbs.add((objetoDistanciaZona) new objetoDistanciaZona(b, distanciaCoord(b.getLatitud(), b.getLongitud(), eventoSospecha.getLatitud(), eventoSospecha.getLongitud())));
                }
                if (!dbs.isEmpty()) {
                    dbs.forEach((db) -> {
                        System.out.println("distancia entre el evento de sospecha " + db.getDistancia() + " nombre de bario " + db.getZona().getBarrio().getNombre() + " nombre de la zona " + db.getZona().getNombre());
                    });
                }
                zonaCercana = dbs.stream().min(Comparator.comparing(objetoDistanciaZona::getDistancia)).orElseThrow(NoSuchElementException::new);
                System.out.println("barrio cercano " + zonaCercana.getZona().getBarrio().getNombre() + " distancia cercano " + zonaCercana.getDistancia());
            }

            if (zonaCercana.getDistancia() * 1000 < zonaCercana.getZona().getRangoResguardado() * 1.20) {
                System.out.println("alerta sucede dentro de un rango aceptable de la zona");
                NotificationPush();
                try {
                    eventoSospecha.setZonaRespondiente(zonaCercana.getZona());
                    em.merge(eventoSospecha);
                } catch (Exception e) {
                    System.out.println("Error: agregar zona respondiente " + e.getLocalizedMessage());
                }
                llamadasSospecha(zonaCercana.getZona().getBarrio());
                String sql = "SELECT\n"
                        + "grupofamiliar.id,\n"
                        + "grupofamiliar.estado,\n"
                        + "grupofamiliar.fechaanulacion,\n"
                        + "grupofamiliar.fechacreacion,\n"
                        + "grupofamiliar.usuarioanulacion,\n"
                        + "grupofamiliar.usuariocreacion,\n"
                        + "grupofamiliar.version,\n"
                        + "grupofamiliar.numerousuarios,\n"
                        + "grupofamiliar.codigoasignado_id,\n"
                        + "grupofamiliar.usuarioprincipal_id\n"
                        + "FROM\n"
                        + "public.codigoseguridad,\n"
                        + "public.grupofamiliar,\n"
                        + "public.barrio\n"
                        + "WHERE \n"
                        + "codigoseguridad.id = grupofamiliar.codigoasignado_id AND\n"
                        + "barrio.id = codigoseguridad.barrio_id AND\n"
                        + "barrio.codigo =" + zonaCercana.getZona().getBarrio().getCodigo() + ";";

                Query query = em.createNativeQuery(sql, GrupoFamiliar.class);
                List<GrupoFamiliar> familiars = (List<GrupoFamiliar>) query.getResultList();
                if (!familiars.isEmpty()) {
                    System.out.println("familiars " + familiars.size());
                    Boolean agregrar = Boolean.TRUE;
                    for (GrupoFamiliar familiar : familiars) {
                        if (familiar.getId() == eventoSospecha.getUsuarioInformante().getGrupoFamiliar().getId()) {
                            agregrar = Boolean.FALSE;
                        }
                    }
                    if (agregrar) {
                        familiars.add(eventoSospecha.getUsuarioInformante().getGrupoFamiliar());
                    }
                    System.out.println("familiars " + familiars.size());
                    List<Usuario> usuariosParaNotificar = new ArrayList();
                    for (GrupoFamiliar gf : familiars) {
                        usuariosParaNotificar.addAll((List<Usuario>) cb.getByOneField(Usuario.class, "grupoFamiliar", gf));
                    }

                    if (!usuariosParaNotificar.isEmpty()) {
                        List<Usuario> us1 = new ArrayList<>();
                        for (Usuario u2 : usuariosParaNotificar) {
                            if (u2.getNumeroIdentificacion().equals(eventoSospecha.getUsuarioInformante().getNumeroIdentificacion())) {
                                us1.add(u2);
                            }
                        }
                        System.out.println("remover usuario origen " + usuariosParaNotificar.removeAll(us1));
                        List<Usuario> usuariosParaNotificarXsms = new ArrayList<>();
                        for (Usuario u : usuariosParaNotificar) {
                            if (u.getTokenFCM() == null) {
                                usuariosParaNotificarXsms.add(u);
                            }
                        }
                        usuariosParaNotificar.removeAll(usuariosParaNotificarXsms);
                        System.out.println("usuarios para notificar " + usuariosParaNotificar.size());
                        System.out.println("usuarios que no tienen token asignados - se envianoticiacion sms " + usuariosParaNotificarXsms.size());
                        if (!usuariosParaNotificar.isEmpty()) {
                            listToSend.clear();
                            cc = CcsClient.prepareClient(ConfiguracionGeneral.idProjectFirebase, ConfiguracionGeneral.AuthKey, true);
                            try {
                                cc.connect();
                            } catch (XMPPException | InterruptedException e) {
                                e.printStackTrace();
                            } catch (SmackException | IOException ex) {
                                Logger.getLogger(LogicaNotificacionEventos.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            usuariosParaNotificar.forEach((xUsuario) -> {
                                SendFCMXmppSospecha(xUsuario.getTokenFCM(), eventoSospecha, xUsuario);
                            });
                            if (!listToSend.isEmpty()) {
                                cc.sendBroadcast(listToSend);
                            }
                        }

                        if (!usuariosParaNotificarXsms.isEmpty()) {
                            if (SMS) {
                                System.out.println("enviando usuariosParaNotificarXsms ");
                                usuariosParaNotificarXsms.forEach((u) -> {
                                    SendSMSSospecha(eventoSospecha, u);
                                });
                            }
                        }
                    }
                }
                List<PersonaSeguridadXZona> personaSeguridadXZonas = (List<PersonaSeguridadXZona>) cb.getByOneField(PersonaSeguridadXZona.class, "barrio", zonaCercana.getZona().getBarrio());
                if (!personaSeguridadXZonas.isEmpty()) {
                    personaSeguridadXZonas.forEach((psxz) -> {
                        SendSMSS(eventoSospecha, psxz);
                    });
                }
            } else {
                System.out.println("alerta fuera del alcance de cualquier zona");
                NotificationPush();
                List<Usuario> usuariosFamiliar = (List<Usuario>) cb.getByOneField(Usuario.class, "grupoFamiliar", eventoSospecha.getUsuarioInformante().getGrupoFamiliar());
                if (!usuariosFamiliar.isEmpty()) {
                    List<Usuario> us1 = new ArrayList<>();
                    usuariosFamiliar.stream().filter((u2) -> (u2.getNumeroIdentificacion().equals(eventoSospecha.getUsuarioInformante().getNumeroIdentificacion()))).forEachOrdered((u2) -> {
                        us1.add(u2);
                    });
                    System.out.println("remover usuario origen " + usuariosFamiliar.removeAll(us1));
                    List<Usuario> usuariosParaNotificarXsms = new ArrayList<>();
                    for (Usuario u : usuariosFamiliar) {
                        if (u.getTokenFCM() == null) {
                            usuariosParaNotificarXsms.add(u);
                        }
                    }
                    usuariosFamiliar.removeAll(usuariosParaNotificarXsms);
                    System.out.println("usuarios para notificar " + usuariosFamiliar.size());
                    System.out.println("usuarios que no tienen token asignados - se envianoticiacion sms " + usuariosParaNotificarXsms.size());
                    if (!usuariosFamiliar.isEmpty()) {
                        listToSend.clear();
                        cc = CcsClient.prepareClient(ConfiguracionGeneral.idProjectFirebase, ConfiguracionGeneral.AuthKey, false);
                        try {
                            cc.connect();
                        } catch (XMPPException | InterruptedException e) {
                            e.printStackTrace();
                        } catch (SmackException | IOException ex) {
                            Logger.getLogger(LogicaNotificacionEventos.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        usuariosFamiliar.forEach((xUsuario) -> {
                            SendFCMXmppSospechaFueraZona(xUsuario.getTokenFCM(), eventoSospecha, xUsuario);
                        });
                        if (!listToSend.isEmpty()) {
                            cc.sendBroadcast(listToSend);
                        }
                    }

                    if (!usuariosParaNotificarXsms.isEmpty()) {
                        if (SMS) {
                            for (Usuario u : usuariosParaNotificarXsms) {
                                System.out.println("enviando sms");
                                SendSMSSospecha(eventoSospecha, u);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: enviarNotificacionSospecha: " + e.getLocalizedMessage());
        }
    }

    public void SendFcmXmppPanico(String token, EventoPanico panico, Usuario u) {
        String messageId = Util.getUniqueMessageId();

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");
        json.put("delivery_receipt_requested", true);
        json.put("message_id", messageId);

        JSONObject data = new JSONObject();
        data.put("title", "Evento de Panico");
        data.put("notId", panico.getId());

        if (panico.getAnonimo()) {
            if (panico.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de panico"
                        + " cerca a su lugar de residencia.<br />"
                        + "Hora:" + panico.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de pánico"
                        + " cerca a su lugar de residencia.<br />"
                        + "Hora:" + panico.getHora() + "<br />"
                        + "Mensaje: " + panico.getMensaje() + "<br />"
                        + "Preste Atencion y tenga cuidado]]>");
            }
        } else {
            if (panico.getMensaje().length() < 5) {
                data.put("message", " <![CDATA[Se recibe el reporte de un evento de panico"
                        + " cerca a su lugar de residencia. <br />"
                        + "Por: " + panico.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora: " + panico.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado. ]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de pánico"
                        + " cerca a su lugar de residencia. <br />"
                        + "Por: " + panico.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora: " + panico.getHora() + "<br />"
                        + "Mensaje: " + panico.getMensaje() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);
//        if (panico.getUuidImagen().length() > 5) {
//            data.put("style", "picture");
//            data.put("picture", "http://localhost:9393/Acira/ImagenEventos?id=" + panico.getUuidImagen());
//        }

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        accion1.setIcon("ignorar");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        accion2.setIcon("mapa");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(panico.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(panico.getHora());
        fcmPanico.setLatitud(panico.getLatitud());
        fcmPanico.setLongitud(panico.getLongitud());
        fcmPanico.setExactitud(panico.getExactitud());
        fcmPanico.setTipo("Panico");
        fcmPanico.setUuidImagen(panico.getUuidImagen());
        fcmPanico.setUuidAudio(panico.getUuidAudio());
        data.put("panico", gson.toJson(fcmPanico));
        data.put("comandosSecundarios", "{\"estado\":\"\null\"}");
        json.put("data", data);

        objetoXmpp xmpp = new objetoXmpp();
        xmpp.setEventoPanico(panico);
        xmpp.setIdmensaje(messageId);
        xmpp.setMensaje(json.toJSONString());
        xmpp.setUsuario(u);
        listToSend.add(xmpp);
    }

    public void SendFcmXmppPanicoFueraZona(String token, EventoPanico panico, Usuario u) {
        String messageId = Util.getUniqueMessageId();

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");
        json.put("delivery_receipt_requested", true);
        json.put("message_id", messageId);

        JSONObject data = new JSONObject();
        json.put("message_id", messageId);
        data.put("title", "Evento de Panico");
        data.put("notId", panico.getId());

        if (panico.getAnonimo()) {
            if (panico.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de panico"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Hora:" + panico.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de pánico"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Hora:" + panico.getHora() + "<br />"
                        + "Mensaje: " + panico.getMensaje() + "<br />"
                        + "Preste Atencion y tenga cuidado]]>");
            }
        } else {
            if (panico.getMensaje().length() < 5) {
                data.put("message", " <![CDATA[Se recibe el reporte de un evento de panico"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Por: " + panico.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora: " + panico.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado. ]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento de pánico"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Por: " + panico.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora: " + panico.getHora() + "<br />"
                        + "Mensaje: " + panico.getMensaje() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);
//        if (panico.getUuidImagen().length() > 5) {
//            data.put("style", "picture");
//            data.put("picture", "http://localhost:9393/Acira/ImagenEventos?id=" + panico.getUuidImagen());
//        }

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        accion1.setIcon("ignorar");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        accion2.setIcon("mapa");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(panico.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(panico.getHora());
        fcmPanico.setLatitud(panico.getLatitud());
        fcmPanico.setLongitud(panico.getLongitud());
        fcmPanico.setExactitud(panico.getExactitud());
        fcmPanico.setUuidImagen(panico.getUuidImagen());
        fcmPanico.setUuidAudio(panico.getUuidAudio());
        fcmPanico.setTipo("Panico");
        data.put("panico", gson.toJson(fcmPanico));
        data.put("comandosSecundarios", "{\"estado\":\"\null\"}");
        json.put("data", data);

        objetoXmpp xmpp = new objetoXmpp();
        xmpp.setEventoPanico(panico);
        xmpp.setIdmensaje(messageId);
        xmpp.setMensaje(json.toJSONString());
        xmpp.setUsuario(u);
        listToSend.add(xmpp);
    }

    public void SendFCMXmppSospecha(String token, EventoSospecha sospecha, Usuario u) {
        String messageId = Util.getUniqueMessageId();

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");
        json.put("delivery_receipt_requested", true);
        json.put("message_id", messageId);

        JSONObject data = new JSONObject();
        data.put("title", "Reporte de sospecha");
        data.put("notId", sospecha.getId());
        if (sospecha.getAnonimo()) {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia.<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia.<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Mensaje: " + sospecha.getMensaje() + ".<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        } else {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia.<br />"
                        + "Por: " + sospecha.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora:" + sospecha.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia.<br />"
                        + "Por " + sospecha.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Mensaje: " + sospecha.getMensaje() + ".<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        accion1.setIcon("ignorar");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        accion2.setIcon("mapa");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(sospecha.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(sospecha.getHora());
        fcmPanico.setLatitud(sospecha.getLatitud());
        fcmPanico.setLongitud(sospecha.getLongitud());
        fcmPanico.setExactitud(sospecha.getExactitud());
        fcmPanico.setUuidImagen(sospecha.getUuidImagen());
        fcmPanico.setUuidAudio(sospecha.getUuidAudio());
        fcmPanico.setTipo("Sospecha");
        data.put("panico", gson.toJson(fcmPanico));
        data.put("comandosSecundarios", "{\"estado\":\"\null\"}");
        json.put("data", data);

        objetoXmpp xmpp = new objetoXmpp();
        xmpp.setEventoSospecha(sospecha);
        xmpp.setIdmensaje(messageId);
        xmpp.setMensaje(json.toJSONString());
        xmpp.setUsuario(u);
        listToSend.add(xmpp);
    }

    public void SendFCMXmppSospechaFueraZona(String token, EventoSospecha sospecha, Usuario u) {
        String messageId = Util.getUniqueMessageId();

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");
        json.put("delivery_receipt_requested", true);
        json.put("message_id", messageId);

        JSONObject data = new JSONObject();
        data.put("title", "Reporte de sospecha");
        data.put("notId", sospecha.getId());
        if (sospecha.getAnonimo()) {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Mensaje: " + sospecha.getMensaje() + ".<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        } else {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Por: " + sospecha.getUsuarioInformante().getNombrecompleto() + "<br />"
                        + "Hora:" + sospecha.getHora() + "<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            } else {
                data.put("message", "<![CDATA[Se recibe el reporte de un evento sospechoso"
                        + " por parte de unos de los miembros de su grupo familiar.<br />"
                        + "Por: " + sospecha.getUsuarioInformante().getNombrecompleto() + ".<br />"
                        + "Hora: " + sospecha.getHora() + "<br />"
                        + "Mensaje: " + sospecha.getMensaje() + ".<br />"
                        + "Preste Atencion y tenga cuidado.]]>");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        accion1.setIcon("ignorar");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        accion2.setIcon("mapa");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(sospecha.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(sospecha.getHora());
        fcmPanico.setLatitud(sospecha.getLatitud());
        fcmPanico.setLongitud(sospecha.getLongitud());
        fcmPanico.setExactitud(sospecha.getExactitud());
        fcmPanico.setUuidImagen(sospecha.getUuidImagen());
        fcmPanico.setUuidAudio(sospecha.getUuidAudio());
        fcmPanico.setTipo("Sospecha");
        data.put("panico", gson.toJson(fcmPanico));
        data.put("comandosSecundarios", "{\"estado\":\"\null\"}");
        json.put("data", data);

        objetoXmpp xmpp = new objetoXmpp();
        xmpp.setEventoSospecha(sospecha);
        xmpp.setIdmensaje(messageId);
        xmpp.setMensaje(json.toJSONString());
        xmpp.setUsuario(u);
        listToSend.add(xmpp);
    }

    public void SendSMSPanico(EventoPanico panico, Usuario usuario) {
        try {
            String urlServicio = null;
            String nombreCompleto = null;
            String direccion = null;

            GrupoFamiliar gf = usuario.getGrupoFamiliar();
            if (gf != null) {
                System.out.println("gf =! null");
                if (!panico.getAnonimo()) {
                    if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                    } else {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                    }
                } else {
                    nombreCompleto = "Usuario Anonimo";
                }
                if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                    direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                } else {
                    direccion = gf.getCodigoAsignado().getDireccion();
                }
                urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento de pánico cerca su lugar de residencia por:"
                        + nombreCompleto
                        + ", Reside:" + direccion
                        + ", Hora:" + panico.getHora().toString().substring(0, 5)
                        + ", Preste Atención" + "&from=seamco";
                urlServicio = urlServicio.replaceAll(" ", "%20");
                urlServicio = urlServicio.replaceAll("#", "N.");
                System.out.println("url servicio if " + urlServicio);
            }

            System.out.println("url servicio" + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("conexion aqui --------" + connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            InformadosEventoSucedido ies = new InformadosEventoSucedido();
            ies.setCodigoEntrega(finalStr.toString());
            ies.setEstado(Boolean.TRUE);
            ies.setMedioUtlizado("SMS");
            ies.setMensajeNativo(urlServicio);
            ies.setFechaCreacion(new Date());
            ies.setExtra("usuario sin token");
            ies.setEventoPanico(panico);
            guardarEnBaseDatos2(ies);

        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-SendSMSPanico: " + e.getLocalizedMessage());
        }
    }

    public void SendSMSSospecha(EventoSospecha panico, Usuario usuario) {
        try {
            String urlServicio = null;
            String nombreCompleto = null;
            String direccion = null;
            GrupoFamiliar gf = usuario.getGrupoFamiliar();

            if (gf != null) {
                System.out.println("gf =! null");
                if (!panico.getAnonimo()) {
                    if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                    } else {
                        nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                    }
                } else {
                    nombreCompleto = "Usuario anonimo";
                }
                if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                    direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                } else {
                    direccion = gf.getCodigoAsignado().getDireccion();
                }
                urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + usuario.getTelefono() + "&content=ACIRA: Se recibe el reporte de un evento sospechoso cerca su lugar de residencia por:"
                        + nombreCompleto
                        + ", Reside:" + direccion
                        + ", Hora:" + panico.getHora().toString().substring(0, 5)
                        + ", Preste Atención" + "&from=seamco";
                urlServicio = urlServicio.replaceAll(" ", "%20");
                urlServicio = urlServicio.replaceAll("#", "N.");
                System.out.println("url servicio if " + urlServicio);
            }

            System.out.println("url servicio" + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("conexion aqui --------" + connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            InformadosEventoSucedido ies = new InformadosEventoSucedido();
            ies.setCodigoEntrega(finalStr.toString());
            ies.setEstado(Boolean.TRUE);
            ies.setMedioUtlizado("SMS");
            ies.setMensajeNativo(urlServicio);
            ies.setFechaCreacion(new Date());
            ies.setExtra("usuario sin token");
            ies.setEventoSospecha(panico);
            guardarEnBaseDatos2(ies);

        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-SendSMSSospecha: " + e.getLocalizedMessage());
        }
    }

    public static double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {
        //double radioTierra = 3958.75;//en millas  
        double radioTierra = 6371;//en kilómetros  
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;

        return distancia;
    }

    public void EncenderAlarma(Barrio barrio) {
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Encender Alarma");
        try {
            String telefonos = "";
            List<PersonaSeguridadXZona> personaSeguridadXZonas = (List<PersonaSeguridadXZona>) cb.getByOneField(PersonaSeguridadXZona.class, "barrio", barrio);
            if (!personaSeguridadXZonas.isEmpty()) {
                int cont = 1;
                for (PersonaSeguridadXZona p : personaSeguridadXZonas) {
                    telefonos = telefonos + "tele" + cont + ":" + p.getNumeroTelefono() + ",";
                    cont += 1;
                }
            }

            if (barrio.getNumeroTelefono() != null) {
                String urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + barrio.getNumeroTelefono() + "&content=panico-" + telefonos + "end&from=seamco";

                System.out.println("url servicio" + urlServicio);
                URL url = new URL(urlServicio);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                System.out.println("conexion aqui --------" + connection);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str;
                StringBuilder finalStr = new StringBuilder();
                while ((str = in.readLine()) != null) {
                    finalStr.append(str);
                    System.out.println("leyendo datos");
                }
                in.close();
                System.out.println("result:  " + finalStr.toString());
                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                ies.setCodigoEntrega(finalStr.toString());
                ies.setEstado(Boolean.TRUE);
                ies.setMedioUtlizado("SMS");
                ies.setMensajeNativo(urlServicio);
                ies.setFechaCreacion(new Date());
                ies.setExtra("Encender Alarma");
                if (guardarEnBaseDatos2(ies)) {
                    System.out.println("Guardo en bd - EncenderAlarma");
                } else {
                    System.out.println("no guardo");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-EncenderAlarma: " + e.getLocalizedMessage());
        }
    }

    public void EncenderAlarmas(Barrio barrio) {
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Encender todas las alarmas");
        try {
            String telefonos = "";
            List<PersonaSeguridadXZona> personaSeguridadXZonas = (List<PersonaSeguridadXZona>) cb.getByOneField(PersonaSeguridadXZona.class, "barrio", barrio);
            if (!personaSeguridadXZonas.isEmpty()) {
                int cont = 1;
                for (PersonaSeguridadXZona p : personaSeguridadXZonas) {
                    telefonos = telefonos + "tele" + cont + ":" + p.getNumeroTelefono() + ",";
                    cont += 1;
                }
            }

            List<Zona> zonas = (List<Zona>) cb.getByOneField(Zona.class, "barrio", barrio);
            if (!zonas.isEmpty()) {
                for (Zona a : zonas) {
                    if (a.getNumeroTelefono() != null) {
                        if (!Objects.equals(a.getNumeroTelefono(), barrio.getNumeroTelefono())) {
                            String urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                                    + a.getNumeroTelefono() + "&content=panico-end&from=seamco";

                            System.out.println("url servicio" + urlServicio);
                            URL url = new URL(urlServicio);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            System.out.println("conexion aqui --------" + connection);
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");

                            BufferedReader in;
                            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String str;
                            StringBuilder finalStr = new StringBuilder();
                            while ((str = in.readLine()) != null) {
                                finalStr.append(str);
                                System.out.println("leyendo datos");
                            }
                            in.close();
                            System.out.println("result:  " + finalStr.toString());
                            InformadosEventoSucedido ies = new InformadosEventoSucedido();
                            ies.setCodigoEntrega(finalStr.toString());
                            ies.setEstado(Boolean.TRUE);
                            ies.setMedioUtlizado("SMS");
                            ies.setMensajeNativo(urlServicio);
                            ies.setFechaCreacion(new Date());
                            ies.setExtra("Encender Alarma");
                            if (guardarEnBaseDatos2(ies)) {
                                System.out.println("Guardo en bd - EncenderAlarma");
                            } else {
                                System.out.println("no guardo");
                            }
                        } else {
                            if (barrio.getNumeroTelefono() != null) {
                                String urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                                        + barrio.getNumeroTelefono() + "&content=panico-" + telefonos + "end&from=seamco";

                                System.out.println("url servicio" + urlServicio);
                                URL url = new URL(urlServicio);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                System.out.println("conexion aqui --------" + connection);
                                connection.setUseCaches(false);
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setRequestMethod("POST");

                                BufferedReader in;
                                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String str;
                                StringBuilder finalStr = new StringBuilder();
                                while ((str = in.readLine()) != null) {
                                    finalStr.append(str);
                                    System.out.println("leyendo datos");
                                }
                                in.close();
                                System.out.println("result:  " + finalStr.toString());
                                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                                ies.setCodigoEntrega(finalStr.toString());
                                ies.setEstado(Boolean.TRUE);
                                ies.setMedioUtlizado("SMS");
                                ies.setMensajeNativo(urlServicio);
                                ies.setFechaCreacion(new Date());
                                ies.setExtra("Encender Alarma");
                                if (guardarEnBaseDatos2(ies)) {
                                    System.out.println("Guardo en bd - EncenderAlarma");
                                } else {
                                    System.out.println("no guardo");
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-EncenderAlarmas: " + e.getLocalizedMessage());
        }
    }

    public void llamadasSospecha(Barrio barrio) {
        System.out.println("-----------------------------------------------------------------");
        System.out.println("llamadas a personal de seguridad en caso de sospecha");
        try {
            String telefonos = "";

            List<PersonaSeguridadXZona> personaSeguridadXZonas = (List<PersonaSeguridadXZona>) cb.getByOneField(PersonaSeguridadXZona.class, "barrio", barrio);

            if (!personaSeguridadXZonas.isEmpty()) {
                int cont = 1;
                for (PersonaSeguridadXZona p : personaSeguridadXZonas) {
                    telefonos = telefonos + "tele" + cont + ":" + p.getNumeroTelefono() + ",";
                    cont += 1;
                }
            }

            if (barrio.getNumeroTelefono() != null) {
                String urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + barrio.getNumeroTelefono() + "&content=sospecha-" + telefonos + "end&from=seamco";

                System.out.println("url servicio" + urlServicio);
                URL url = new URL(urlServicio);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                System.out.println("conexion aqui --------" + connection);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String str;
                StringBuilder finalStr = new StringBuilder();
                while ((str = in.readLine()) != null) {
                    finalStr.append(str);
                    System.out.println("leyendo datos");
                }
                in.close();
                System.out.println("result:  " + finalStr.toString());
                InformadosEventoSucedido ies = new InformadosEventoSucedido();
                ies.setCodigoEntrega(finalStr.toString());
                ies.setEstado(Boolean.TRUE);
                ies.setMedioUtlizado("SMS");
                ies.setMensajeNativo(urlServicio);
                ies.setFechaCreacion(new Date());
                ies.setExtra("llamadas Sospecha");
                if (guardarEnBaseDatos2(ies)) {
                    System.out.println("Guardo en bd - llamadasSospecha");
                } else {
                    System.out.println("no guardo");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-llamadasSospecha: " + e.getLocalizedMessage());
        }
    }

    public void SendSMSP(EventoPanico panico, PersonaSeguridadXZona personaSeguridadXZona) {
        try {
            String urlServicio = null;
            String nombreCompleto = null;
            String direccion = null;

            GrupoFamiliar gf = panico.getUsuarioInformante().getGrupoFamiliar();
            if (gf != null) {
                System.out.println("gf =! null");
                if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                    nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                } else {
                    nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                }
                if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                    direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                } else {
                    direccion = gf.getCodigoAsignado().getDireccion();
                }
                urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + personaSeguridadXZona.getNumeroTelefono() + "&content=ACIRA: Se recibe el reporte de un evento de pánico cerca su lugar de residencia por:"
                        + nombreCompleto
                        + ", Reside:" + direccion
                        + ", Hora:" + panico.getHora().toString().substring(0, 5)
                        + ", Preste Atención" + "&from=seamco";
                urlServicio = urlServicio.replaceAll(" ", "%20");
                urlServicio = urlServicio.replaceAll("#", "N.");
                System.out.println("url servicio if " + urlServicio);
            }

            System.out.println("url servicio" + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("conexion aqui --------" + connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            InformadosEventoSucedido ies = new InformadosEventoSucedido();
            ies.setCodigoEntrega(finalStr.toString());
            ies.setEstado(Boolean.TRUE);
            ies.setEventoPanico(panico);
            ies.setPersonaSeguridadXZona(personaSeguridadXZona);
            ies.setMedioUtlizado("SMS");
            ies.setMensajeNativo(urlServicio);
            ies.setFechaCreacion(new Date());
            if (guardarEnBaseDatos2(ies)) {
                System.out.println("Guardo en bd - SendSMSS");
            } else {
                System.out.println("no guardo");
            }
        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-SendSMSP: " + e.getLocalizedMessage());
        }
    }

    public void SendSMSS(EventoSospecha panico, PersonaSeguridadXZona personaSeguridadXZona) {
        try {
            String urlServicio = null;
            String nombreCompleto = null;
            String direccion = null;

            GrupoFamiliar gf = panico.getUsuarioInformante().getGrupoFamiliar();
            if (gf != null) {
                System.out.println("gf =! null");
                if (panico.getUsuarioInformante().getNombrecompleto().length() > 15) {
                    nombreCompleto = panico.getUsuarioInformante().getNombrecompleto().substring(0, 15);
                } else {
                    nombreCompleto = panico.getUsuarioInformante().getNombrecompleto();
                }
                if (gf.getCodigoAsignado().getDireccion().length() > 21) {
                    direccion = gf.getCodigoAsignado().getDireccion().substring(0, 21);
                } else {
                    direccion = gf.getCodigoAsignado().getDireccion();
                }
                urlServicio = "http://saemcolombia.com.co:8009/send?username=msm23&password=msm1254&to=57"
                        + personaSeguridadXZona.getNumeroTelefono() + "&content=ACIRA: Se recibe el reporte de un evento sospechoso cerca su lugar de residencia por:"
                        + nombreCompleto
                        + ", Reside:" + direccion
                        + ", Hora:" + panico.getHora().toString().substring(0, 5)
                        + ", Preste Atención" + "&from=seamco";
                urlServicio = urlServicio.replaceAll(" ", "%20");
                urlServicio = urlServicio.replaceAll("#", "N.");
                System.out.println("url servicio if " + urlServicio);
            }

            System.out.println("url servicio" + urlServicio);
            URL url = new URL(urlServicio);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println("conexion aqui --------" + connection);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            InformadosEventoSucedido ies = new InformadosEventoSucedido();
            ies.setCodigoEntrega(finalStr.toString());
            ies.setEstado(Boolean.TRUE);
            ies.setEventoSospecha(panico);
            ies.setPersonaSeguridadXZona(personaSeguridadXZona);
            ies.setMedioUtlizado("SMS");
            ies.setMensajeNativo(urlServicio);
            ies.setFechaCreacion(new Date());
            if (guardarEnBaseDatos2(ies)) {
                System.out.println("Guardo en bd - SendSMSS");
            } else {
                System.out.println("no guardo");
            }
        } catch (IOException e) {
            System.out.println("Error: LogicaNotificacionEventos-SendSMSS: " + e.getLocalizedMessage());
        }
    }

    public Boolean guardarEnBaseDatos2(InformadosEventoSucedido ies) {
        try {
            em.persist(ies);
            return Boolean.TRUE;
        } catch (Exception e) {
            System.out.println("Error: LogicaNotificacionEventos-guardarEnBaseDatos2: " + e.getLocalizedMessage());
            return Boolean.FALSE;
        }
    }

    public void NotificationPush() {
        try {
            String chanel = "/observatorio";
            String detalle = "Actualizando ya que se recibe un evento";
            String resumen = "Push";

            EventBus eventBus = EventBusFactory.getDefault().eventBus();
            eventBus.publish(chanel, new FacesMessage(resumen, detalle));
        } catch (Exception e) {
            System.out.println("Error: NotificationPush " + e.getMessage());
        }
    }

    /*public void SendFcmHttpManual(String token, EventoPanico panico, Usuario u) throws Exception {
        System.out.println("--------------------------------------------------------");
        String authKey = "AAAAa7zwMYQ:APA91bEdScvbceh2IV6AEXvG6MM1SKz9YER-Gj-uTYTp5VFLATtatHH8JPg15S42rXEuOMl9ycRmgUwn9cBqlU6TqxeG93pbLoMrVU795bwnUHPrhPGcdzFLsw0sk0QPmjheD-ULYsmd";
        String FCMurl = "https://fcm.googleapis.com/fcm/send";

        URL url = new URL(FCMurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "key=" + authKey);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");

        JSONObject data = new JSONObject();
        data.put("title", "Evento de Panico");
        if (panico.getAnonimo()) {
            if (panico.getMensaje().length() < 5) {
                data.put("message", "Se recibe el reporte de un evento de panico"
                        + " cerca a su lugar de residencia."
                        + "<br>Hora:" + panico.getHora()
                        + "<br>Preste Atencion y tenga cuidado.");
            } else {
                data.put("message", "Se recibe el reporte de un evento de pánico"
                        + " cerca a su lugar de residencia."
                        + "<br>Hora:" + panico.getHora()
                        + "<br>Mensaje: " + panico.getMensaje()
                        + "<br>Preste Atencion y tenga cuidado");
            }
        } else {
            if (panico.getMensaje().length() < 5) {
                data.put("message", "Se recibe el reporte de un evento de panico"
                        + " cerca a su lugar de residencia."
                        + "<br>Por: " + panico.getUsuarioInformante().getNombrecompleto()
                        + "<br>Hora: " + panico.getHora()
                        + "<br>Preste Atencion y tenga cuidado.");
            } else {
                data.put("message", "Se recibe el reporte de un evento de pánico"
                        + " cerca a su lugar de residencia."
                        + "<br>Por: " + panico.getUsuarioInformante().getNombrecompleto()
                        + "<br>Hora: " + panico.getHora()
                        + "<br>Mensaje: " + panico.getMensaje()
                        + "<br>Preste Atencion y tenga cuidado.");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        accion1.setIcon("emailGuests");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("Window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        accion2.setIcon("www/fcmpushicon");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("Window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(panico.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(panico.getHora());
        fcmPanico.setLatitud(panico.getLatitud());
        fcmPanico.setLongitud(panico.getLongitud());
        fcmPanico.setExactitud(panico.getExactitud());
        fcmPanico.setTipo("Panico");
        data.put("panico", gson.toJson(fcmPanico));
        data.put("comandosSecundarios", "{\"estado\":\"\null\"}");

        json.put("data", data);

        System.out.println("json " + json);

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(json.toString());
        wr.flush();

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String str;
        StringBuilder finalStr = new StringBuilder();
        while ((str = in.readLine()) != null) {
            finalStr.append(str);
            System.out.println("leyendo datos");
        }
        in.close();
        System.out.println("result:  " + finalStr.toString());
        resultadoFirebaseFCM result = new Gson().fromJson(finalStr.toString(), resultadoFirebaseFCM.class
        );
        if (result != null) {
            System.out.println("result != null");
            if (result.getSuccess() != 1) {
                System.out.println("fallo al enviar FCM");
                SendSMSPanico(panico, u);
            } else {
                System.out.println("envio FCM");
            }
        }
    }

public void SendFcmHttpManualSospecha(String token, EventoSospecha sospecha, Usuario u) throws Exception {
        System.out.println("--------------------------------------------------------");
        String authKey = "AAAAa7zwMYQ:APA91bEdScvbceh2IV6AEXvG6MM1SKz9YER-Gj-uTYTp5VFLATtatHH8JPg15S42rXEuOMl9ycRmgUwn9cBqlU6TqxeG93pbLoMrVU795bwnUHPrhPGcdzFLsw0sk0QPmjheD-ULYsmd";
        String FCMurl = "https://fcm.googleapis.com/fcm/send";

        URL url = new URL(FCMurl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "key=" + authKey);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("to", token);
        json.put("time_to_live", 0);
        json.put("priority", "high");

        JSONObject data = new JSONObject();
        data.put("title", "Reporte de sospecha");
        if (sospecha.getAnonimo()) {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia."
                        + "<br>Hora: " + sospecha.getHora()
                        + "<br>Preste Atencion y tenga cuidado.");
            } else {
                data.put("message", "Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia."
                        + "<br>Hora: " + sospecha.getHora()
                        + "<br>Mensaje: " + sospecha.getMensaje()
                        + ".<br>Preste Atencion y tenga cuidado.");
            }
        } else {
            if (sospecha.getMensaje().length() < 5) {
                data.put("message", "Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia."
                        + "<br>Por: " + sospecha.getUsuarioInformante().getNombrecompleto()
                        + "<br>Hora:" + sospecha.getHora()
                        + "<br>Preste Atencion y tenga cuidado.");
            } else {
                data.put("message", "Se recibe el reporte de un evento sospechoso"
                        + " cerca a su lugar de residencia."
                        + "<br>Por " + sospecha.getUsuarioInformante().getNombrecompleto()
                        + "<br>Hora: " + sospecha.getHora()
                        + "<br>Mensaje: " + sospecha.getMensaje()
                        + ".<br>Preste Atencion y tenga cuidado.");
            }
        }
        data.put("icon", "notification");
        data.put("content-available", 1);

        List<objetoJsonAccionNoti> accionNotis = new ArrayList<>();
        objetoJsonAccionNoti accion1 = new objetoJsonAccionNoti();
        //accion1.setIcon("emailGuests");
        accion1.setTitle("ignorar");
        accion1.setForeground("false");
        accion1.setCallback("Window.reject");
        accionNotis.add(accion1);

        objetoJsonAccionNoti accion2 = new objetoJsonAccionNoti();
        //accion2.setIcon("www/fcmpushicon");
        accion2.setTitle("ver en mapa");
        accion2.setForeground("true");
        accion2.setCallback("Window.approve");
        accionNotis.add(accion2);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.put("actions", gson.toJson(accionNotis));

        objetoFcm fcmPanico = new objetoFcm();
        fcmPanico.setNombreReporto(sospecha.getUsuarioInformante().getNombrecompleto());
        fcmPanico.setHora(sospecha.getHora());
        fcmPanico.setLatitud(sospecha.getLatitud());
        fcmPanico.setLongitud(sospecha.getLongitud());
        fcmPanico.setExactitud(sospecha.getExactitud());
        fcmPanico.setTipo("Sospecha");
        data.put("panico", gson.toJson(fcmPanico));

        json.put("data", data);

        System.out.println("json " + json);

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(json.toString());
        wr.flush();

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String str;
        StringBuilder finalStr = new StringBuilder();
        while ((str = in.readLine()) != null) {
            finalStr.append(str);
            System.out.println("leyendo datos");
        }
        in.close();
        System.out.println("result:  " + finalStr.toString());
        resultadoFirebaseFCM result = new Gson().fromJson(finalStr.toString(), resultadoFirebaseFCM.class
        );
        if (result != null) {
            System.out.println("result != null");
            if (result.getSuccess() != 1) {
                System.out.println("fallo al enviar FCM");
                SendSMSSospecha(sospecha, u);
            } else {
                System.out.println("envio FCM");
            }
        }
    }*/
}
