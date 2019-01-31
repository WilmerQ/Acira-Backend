/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.CodigoSeguridad;
import co.com.acira.modelo.Usuario;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbCodigoSeguridad")
public class MbCodigoSeguridad implements Serializable {

    @EJB
    private CommonsBean cb;
    private CodigoSeguridad codigoSeguridad;
    private List<CodigoSeguridad> codigoSeguridads;
    private List<CodigoSeguridad> codigoSeguridadsFilter;
    private List<SelectItem> listBarriosActivos;
    private Long idBarrio;
    private String codigoString;

    public MbCodigoSeguridad() {
    }

    @PostConstruct
    public void init() {
        idBarrio = null;
        codigoSeguridad = new CodigoSeguridad();
        listBarriosActivos = new LinkedList<>();
        codigoSeguridads = cb.getAll(CodigoSeguridad.class);
        try {
            System.out.println("barrios activos " + cb.getByOneField(Barrio.class, "estado", Boolean.TRUE).size());
            for (Barrio c : (List<Barrio>) cb.getByOneField(Barrio.class, "estado", Boolean.TRUE)) {
                listBarriosActivos.add(new SelectItem(c.getId(), c.getNombre()));
            }
        } catch (Exception e) {
        }
    }

    public void accionGuardarCodigo() {
        System.out.println("lat en mb: " + codigoSeguridad.getLatitud());
        System.out.println("lng en mb: " + codigoSeguridad.getLongitud());
        if (VerificarFormulario()) {
            String s = codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal().toString();
            s = s.substring(s.length() - 2, s.length());
            Double valorEntero = Math.floor(Math.random() * (100 - 999 + 1) + 999);
            codigoString = "" + codigoSeguridad.getBarrio().getCiudad().getCodigo()
                    + "" + codigoSeguridad.getBarrio().getCodigo()
                    + "" + s
                    + "" + valorEntero.longValue();
            System.out.println("cogigo string " + codigoString);
            codigoSeguridad.setCodigoGenerado(codigoString);
            if (cb.guardar(codigoSeguridad)) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Exitoso", "Se ha generado el codigo ");
                init();
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_FATAL, "Error", "Ha fallado al guardar el barrio");
            }
        }
    }

    public void accionCargarCodigo(CodigoSeguridad b) {
        this.codigoSeguridad = b;
        idBarrio = b.getBarrio().getId();
        codigoString = b.getCodigoGenerado();
    }

    public void accionResetCodigo() {
        this.codigoSeguridad = new CodigoSeguridad();
        idBarrio = null;
        codigoString = null;
    }

    public Boolean VerificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (codigoSeguridad.getNombreUsuarioPrincipal().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue nombre del usuario");
        }

        if (codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue identificacion del usuario");
        } else {
            try {
                Usuario u = (Usuario) cb.getByOneFieldWithOneResult(Usuario.class, "numeroIdentificacion", codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "usuario ya existe en el sistema");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
            }
        }

        if (codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal() != null) {
            try {
                CodigoSeguridad cs = (CodigoSeguridad) cb.getByOneFieldWithOneResult(CodigoSeguridad.class, "numeroIdentificacionUsuarioPrincipal", codigoSeguridad.getNumeroIdentificacionUsuarioPrincipal());
                if (cs != null) {

                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "ya se genero un codigo de seguridad para este usuario");
                }
            } catch (Exception ex) {
            }
        }

        if (idBarrio != null) {
            try {
                Barrio u = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "id", idBarrio);
                codigoSeguridad.setBarrio(u);
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
            }
        } else {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Selecione un barrio de la lista");
        }

        if (codigoSeguridad.getDireccion().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue direccion del usuario");
        }
        return resultado;
    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    public CodigoSeguridad getCodigoSeguridad() {
        return codigoSeguridad;
    }

    public void setCodigoSeguridad(CodigoSeguridad codigoSeguridad) {
        this.codigoSeguridad = codigoSeguridad;
    }

    public List<SelectItem> getListBarriosActivos() {
        return listBarriosActivos;
    }

    public void setListBarriosActivos(List<SelectItem> listBarriosActivos) {
        this.listBarriosActivos = listBarriosActivos;
    }

    public Long getIdBarrio() {
        return idBarrio;
    }

    public void setIdBarrio(Long idBarrio) {
        this.idBarrio = idBarrio;
    }

    public String getCodigoString() {
        return codigoString;
    }

    public void setCodigoString(String codigoString) {
        this.codigoString = codigoString;
    }

    public List<CodigoSeguridad> getCodigoSeguridads() {
        return codigoSeguridads;
    }

    public void setCodigoSeguridads(List<CodigoSeguridad> codigoSeguridads) {
        this.codigoSeguridads = codigoSeguridads;
    }

}
