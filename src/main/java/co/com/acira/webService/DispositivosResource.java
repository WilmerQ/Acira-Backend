/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.clases.nuevoUsuario;
import co.com.acira.logica.CommonsBean;
import co.com.acira.logica.LogicaDispositivos;
import co.com.acira.modelo.Usuario;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("dispositivos")
public class DispositivosResource {

    @Context
    private UriInfo context;

    @EJB
    private CommonsBean cb;
    @EJB
    private LogicaDispositivos ld;

    public DispositivosResource() {
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/registrardispositivo/{usuario}/{token}/{idDispositivo}")
    public Response registrarDispositivo(@PathParam("usuario") String usuario,
            @PathParam("token") String token,
            @PathParam("idDispositivo") String idDispositivo) {
        try {
            System.out.println("-------------------------------------------");
            System.out.println("DispositivosResource registrarDispositivo");
            System.out.println("parametros recibidos");
            System.out.println(usuario);
            System.out.println(token);
            System.out.println(idDispositivo);

            Gson gson = new Gson();
            nuevoUsuario usuario1 = gson.fromJson(usuario, nuevoUsuario.class);
            token = gson.fromJson(token, String.class);
            idDispositivo = gson.fromJson(idDispositivo, String.class);
            Integer idDispo = Integer.parseInt(idDispositivo);
            Usuario u = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
            if (u != null && token.length() > 5 && idDispo != null) {
                String tokenAux = u.getTokenFCM();
                if (u.getTokenFCM() == null) {
                    System.out.println("el usuario no tiene dispositivo resgistrado");
                    u.setTokenFCM(token);
                    u.setIdDispositivo(idDispo.longValue());
                    if (cb.guardar(u)) {
                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Se ha registrado el dispositivo para recibir las notificaciones\"}");
                    } else {
                        System.out.println("Error: registrarDispositivo: no se ha agregado el dispositivo al usuario");
                        return new ResponseMessenger().getResponseError("no se ha guardado");
                    }
                } else {
                    System.out.println("ya existe el usuario con dispositivo resgistrado");
                    if ((u.getIdSesion().equals(usuario1.getIdSesion())) && (u.getIdDispositivo().equals(idDispo.longValue()))) {
                        if (u.getTokenFCM().equals(token)) {
                            System.out.println("Error: registrarDispositivo: ya existe el dispositivo");
                            return new ResponseMessenger().getResponseError("ya existe el dispositivo");
                        } else {
                            System.out.println("token diferente");
                            u.setTokenFCM(token);
                            if (cb.guardar(u)) {
                                System.out.println("se actualizo token");
                                return new ResponseMessenger().getResponseOk("{\"mensaje\":\"se actualizo token\"}");
                            }
                        }
                    } else {
                        if (usuario1.getIdSesion().equals(u.getIdSesion())) {
                            u.setTokenFCM(token);
                            u.setIdDispositivo(idDispo.longValue());
                            new Thread(new Runnable() {
                                private String token = tokenAux;

                                @Override
                                public void run() {
                                    ld.SendFcmManual(token);
                                }
                            }).start();
                            if (cb.guardar(u)) {
                                System.out.println("se actualizo token");
                                return new ResponseMessenger().getResponseOk("{\"mensaje\":\"se actualizo token\"}");
                            }
                        } else {
                            System.out.println("cierraSesion");
                            return new ResponseMessenger().getResponseError("cierraSesion");
                        }
                    }
                }
            } else {
                System.out.println("Error: registrarDispositivo: token y/o usuario y/o id Dispositivo son null");
                return new ResponseMessenger().getResponseError("Se ha detectado un problema, por favor reinicie la aplicación");
            }
        } catch (Exception e) {
            System.out.println("Error: registrarDispositivo: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }

}
