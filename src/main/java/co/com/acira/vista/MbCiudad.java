/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Ciudad;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbCiudad")
public class MbCiudad implements Serializable {

    private Ciudad ciudad;
    private List<Ciudad> ciudads;
    @EJB
    private CommonsBean cb;

    public MbCiudad() {
    }

    @PostConstruct
    public void init() {
        ciudad = new Ciudad();
        ciudads = new ArrayList<>();
        ciudads = cb.getAll(Ciudad.class);
    }

    public void accionGuardarCiudad() {
        if (VerificarFormulario()) {
            if (cb.guardar(ciudad)) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Exitoso", "Se ha guardado la ciudad");
                init();

            } else {
                mostrarMensaje(FacesMessage.SEVERITY_FATAL, "Error", "Ha fallado al guardar la ciudad");

            }
        }
    }

    public void accionResetCiudad() {
        this.ciudad = new Ciudad();
    }

    public Boolean VerificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (ciudad.getNombre().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue nombre de la ciudad");
        } else {
            try {
                Ciudad u = (Ciudad) cb.getByOneFieldWithOneResult(Ciudad.class, "nombre", ciudad.getNombre());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Nombre de usuario ya existe");
                }
            } catch (Exception ex) {
                Logger.getLogger(MbCiudad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (ciudad.getCodigo() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue codigo de ciudad");
        } else {
            try {
                Ciudad u = (Ciudad) cb.getByOneFieldWithOneResult(Ciudad.class, "codigo", ciudad.getCodigo());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "codigo de ciudad ya existe");
                }
            } catch (Exception ex) {
                Logger.getLogger(MbCiudad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultado;
    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    public void accionCargarCiudad(Ciudad c) {
        this.ciudad = c;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public List<Ciudad> getCiudads() {
        return ciudads;
    }

    public void setCiudads(List<Ciudad> ciudads) {
        this.ciudads = ciudads;
    }

}
