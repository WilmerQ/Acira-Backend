/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.clases.barrio;
import co.com.acira.clases.ciudad;
import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.Ciudad;
import co.com.acira.modelo.Usuario;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("validaciones")
public class ValidacionesResource {

    @Context
    private UriInfo context;

    @EJB
    CommonsBean cb;

    public ValidacionesResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{parametro}/{dato}")
    public Response validarCampos(@PathParam("dato") String dato, @PathParam("parametro") String parametro) {
        try {
            System.out.println("-------------------------------------------");
            System.out.println("metodo validarCampos");
            System.out.println("objeto recibido en json: " + parametro);
            System.out.println("objeto recibido en json: " + dato);
            Gson gson = new Gson();
            String parametro1 = gson.fromJson(parametro, String.class);
            String dato1 = gson.fromJson(dato, String.class);

            if (!parametro1.isEmpty() && !dato.isEmpty()) {
                if (parametro1.equals("nombreUsuario")) {
                    Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "nombreUsuario", dato1);
                    if (usurTemp != null) {
                        System.out.println("Error: ValidacionesResource-validarCampos: nombre de usuario ya utilizado");
                        return new ResponseMessenger().getResponseError("existe");
                    } else {
                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"no existe\"}");
                    }
                }

                if (parametro1.equals("numeroIdentificacion")) {
                    Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", new Long(dato1));
                    if (usurTemp != null) {
                        System.out.println("Error: ValidacionesResource-validarCampos: numero de identificacion ya existe");
                        return new ResponseMessenger().getResponseError("existe");
                    } else {
                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"no existe\"}");
                    }
                }

                if (parametro1.equals("telefono")) {
                    Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "telefono", new Long(dato1));
                    if (usurTemp != null) {
                        System.out.println("rror: ValidacionesResource-validarCampos: telefono ya existe");
                        return new ResponseMessenger().getResponseError("existe");
                    } else {
                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"no existe\"}");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: ValidacionesResource-validarCampos: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ciudades")
    public Response ciudadesActivas() {
        try {
            System.out.println("-------------------------------------------");
            System.out.println("metodo ciudadesActivas");

            Gson gson = new Gson();

            List<Ciudad> ciudads = cb.getAll(Ciudad.class);
            List<ciudad> temp = new ArrayList<>();
            ciudads.forEach((c) -> {
                ciudad ciudadToObjeto = new ciudad();
                ciudadToObjeto.setCodigo(c.getId());
                ciudadToObjeto.setNombre(c.getNombre());
                temp.add(ciudadToObjeto);
            });
            return new ResponseMessenger().getResponseOk(gson.toJson(temp));
        } catch (Exception e) {
            System.out.println("Error: ValidacionesResource-validarCampos: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/barrios/{codigociudad}")
    public Response barriosActivos(@PathParam("codigociudad") String codigociudad) {
        try {
            System.out.println("-------------------------------------------");
            System.out.println("metodo barriosActivos");
            System.out.println("parametro recibido " + codigociudad);

            Gson gson = new Gson();
            String parametro1 = gson.fromJson(codigociudad, String.class);
            Ciudad ciudad = (Ciudad) cb.getByOneFieldWithOneResult(Ciudad.class, "nombre", parametro1);
            if (ciudad != null) {
                List<Barrio> barrios = cb.getByOneField(Barrio.class, "ciudad", ciudad);
                List<barrio> temp = new ArrayList<>();
                barrios.forEach((b) -> {
                    if (b.getEstado()) {
                        barrio objeto = new barrio();
                        objeto.setCodigo(b.getCodigo());
                        objeto.setNombre(b.getNombre());
                        temp.add(objeto);
                    }
                });
                return new ResponseMessenger().getResponseOk(gson.toJson(temp));
            } else {
                System.out.println("ciudad invalida");
                return new ResponseMessenger().getResponseError("ciudad invalida");
            }
        } catch (Exception e) {
            System.out.println("Error: ValidacionesResource-validarCampos: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }

    }
}
