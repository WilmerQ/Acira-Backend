/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.base.SessionOperations;
import co.com.acira.clases.unionPanicoAndSospecha;
import co.com.acira.logica.LogicaObservatorio;
import co.com.acira.modelo.Ciudad;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbObservatorio")
public class MbObservatorio implements Serializable {

    private MapModel advancedModel;
    private List<unionPanicoAndSospecha> panicoAndSospechas;
    private Usuario usuario;
    private List<EventoPanico> panicos;
    private List<EventoSospecha> sospechas;
    private List<Marker> markers;
    private unionPanicoAndSospecha toDialog;
    @EJB
    private LogicaObservatorio lo;
    private Double latitud;
    private Double longitud;

    private ConfiguracionGeneral general;

    public MbObservatorio() {
    }

    @PostConstruct
    public void init() {
        general = new ConfiguracionGeneral();

        toDialog = null;
        markers = new ArrayList<>();
        panicoAndSospechas = new ArrayList<>();
        usuario = (Usuario) SessionOperations.getSessionValue("USUARIO");
        if (usuario != null) {
            Ciudad c = usuario.getGrupoFamiliar().getCodigoAsignado().getBarrio().getCiudad();
            latitud = c.getCentroLatitud();
            longitud = c.getCentroLongitud();
            advancedModel = new DefaultMapModel();
            panicos = lo.panicosXciudad(c);
            sospechas = lo.sospechasXciudad(c);

            for (EventoPanico ep : panicos) {
                unionPanicoAndSospecha pas = new unionPanicoAndSospecha();
                pas.setEventoPanico(ep);
                pas.setFechacreacion(ep.getFechaCreacion());
                pas.setTipoEvento("panico");
                panicoAndSospechas.add(pas);
            }

            for (EventoSospecha ep : sospechas) {
                unionPanicoAndSospecha pas = new unionPanicoAndSospecha();
                pas.setEventoSospecha(ep);
                pas.setFechacreacion(ep.getFechaCreacion());
                pas.setTipoEvento("sospecha");
                panicoAndSospechas.add(pas);
            }

            Collections.sort(panicoAndSospechas, new Comparator<unionPanicoAndSospecha>() {
                @Override
                public int compare(unionPanicoAndSospecha o1, unionPanicoAndSospecha o2) {
                    if (o1.getFechacreacion() == null || o2.getFechacreacion() == null) {
                        return 0;
                    }
                    return o1.getFechacreacion().compareTo(o2.getFechacreacion());
                }
            });
            Collections.reverse(panicoAndSospechas);

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                String temp = format.format(new Date());
                System.out.println("fecha de hoy " + temp);
                Date corte00 = format.parse(temp);
                System.out.println("fecha de hoy " + corte00.toString());
                List<unionPanicoAndSospecha> toDelete = new ArrayList<>();
                for (unionPanicoAndSospecha pas : panicoAndSospechas) {
                    if (pas.getFechacreacion().before(corte00)) {
                        toDelete.add(pas);
                    }
                }
                panicoAndSospechas.removeAll(toDelete);
            } catch (Exception e) {
                System.out.println("Error init " + e.getMessage());
            }
        }
    }

    public void revisarEventos() {
        init();
        if (panicoAndSospechas.get(0).getEventoPanico() != null) {
            addMarker(panicoAndSospechas.get(0).getEventoPanico().getLatitud(), panicoAndSospechas.get(0).getEventoPanico().getLongitud());
        } else {
            addMarker(panicoAndSospechas.get(0).getEventoSospecha().getLatitud(), panicoAndSospechas.get(0).getEventoSospecha().getLongitud());
        }
    }

    public void addMarker(double lat, double lng) {
        advancedModel.getMarkers().clear();
        Marker marker = new Marker(new LatLng(lat, lng), "Evento Aqui");
        advancedModel.addOverlay(marker);
        latitud = lat;
        longitud = lng;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Marker Added", "Lat:" + lat + ", Lng:" + lng));
    }

    public List<unionPanicoAndSospecha> getPanicoAndSospechas() {
        return panicoAndSospechas;
    }

    public void setPanicoAndSospechas(List<unionPanicoAndSospecha> panicoAndSospechas) {
        this.panicoAndSospechas = panicoAndSospechas;
    }

    public unionPanicoAndSospecha getToDialog() {
        return toDialog;
    }

    public void setToDialog(unionPanicoAndSospecha toDialog) {
        this.toDialog = toDialog;
    }

    public MapModel getAdvancedModel() {
        return advancedModel;
    }

    public void setAdvancedModel(MapModel advancedModel) {
        this.advancedModel = advancedModel;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public ConfiguracionGeneral getGeneral() {
        return general;
    }

    public void setGeneral(ConfiguracionGeneral general) {
        this.general = general;
    }

}
