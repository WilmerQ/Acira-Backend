/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.Ciudad;
import co.com.acira.modelo.CodigoSeguridad;
import co.com.acira.modelo.Usuario;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("CodigoFamiliar")
public class CodigoFamiliarResource {

    @Context
    private UriInfo context;

    @EJB
    CommonsBean cb;

    /**
     * Creates a new instance of CodigoFamiliarResource
     */
    public CodigoFamiliarResource() {
    }

    /**
     * Retrieves representation of an instance of
     * co.com.acira.webService.CodigoFamiliarResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String getXml() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of CodigoFamiliarResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public void putXml(String content) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{documento}/{nombre}/{ciudad}/{barrio}/{direccion}")
    public Response crearCodigoSeguridad(
            @PathParam("documento") Long documento,
            @PathParam("nombre") String nombre,
            @PathParam("ciudad") String ciudad,
            @PathParam("barrio") String barrio,
            @PathParam("direccion") String direccion) {
        try {
            System.out.println("-------------------------------------------");
            System.out.println("metodo validarCampos");
            System.out.println("objeto recibido en json: " + documento);
            System.out.println("objeto recibido en json: " + nombre);
            System.out.println("objeto recibido en json: " + ciudad);
            System.out.println("objeto recibido en json: " + barrio);
            System.out.println("objeto recibido en json: " + direccion);
            Gson gson = new Gson();
            String nombre1 = gson.fromJson(nombre, String.class);
            String ciudad1 = gson.fromJson(ciudad, String.class);
            String barrio1 = gson.fromJson(barrio, String.class);
            String direccion1 = gson.fromJson(direccion, String.class);

            String codigoString;

            Ciudad c = (Ciudad) cb.getByOneFieldWithOneResult(Ciudad.class, "nombre", ciudad1);
            if (c != null) {
                Barrio b = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "nombre", barrio1);
                if (b != null) {
                    CodigoSeguridad codigoSeguridad = new CodigoSeguridad();
                    codigoSeguridad.setBarrio(b);
                    codigoSeguridad.setDireccion(direccion1);
                    codigoSeguridad.setNombreUsuarioPrincipal(nombre1);
                    codigoSeguridad.setNumeroIdentificacionUsuarioPrincipal(documento);

                    String s = codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal().toString();
                    s = s.substring(s.length() - 2, s.length());
                    Double valorEntero = Math.floor(Math.random() * (100 - 999 + 1) + 999);
                    codigoString = "" + codigoSeguridad.getBarrio().getCiudad().getCodigo()
                            + "" + codigoSeguridad.getBarrio().getCodigo()
                            + "" + s
                            + "" + valorEntero.longValue();
                    System.out.println("cogigo string " + codigoString);
                    codigoSeguridad.setLatitud(0.0);
                    codigoSeguridad.setLongitud(0.0);
                    codigoSeguridad.setCodigoGenerado(codigoString);
                    if (cb.guardar(codigoSeguridad)) {
                        return new ResponseMessenger().getResponseOk("{\"codigo\":" + codigoString + "}");
                    } else {
                        return new ResponseMessenger().getResponseError("no se pudo crear");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: ValidacionesResource-validarCampos: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }
}
