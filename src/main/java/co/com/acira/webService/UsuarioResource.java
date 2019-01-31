/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.base.Md5;
import co.com.acira.clases.credencialInicioSesion;
import co.com.acira.clases.nuevoUsuario;
import co.com.acira.clases.objetoUsuario;
import co.com.acira.clases.respuestaSoapUnimagdalena;
import co.com.acira.logica.CommonsBean;
import co.com.acira.logica.LogicaLogToEmail;
import co.com.acira.logica.LogicaUnimagdalena;
import co.com.acira.logica.LogicaUsuario;
import co.com.acira.modelo.Adjunto;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.CodigoSeguridad;
import co.com.acira.modelo.GrupoFamiliar;
import co.com.acira.modelo.Usuario;
import co.com.acira.webService.base.ResponseMessenger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.apache.commons.io.IOUtils;

/**
 * REST Web Service
 *
 * @author wilme
 */
@Path("usuario")
@Stateless
public class UsuarioResource {

    @Context
    private UriInfo context;

    public UsuarioResource() {
    }

    @EJB
    LogicaUsuario logicaUsuario;
    @EJB
    CommonsBean cb;
    @EJB
    LogicaUnimagdalena logicaUnimagdalena;
    @EJB
    LogicaLogToEmail logToEmail;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{usuario}")
    public Response inicioSesion(@PathParam("usuario") String usuario) {
        try {
            System.out.println("--------------------------------");
            System.out.println("metodo inicio sesion");
            Gson gson = new Gson();
            credencialInicioSesion cl = gson.fromJson(usuario, credencialInicioSesion.class);
            Usuario u = logicaUsuario.Login(cl);
            if (u != null) {
                objetoUsuario u1 = new objetoUsuario(u);
                if (u.getGrupoFamiliar() != null) {
                    if (u.getGrupoFamiliar().getEstado()) {
                        Double valorEntero = Math.floor(Math.random() * (100 - 999 + 1) + 999);
                        u.setIdSesion(valorEntero.longValue());
                        u1.setIdSesion(u.getIdSesion());
                        Gson g = new GsonBuilder().setPrettyPrinting().create();
                        System.out.println("usuario: " + g.toJson(u1));
                        return new ResponseMessenger().getResponseOk(g.toJson(u1));
                    } else {
                        System.out.println("Error: inicioSesion: grupo familiar desactivado");
                        return new ResponseMessenger().getResponseError("Su grupo familiar se encuentra desactivado, favor revise el estado de su pago mensual");
                    }
                } else {
                    System.out.println("Error: inicioSesion: usuario sin grupo familiar asignado");
                    return new ResponseMessenger().getResponseError("Problema con su grupo familiar asociado");
                }
            } else {
                System.out.println("Error: inicioSesion: usaurio y/o contrasena incorrecto ");
                return new ResponseMessenger().getResponseError("Usuario y/o Contraseña incorrecto");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: inicioSesion " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        } catch (Exception ex) {
            System.out.println("Error: inicioSesion " + ex.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Usuario y/o Contraseña incorrecto");
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/registrar/{usuario}")
    public Response registrarUsuario(@PathParam("usuario") String usuario) {
        try {
            System.out.println("----------------------------");
            System.out.println("metodo Registrar");
            System.out.println("objeto recibido en json: " + usuario);
            Gson gson = new Gson();
            nuevoUsuario usuario1 = gson.fromJson(usuario, nuevoUsuario.class);
            try {
                System.out.println("usuario1 " + usuario1.getNombrecompleto());
                System.out.println("usuario1 " + usuario1.getNombreusuario());
                System.out.println("usuario1 " + usuario1.getContrasena());
                System.out.println("usuario1 " + usuario1.getEmail());
                System.out.println("usuario1 " + usuario1.getCelular());
                System.out.println("usuario1 " + usuario1.getCodigofamiliar());
                System.out.println("usuario1 " + usuario1.getIdentificacion());
                System.out.println("usuario1 " + usuario1.getUuidDocumento());
                //validacion de usuario
                Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "nombreUsuario", usuario1.getNombreusuario());
                if (usurTemp != null) {
                    System.out.println("Error: registrarUsuario: nombre de usuario ya utilizado");
                    return new ResponseMessenger().getResponseError("El nombre de usuario ya se encuentra siendo utilizado por otro usuario, favor ingrese uno diferente");
                }

                usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
                if (usurTemp != null) {
                    System.out.println("Error: registrarUsuario: identificacion ya utilizado");
                    return new ResponseMessenger().getResponseError("Ya se encuentra registrado un usuario con el mismo número de identificación, favor verifique");
                }

                usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "telefono", usuario1.getCelular());
                if (usurTemp != null) {
                    System.out.println("Error: registrarUsuario: telefono ya utilizado");
                    return new ResponseMessenger().getResponseError("Otro usuario registro el número de celular, favor verifique o cámbielo");
                }
                //cerrando validacion de usuario
                CodigoSeguridad cs = (CodigoSeguridad) cb.getByOneFieldWithOneResult(CodigoSeguridad.class, "codigoGenerado", usuario1.getCodigofamiliar());
                if (cs != null) {
                    System.out.println("codigo de seguridad != null");
                    GrupoFamiliar grupoFamiliar = (GrupoFamiliar) cb.getByOneFieldWithOneResult(GrupoFamiliar.class, "codigoAsignado", cs);
                    if (grupoFamiliar != null) {
                        System.out.println("grupo Familiar != null");
                        if (grupoFamiliar.getUsuarioPrincipal() == null) {
                            System.out.println("grupo sin usuario principal");
                            if (Objects.equals(grupoFamiliar.getCodigoAsignado().getNumeroIdentificacionUsuarioPrincipal(), usuario1.getIdentificacion())) {
                                Usuario u = new Usuario();
                                u.setNombrecompleto(usuario1.getNombrecompleto());
                                u.setNombreUsuario(usuario1.getNombreusuario());
                                u.setNumeroIdentificacion(usuario1.getIdentificacion());
                                u.setEmail(usuario1.getEmail());
                                u.setTelefono(usuario1.getCelular());
                                u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                                u.setGrupoFamiliar(grupoFamiliar);
                                u.setUuidDocumentoIdentidad(usuario1.getUuidDocumento());
                                if (cb.guardar(u)) {
                                    System.out.println("");
                                    Usuario temp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", u.getNumeroIdentificacion());
                                    grupoFamiliar.setUsuarioPrincipal(temp);
                                    if (cb.guardar(grupoFamiliar)) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                logToEmail.Registro(u);
                                            }
                                        }).start();
                                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro exitoso\"}");
                                    }
                                }
                            }
                        }

                        List<Usuario> familiarXusuarioSecundarios = (List<Usuario>) cb.getByOneField(Usuario.class, "grupoFamiliar", grupoFamiliar);
                        System.out.println("numero de integrantes exitentes del grupo");
                        if (familiarXusuarioSecundarios.isEmpty()) {
                            System.out.println("no existen integrantes secundarios");
                            Usuario u = new Usuario();
                            u.setNombrecompleto(usuario1.getNombrecompleto());
                            u.setNombreUsuario(usuario1.getNombreusuario());
                            u.setNumeroIdentificacion(usuario1.getIdentificacion());
                            u.setEmail(usuario1.getEmail());
                            u.setTelefono(usuario1.getCelular());
                            u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                            u.setGrupoFamiliar(grupoFamiliar);
                            u.setUuidDocumentoIdentidad(usuario1.getUuidDocumento());
                            if (cb.guardar(u)) {
                                System.out.println("guardar usuario secundario al grupo familiar existente");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        logToEmail.Registro(u);
                                    }
                                }).start();
                                return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro exitoso\"}");
                            }
                        } else {
                            if (familiarXusuarioSecundarios.size() < grupoFamiliar.getNumeroUsuarios()) {
                                System.out.println("existen usuarios pero aun le queda cupo");
                                Usuario u = new Usuario();
                                u.setNombrecompleto(usuario1.getNombrecompleto());
                                u.setNombreUsuario(usuario1.getNombreusuario());
                                u.setNumeroIdentificacion(usuario1.getIdentificacion());
                                u.setEmail(usuario1.getEmail());
                                u.setTelefono(usuario1.getCelular());
                                u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                                u.setGrupoFamiliar(grupoFamiliar);
                                u.setUuidDocumentoIdentidad(usuario1.getUuidDocumento());
                                if (cb.guardar(u)) {
                                    System.out.println("guardar usuario secundario");
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            logToEmail.Registro(u);
                                        }
                                    }).start();
                                    return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro exitoso\"}");
                                }
                            } else {
                                System.out.println("full cupos");
                                return new ResponseMessenger().getResponseError("El código familiar ya alcanzo el cupo de usuarios establecido, si desea extender el número de usuarios colóquese en contacto con el prestador del servicio.");
                            }
                        }
                    } else {
                        System.out.println("grupo familiar == null");
                        GrupoFamiliar nuevoGrupo = new GrupoFamiliar();
                        nuevoGrupo.setCodigoAsignado(cs);
                        nuevoGrupo.setNumeroUsuarios(new Long(3));
                        if (Objects.equals(cs.getNumeroIdentificacionUsuarioPrincipal(), usuario1.getIdentificacion())) {
                            System.out.println("getNumeroIdentificacionUsuarioPrincipal == usuario1.getIdentificacion()");
                            Usuario u = new Usuario();
                            u.setNombrecompleto(usuario1.getNombrecompleto());
                            u.setNombreUsuario(usuario1.getNombreusuario());
                            u.setNumeroIdentificacion(usuario1.getIdentificacion());
                            u.setEmail(usuario1.getEmail());
                            u.setTelefono(usuario1.getCelular());
                            u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                            u.setUuidDocumentoIdentidad(usuario1.getUuidDocumento());
                            if (cb.guardar(u)) {
                                System.out.println("guardar usuario principal ok");
                                Usuario temp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", u.getNumeroIdentificacion());
                                nuevoGrupo.setUsuarioPrincipal(temp);
                                if (cb.guardar(nuevoGrupo)) {
                                    System.out.println("guardar nuevo grupo con usuario principal ok");
                                    GrupoFamiliar grupoGuardado = (GrupoFamiliar) cb.getByOneFieldWithOneResult(GrupoFamiliar.class, "codigoAsignado", cs);
                                    temp.setGrupoFamiliar(grupoGuardado);
                                    if (cb.guardar(temp)) {
                                        System.out.println("actualizar usuario");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                logToEmail.Registro(u);
                                            }
                                        }).start();
                                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro exitoso, Recuerde que usted es el encargado principal del uso dado a su código asignado\"}");
                                    }
                                } else {
                                    System.out.println("guardar nuevo grupo error");
                                }
                            } else {
                                System.out.println("guardar usuario principal error");
                            }
                        } else {
                            if (cb.guardar(nuevoGrupo)) {
                                System.out.println("guardar nuevo grupo ok sin usuario Principal");
                                GrupoFamiliar gf = (GrupoFamiliar) cb.getByOneFieldWithOneResult(GrupoFamiliar.class, "codigoAsignado", cs);
                                if (gf != null) {
                                    Usuario u = new Usuario();
                                    u.setNombrecompleto(usuario1.getNombrecompleto());
                                    u.setNombreUsuario(usuario1.getNombreusuario());
                                    u.setNumeroIdentificacion(usuario1.getIdentificacion());
                                    u.setEmail(usuario1.getEmail());
                                    u.setTelefono(usuario1.getCelular());
                                    u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                                    u.setUuidDocumentoIdentidad(usuario1.getUuidDocumento());
                                    if (cb.guardar(u)) {
                                        System.out.println(" guardar usuario para agregarlo a grupo familiar ok");
                                        Usuario temp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", u.getNumeroIdentificacion());
                                        temp.setGrupoFamiliar(gf);
                                        System.out.println("guardando usuario secuendario para grupo familiar sin usuario principal asignado");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                logToEmail.Registro(u);
                                            }
                                        }).start();
                                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro exitoso, recuerde que el código familiar ingresado aun no registra el registro del usuario principal\"}");
                                    } else {
                                        System.out.println(" guardar usuario para agregarlo a grupo familiar error");
                                    }
                                }
                            } else {
                                System.out.println("guardar nuevo grupo error sin usuario Principal");
                            }
                        }
                    }
                } else {
                    System.out.println("aqui valiando si es estudiante");
                    respuestaSoapUnimagdalena respuesta = logicaUnimagdalena.ValidarEstudiante(usuario1.getIdentificacion().toString(), usuario1.getCodigofamiliar());
                    if (respuesta != null) {
                        if (respuesta.getSuccess()) {
                            try {
                                Barrio barrio = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "nombre", ConfiguracionGeneral.nombreBarrioMadalena);

                                CodigoSeguridad codigoSeguridad = new CodigoSeguridad();
                                codigoSeguridad.setBarrio(barrio);
                                codigoSeguridad.setCodigoGenerado(usuario1.getCodigofamiliar());
                                codigoSeguridad.setDireccion("Estudiante de la universidad del Magdalena");
                                codigoSeguridad.setNombreUsuarioPrincipal(respuesta.getNombreCompleto());
                                codigoSeguridad.setNumeroIdentificacionUsuarioPrincipal(usuario1.getIdentificacion());
                                codigoSeguridad.setLatitud(11.22627984301015);
                                codigoSeguridad.setLongitud(-74.1883219062216);

                                if (cb.guardar(codigoSeguridad)) {
                                    codigoSeguridad = (CodigoSeguridad) cb.getByOneFieldWithOneResult(CodigoSeguridad.class, "codigoGenerado", usuario1.getCodigofamiliar());
                                    GrupoFamiliar grupoFamiliar = new GrupoFamiliar();
                                    grupoFamiliar.setCodigoAsignado(codigoSeguridad);
                                    grupoFamiliar.setNumeroUsuarios(1L);

                                    if (cb.guardar(grupoFamiliar)) {
                                        grupoFamiliar = (GrupoFamiliar) cb.getByOneFieldWithOneResult(GrupoFamiliar.class, "codigoAsignado", codigoSeguridad);
                                        Usuario usuario2 = new Usuario();
                                        usuario2.setNombrecompleto(usuario1.getNombrecompleto());
                                        usuario2.setNombreUsuario(usuario1.getNombreusuario());
                                        usuario2.setNumeroIdentificacion(usuario1.getIdentificacion());
                                        usuario2.setEmail(usuario1.getEmail());
                                        usuario2.setTelefono(usuario1.getCelular());
                                        usuario2.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                                        usuario2.setGrupoFamiliar(grupoFamiliar);

                                        if (cb.guardar(usuario2)) {
                                            usuario2 = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
                                            grupoFamiliar.setUsuarioPrincipal(usuario2);
                                            Usuario tmp = usuario2;
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    logToEmail.Registro(tmp);
                                                }
                                            }).start();
                                            return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Registro Exitoso, se ha registrado como estudiante de la Universidad del Magdalena \"}");
                                        } else {
                                            System.out.println("Error: registrarUsuario: tratanto de guardar usuario  est. universidad del magdalena");
                                        }
                                    } else {
                                        System.out.println("Error: registrarUsuario: tratanto de guardar grupo familiar est. universidad del magdalena");
                                    }
                                } else {
                                    System.out.println("Error: registrarUsuario: tratanto de guardar codigo de seguridad est. universidad del magdalena");
                                }
                            } catch (Exception e) {
                                System.out.println("Error: registrarUsuario: " + e.getMessage());
                                return new ResponseMessenger().getResponseError("El código familiar ingresado no concuerda, favor verificarlo y vuelva intentar");
                            }
                        } else {
                            System.out.println("Error: registrarUsuario: " + respuesta.getMensaje());
                            return new ResponseMessenger().getResponseError("El código familiar ingresado no concuerda, favor verificarlo y vuelva intentar");
                        }
                    } else {
                        System.out.println("Error: registrarUsuario: grupo familiar null");
                        return new ResponseMessenger().getResponseError("El código familiar ingresado no concuerda, favor verificarlo y vuelva intentar");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error: registrarUsuario: " + ex.getLocalizedMessage());
                return new ResponseMessenger().getResponseError("Problema interno del servidor");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: registrarUsuario: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json;charset=UTF-8")
    @Path("/actualizar")
    public Response actualizarUsuario(
            final MultivaluedMap<String, String> formParams) {
        //@FormParam("data") String data) {
        try {
            System.out.println("-----------------------------");
            System.out.println("metodo actualizarUsuario");
            String infRecibida = null;
            for (String temp : formParams.keySet()) {
                System.out.println("objeto recibido en json: " + temp);
                infRecibida = temp;
            }
            //System.out.println("objeto recibido en json: " + data);
            Gson gson = new Gson();
            nuevoUsuario usuario1 = gson.fromJson(infRecibida, nuevoUsuario.class);
            try {
                Boolean actualizar = Boolean.FALSE;
                Usuario u = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", usuario1.getIdentificacion());
                if (u != null) {
                    if (!u.getNombrecompleto().equals(usuario1.getNombrecompleto())) {
                        u.setNombrecompleto(usuario1.getNombrecompleto());
                        actualizar = Boolean.TRUE;
                    }

                    if (!Objects.equals(u.getTelefono(), usuario1.getCelular())) {
                        Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "telefono", usuario1.getCelular());
                        if (usurTemp != null) {
                            System.out.println("Error: actualizarUsuario: numero de celular ya utilizado");
                            return new ResponseMessenger().getResponseError("Otro usuario registro el número de celular, favor verifique o cámbielo");
                        } else {
                            u.setTelefono(usuario1.getCelular());
                            actualizar = Boolean.TRUE;
                        }
                    }

                    if (!u.getEmail().equals(usuario1.getEmail())) {
                        u.setEmail(usuario1.getEmail());
                        actualizar = Boolean.TRUE;
                    }

                    if (!u.getNombreUsuario().equals(usuario1.getNombreusuario())) {
                        Usuario usurTemp = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "nombreUsuario", usuario1.getNombreusuario());
                        if (usurTemp != null) {
                            System.out.println("Error: actualizarUsuario: nombre de usuario ya utilizado");
                            return new ResponseMessenger().getResponseError("El nombre de usuario ya se encuentra siendo utilizado por otro usuario, favor ingrese uno diferente");
                        } else {
                            u.setNombreUsuario(usuario1.getNombreusuario());
                            actualizar = Boolean.TRUE;
                        }
                    }

                    if (usuario1.getContrasena() != null && usuario1.getContrasena().length() > 5) {
                        if (!u.getContrasena().equals(Md5.getEncoddedString(usuario1.getContrasena()))) {
                            u.setContrasena(Md5.getEncoddedString(usuario1.getContrasena()));
                            actualizar = Boolean.TRUE;
                        }
                    }
                }

                if (actualizar) {
                    if (cb.guardar(u)) {
                        System.out.println("Actualización exitosa");
                        return new ResponseMessenger().getResponseOk("{\"mensaje\":\"Actualización exitosa\"}");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error: actualizarUsuario: " + ex.getLocalizedMessage());
                return new ResponseMessenger().getResponseError("Problema interno del servidor");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error: actualizarUsuario: " + e.getLocalizedMessage());
            return new ResponseMessenger().getResponseError("Problema interno del servidor");
        }
        return null;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json;charset=UTF-8")
    @Path("/subirdocumento")
    public Response subirDocumento(MultipartFormDataInput input) {
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
                String fileName = ConfiguracionGeneral.RUTA + "documentos" + File.separator + uuid + ".jpg";
                writeFile(bytes, fileName);
                Adjunto adjunto = new Adjunto();
                adjunto.setTipo("documento identidad");
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
