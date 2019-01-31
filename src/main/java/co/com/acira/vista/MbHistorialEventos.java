/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbHistorialEventos")
public class MbHistorialEventos implements Serializable {

    @EJB
    private CommonsBean cb;
    private List<SelectItem> tipoEventos;
    private Long idTipoEvento;
    private List<EventoPanico> allPanicos;
    private List<EventoSospecha> allSospechas;
    private List<EventoPanico> allPanicosFiltred;
    private List<EventoSospecha> allSospechasFiltred;

    private Boolean popupVerMapa;
    private MapModel advancedModel;
    private Marker marker;
    private Boolean popupVerUsuarioReporte;
    private LatLng centroMap;
    private Usuario u;
    private List<Usuario> usuarios;

    public MbHistorialEventos() {
    }

    @PostConstruct
    public void init() {
        tipoEventos = new LinkedList();
        tipoEventos.add(new SelectItem(0, "Pánico"));
        tipoEventos.add(new SelectItem(1, "Sospecha"));
        allPanicos = new ArrayList<>();
        allSospechas = new ArrayList<>();
        popupVerMapa = Boolean.FALSE;
        popupVerUsuarioReporte = Boolean.FALSE;
        advancedModel = new DefaultMapModel();
        centroMap = new LatLng(11.215008, -74.201816);
        usuarios = new ArrayList<>();
    }

    public void listenerTipoEvento() {
        System.out.println("aqui en listenerTipoEvento");
        if (idTipoEvento != null) {
            if (idTipoEvento == 0) {
                allPanicos.clear();
                allSospechas.clear();
                allPanicos = cb.getAll(EventoPanico.class, "ORDER BY o.fechaCreacion DESC");
                System.out.println("size " + allPanicos.size());
            } else if (idTipoEvento == 1) {
                allPanicos.clear();
                allSospechas.clear();
                allSospechas = cb.getAll(EventoSospecha.class, "ORDER BY o.fechaCreacion DESC");
                System.out.println("size " + allSospechas.size());
            }
        }
    }

    public void popupVerMapaMetodo(Double latitud, Double longitud) {
        advancedModel = new DefaultMapModel();
        popupVerMapa = Boolean.TRUE;
        popupVerUsuarioReporte = Boolean.FALSE;
        marker = new Marker(new LatLng(latitud, longitud));
        marker.setTitle("lugar del reporte");
        centroMap = new LatLng(latitud, longitud);
        advancedModel.addOverlay(marker);
    }

    public void popupVerUsuario(Usuario usuario) {
        this.u = null;
        this.u = usuario;
        popupVerUsuarioReporte = Boolean.TRUE;
        popupVerMapa = Boolean.FALSE;
    }

    public Boolean usuariosDelGrupo() {
        try {
            usuarios.clear();
            usuarios = cb.getByOneField(Usuario.class, "grupoFamiliar", u.getGrupoFamiliar());
            System.out.println("usuarios " + usuarios.size());
            Usuario temp = null;
            for (Usuario usuariotemp : usuarios) {
                if (Objects.equals(usuariotemp.getId(), u.getId())) {
                    temp = usuariotemp;
                }
            }
            System.out.println("eliminar usuario actual: " + usuarios.remove(temp));
            System.out.println("usuarios " + usuarios.size());
            if (usuarios.size() > 1) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    public List<SelectItem> getTipoEventos() {
        return tipoEventos;
    }

    public void setTipoEventos(List<SelectItem> tipoEventos) {
        this.tipoEventos = tipoEventos;
    }

    public Long getIdTipoEvento() {
        return idTipoEvento;
    }

    public void setIdTipoEvento(Long idTipoEvento) {
        this.idTipoEvento = idTipoEvento;
    }

    public List<EventoPanico> getAllPanicos() {
        return allPanicos;
    }

    public void setAllPanicos(List<EventoPanico> allPanicos) {
        this.allPanicos = allPanicos;
    }

    public List<EventoSospecha> getAllSospechas() {
        return allSospechas;
    }

    public void setAllSospechas(List<EventoSospecha> allSospechas) {
        this.allSospechas = allSospechas;
    }

    public List<EventoPanico> getAllPanicosFiltred() {
        return allPanicosFiltred;
    }

    public void setAllPanicosFiltred(List<EventoPanico> allPanicosFiltred) {
        this.allPanicosFiltred = allPanicosFiltred;
    }

    public List<EventoSospecha> getAllSospechasFiltred() {
        return allSospechasFiltred;
    }

    public void setAllSospechasFiltred(List<EventoSospecha> allSospechasFiltred) {
        this.allSospechasFiltred = allSospechasFiltred;
    }

    public Boolean getPopupVerMapa() {
        return popupVerMapa;
    }

    public void setPopupVerMapa(Boolean popupVerMapa) {
        this.popupVerMapa = popupVerMapa;
    }

    public MapModel getAdvancedModel() {
        return advancedModel;
    }

    public void setAdvancedModel(MapModel advancedModel) {
        this.advancedModel = advancedModel;
    }

    public Boolean getPopupVerUsuarioReporte() {
        System.out.println("contador");
        return popupVerUsuarioReporte;
    }

    public void setPopupVerUsuarioReporte(Boolean popupVerUsuarioReporte) {
        this.popupVerUsuarioReporte = popupVerUsuarioReporte;
    }

    public LatLng getCentroMap() {
        return centroMap;
    }

    public void setCentroMap(LatLng centroMap) {
        this.centroMap = centroMap;
    }

    public Usuario getU() {
        return u;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

}
