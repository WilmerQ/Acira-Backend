/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.base.Md5;
import co.com.acira.base.SessionOperations;
import co.com.acira.logica.CommonsBean;
import co.com.acira.logica.LogicaLogToEmail;
import co.com.acira.logica.LogicaUsuario;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.Usuario;
import io.sentry.Sentry;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author wilme
 */
@SessionScoped
@ManagedBean(name = "MbUsuario")
public class MbUsuario implements Serializable {

    private Usuario usuario;
    private Boolean autenticado;
    private Boolean isusuario;
    private Boolean isadmin;
    private String mensaje;

    //Loguin
    private String nombreDeUsuaio;
    private String password;

    @EJB
    LogicaUsuario logicaUsuario;
    @EJB
    LogicaLogToEmail logToEmail;
    @EJB
    CommonsBean cb;

    public MbUsuario() {
    }

    @PostConstruct
    public void init() {
        mensaje = "";
        usuario = (Usuario) SessionOperations.getSessionValue("USUARIO");
        if (usuario == null) {
            usuario = new Usuario();
            autenticado = Boolean.FALSE;
            isusuario = Boolean.FALSE;
            isadmin = Boolean.FALSE;
            SessionOperations.setSessionValue("USER", Boolean.FALSE);
        } else {
            autenticado = Boolean.TRUE;
            isusuario = Boolean.TRUE;
        }
    }

    public String accionLogin() {
        if (verificarFormulario()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
            autenticado = false;
            isusuario = false;
            isadmin = false;
            Usuario u = logicaUsuario.LoginWeb(nombreDeUsuaio, Md5.getEncoddedString(password));
            SessionOperations.setSessionValue("USER", Boolean.FALSE);
            if (u != null) {
                mensaje = "";
                String url;
                usuario = u;
                autenticado = true;
                if (u.getNombreUsuario().equals("Administrador")) {
                    isadmin = true;
                    usuario.setNombreUsuario("Administrador");
                    SessionOperations.setSessionValue("ADMIN", Boolean.TRUE);
                    SessionOperations.setSessionValue("USER", Boolean.FALSE);
                    System.out.println("logueo admin");
                    url = "admin/gestionCodigoSeg.xhtml";
                    //para probar mails
                    //EventoPanico panico = (EventoPanico) cb.getById(EventoPanico.class, 300l);
                    // logToEmail.MailToPanico(panico, null);
                    
                    Sentry.capture("This is a from mb usuario");
                } else {
                    isusuario = true;
                    SessionOperations.setSessionValue("USER", Boolean.TRUE);
                    SessionOperations.setSessionValue("ADMIN", Boolean.FALSE);
                    System.out.println("logueo user");
                    url = "usuario/gestionEventos.xhtml";
                }
                SessionOperations.setSessionValue("USUARIO", usuario);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, u.getNombreUsuario(), "Bienvenido"));
                redirect(url);
                init();
            } else {
                mensaje = "Verifique sus Credenciales";
            }
        }
        return null;
    }

    public String accionLogout() {
        init();
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        try {
            SessionOperations.setSessionValue("USER", Boolean.FALSE);
            SessionOperations.setSessionValue("ADMIN", Boolean.FALSE);
            context.getExternalContext().invalidateSession();
        } catch (Exception e) {

        }
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Salida", "Se ha cerrado la sesion correctamente"));
        String patch = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getContextPath();
        redirect(patch);
        return null;
    }

    public Boolean verificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (nombreDeUsuaio.length() == 0) {
            //System.out.println("nombreDeUsuaio.length() " + nombreDeUsuaio.length());
            resultado = Boolean.FALSE;
            mensaje = "Error: Inserte Nombre de usuario";
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Inserte Nombre de usuario");
        }

        if (password.trim().length() == 0) {
            resultado = Boolean.FALSE;
            mensaje = "Error: Inserte su contraseña ";
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Inserte su  Contraseña");
        } else {
            String[] campos = password.split(" ");
            if (campos.length > 1) {
                resultado = Boolean.FALSE;
                mensaje = "Error: la Contraseña no permite espacios";
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "la Contraseña no permite espacios");
            }
        }
        return resultado;
    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    private void redirect(String url) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            context.getExternalContext().redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(MbUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNombreDeUsuaio() {
        return nombreDeUsuaio;
    }

    public void setNombreDeUsuaio(String nombreDeUsuaio) {
        this.nombreDeUsuaio = nombreDeUsuaio;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
