/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.PersonaSeguridadXZona;
import java.io.Serializable;
import java.util.ArrayList;
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
@ManagedBean(name = "MbSeguridadXBarrio")
public class MbSeguridadXBarrio implements Serializable {

    @EJB
    private CommonsBean cb;
    private List<SelectItem> listBarriosActivos;
    private Long idBarrio;
    private PersonaSeguridadXZona personaSeguridadXZona;
    private List<PersonaSeguridadXZona> personaSeguridadXZonas;
    private Barrio barrioActivo;

    public MbSeguridadXBarrio() {
    }

    @PostConstruct
    public void init() {
        personaSeguridadXZona = new PersonaSeguridadXZona();
        personaSeguridadXZonas = new ArrayList<>();
        listBarriosActivos = new LinkedList<>();
        try {
            List<Barrio> temp = (List<Barrio>) cb.getByOneField(Barrio.class, "estado", Boolean.TRUE);
            System.out.println("barrios activos " + temp.size());
            temp.forEach((c) -> {
                listBarriosActivos.add(new SelectItem(c.getId(), c.getNombre()));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void accionGuardar() {
        if (VerificarFormulario()) {
            personaSeguridadXZona.setBarrio(barrioActivo);
            if (cb.guardar(personaSeguridadXZona)) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Exitoso", "Se ha guardado");
                init();
                idBarrio = null;
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_FATAL, "Error", "Ha fallado al guardar el barrio");
            }
        }
    }

    public Boolean VerificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (barrioActivo == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Selecione un barrio de la lista");
        }

        if (personaSeguridadXZona.getNumeroTelefono() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue numero de telefono");
        } else {
            try {
                PersonaSeguridadXZona u = (PersonaSeguridadXZona) cb.getByOneFieldWithOneResult(PersonaSeguridadXZona.class, "numeroTelefono", personaSeguridadXZona.getNumeroTelefono());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "numero de telefono ya existe en la lista, favor ingrese otro");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
            }
        }

        if (personaSeguridadXZona.getNombre().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue nombre de usuario");
        }
        return resultado;
    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    public void listenerBarrio() {
        System.out.println("aqui en ListenerBarrio");
        barrioActivo = null;
        if (idBarrio != null) {
            barrioActivo = (Barrio) cb.getById(Barrio.class, idBarrio);
            if (barrioActivo != null) {
                personaSeguridadXZonas = cb.getByOneField(PersonaSeguridadXZona.class, "barrio", barrioActivo);
            }
        }
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

    public PersonaSeguridadXZona getPersonaSeguridadXZona() {
        return personaSeguridadXZona;
    }

    public void setPersonaSeguridadXZona(PersonaSeguridadXZona personaSeguridadXZona) {
        this.personaSeguridadXZona = personaSeguridadXZona;
    }

    public List<PersonaSeguridadXZona> getPersonaSeguridadXZonas() {
        return personaSeguridadXZonas;
    }

    public void setPersonaSeguridadXZonas(List<PersonaSeguridadXZona> personaSeguridadXZonas) {
        this.personaSeguridadXZonas = personaSeguridadXZonas;
    }

}
