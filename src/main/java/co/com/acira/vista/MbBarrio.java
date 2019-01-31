/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.vista;

import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Barrio;
import co.com.acira.modelo.Ciudad;
import co.com.acira.modelo.LimitesXBarrio;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polygon;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbBarrio")
public class MbBarrio implements Serializable {

    @EJB
    private CommonsBean cb;
    private List<SelectItem> listCiudadesActivas;
    private List<Barrio> barrios;
    private Long idCiudad;
    private Barrio barrio;

    private MapModel advancedModel;

    private Boolean bloqueo;
    private List<LimitesXBarrio> listaCordenadaDeLaZona;
    private Marker marker;

    private Double centroMapLat;
    private Double centroMapLong;
    private Long zoomMap;

    public MbBarrio() {
    }

    @PostConstruct
    public void init() {
        barrios = new ArrayList<>();
        barrios = cb.getAll(Barrio.class);
        barrio = new Barrio();
        listCiudadesActivas = new LinkedList<>();
        List<Ciudad> temp = (List<Ciudad>) cb.getByOneField(Ciudad.class, "estado", Boolean.TRUE);
        try {
            System.out.println("temp " + temp.size());
        } catch (Exception e) {
        }
        for (Ciudad c : temp) {
            listCiudadesActivas.add(new SelectItem(c.getId(), c.getNombre()));
        }

        advancedModel = new DefaultMapModel();

        listaCordenadaDeLaZona = new ArrayList<>();
        bloqueo = Boolean.FALSE;

        centroMapLat = 11.247141;
        centroMapLong = -74.205504;
        zoomMap = 14L;
    }

    public void accionGuardarBarrio() {
        if (VerificarFormulario()) {
            try {
                if (cb.guardar(barrio)) {
                    for (LimitesXBarrio cdlz : listaCordenadaDeLaZona) {
                        cdlz.setBarrio(barrio);
                        cb.guardar(cdlz);
                    }
                    mostrarMensaje(FacesMessage.SEVERITY_INFO, "Exitoso", "Se ha guardado");
                    init();
                }
            } catch (Exception e) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "No se ha guardado");
            }

        }
    }

    public void accionResetBarrio() {
        this.barrio = new Barrio();
        advancedModel = new DefaultMapModel();
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }

    public void agragarMarker() {
        bloqueo = Boolean.TRUE;
        if (!listaCordenadaDeLaZona.isEmpty()) {
            advancedModel = new DefaultMapModel();
            for (LimitesXBarrio cdlz : listaCordenadaDeLaZona) {
                advancedModel.addOverlay(cdlz.getMarker());
            }
        } else {
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Informacion", "Mueva el punto en el mapa");
        }
        LatLng coord1 = new LatLng(11.247141, -74.205504);
        if (!listaCordenadaDeLaZona.isEmpty()) {
            zoomMap = 17L;
            coord1 = new LatLng(listaCordenadaDeLaZona.get(listaCordenadaDeLaZona.size() - 1).getLatitud() + 0.001, listaCordenadaDeLaZona.get(listaCordenadaDeLaZona.size() - 1).getLongitud());
        }

        int numero = listaCordenadaDeLaZona.size() + 1;
//        centroMap = coord1;
        centroMapLat = coord1.getLat();
        centroMapLong = coord1.getLng();

        advancedModel.addOverlay(new Marker(coord1, numero + ""));
        for (Marker premarker : getAdvancedModel().getMarkers()) {
            premarker.setDraggable(true);
        }
    }

    public void dibujar() {
        if (listaCordenadaDeLaZona.size() > 1) {
            advancedModel = new DefaultMapModel();
            Polygon polygon = new Polygon();
            //Polygon
            for (LimitesXBarrio cdlz : listaCordenadaDeLaZona) {
//                System.out.println(cdlz.getOrden());
                polygon.getPaths().add(cdlz.getMarker().getLatlng());
            }
            polygon.setStrokeColor("#FF0000");
            polygon.setFillColor("#FF0000");
            polygon.setStrokeOpacity(0.7);
            polygon.setFillOpacity(0.5);
            advancedModel.addOverlay(polygon);
        } else {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Tiene que agregar mas de un punto");
        }
    }

    public void resetMap() {
        advancedModel = new DefaultMapModel();
        listaCordenadaDeLaZona = new ArrayList<>();
    }

    public void onMarkerDrag(MarkerDragEvent event) {
        marker = event.getMarker();
        bloqueo = Boolean.FALSE;
        if (listaCordenadaDeLaZona.isEmpty()) {
            LimitesXBarrio cdlz = new LimitesXBarrio();
            cdlz.setMarker(marker);
            cdlz.setLatitud(marker.getLatlng().getLat());
            cdlz.setLongitud(marker.getLatlng().getLng());
            cdlz.setOrden(1);
            listaCordenadaDeLaZona.add(cdlz);
        } else {
            Boolean entro = Boolean.FALSE;
            for (LimitesXBarrio cordenadaDeLaZona : listaCordenadaDeLaZona) {
                if (cordenadaDeLaZona.getMarker().getId().equals(marker.getId())) {
                    entro = Boolean.TRUE;
                    cordenadaDeLaZona.setMarker(marker);
                    cordenadaDeLaZona.setLatitud(marker.getLatlng().getLat());
                    cordenadaDeLaZona.setLongitud(marker.getLatlng().getLng());
                    break;
                }
            }
            if (!entro) {
                LimitesXBarrio cdlz = new LimitesXBarrio();
                cdlz.setMarker(marker);
                cdlz.setLatitud(marker.getLatlng().getLat());
                cdlz.setLongitud(marker.getLatlng().getLng());
                cdlz.setOrden(listaCordenadaDeLaZona.size() + 1);
                listaCordenadaDeLaZona.add(cdlz);
            }
        }
    }

    public Boolean VerificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (barrio.getNombre().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue nombre del barrio");
        } else {
            try {
                Barrio u = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "nombre", barrio.getNombre());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Nombre de barrio ya existe");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbCiudad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (barrio.getCodigo() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue codigo del barrio");
        } else {
            int temp = (int) Math.log10(barrio.getCodigo()) + 1;
            System.out.println("digitos del cogido del barrio " + temp);
            if (temp < 2) {
                resultado = Boolean.FALSE;
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "el codigo del barrio debe tener minimo 2 digitos");
            } else {
                try {
                    Barrio u = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "codigo", barrio.getCodigo());
                    if (u != null) {
                        resultado = Boolean.FALSE;
                        mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "codigo de barrio ya existe");
                    }
                } catch (Exception ex) {
                    resultado = Boolean.FALSE;
                    Logger.getLogger(MbBarrio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (idCiudad != null) {
            try {
                Ciudad u = (Ciudad) cb.getByOneFieldWithOneResult(Ciudad.class, "id", idCiudad);
                barrio.setCiudad(u);
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbBarrio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Selecione una ciudad de la lista");
        }

        if (barrio.getNumeroTelefono() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue numero del Barrio");
        } else {
            int temp = (int) Math.log10(barrio.getCodigo()) + 1;
            System.out.println("digitos del cogido del barrio " + temp);
            try {
                Barrio u = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "numeroTelefono", barrio.getNumeroTelefono());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "ya existe un barrio con el mismo numero de telefono");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbBarrio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (listaCordenadaDeLaZona.isEmpty()) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Dibuje la zona del barrio");
        }
        return resultado;
    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    public void accionCargarBarrio(Barrio b) {
        this.barrio = b;
        idCiudad = barrio.getCiudad().getCodigo();
        advancedModel = new DefaultMapModel();
        listaCordenadaDeLaZona = new ArrayList<>();
        listaCordenadaDeLaZona = (List<LimitesXBarrio>) cb.getByOneField(LimitesXBarrio.class, "barrio", barrio);
        for (LimitesXBarrio lxb : listaCordenadaDeLaZona) {
            lxb.setMarker(new Marker(new LatLng(lxb.getLatitud(), lxb.getLongitud())));
        }
        dibujar();
    }

    public List<SelectItem> getListCiudadesActivas() {
        return listCiudadesActivas;
    }

    public void setListCiudadesActivas(List<SelectItem> listCiudadesActivas) {
        this.listCiudadesActivas = listCiudadesActivas;
    }

    public Long getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(Long idCiudad) {
        this.idCiudad = idCiudad;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public List<Barrio> getBarrios() {
        return barrios;
    }

    public void setBarrios(List<Barrio> barrios) {
        this.barrios = barrios;
    }

    public MapModel getAdvancedModel() {
        return advancedModel;
    }

    public void setAdvancedModel(MapModel advancedModel) {
        this.advancedModel = advancedModel;
    }

    public Boolean getBloqueo() {
        return bloqueo;
    }

    public void setBloqueo(Boolean bloqueo) {
        this.bloqueo = bloqueo;
    }

    public Double getCentroMapLat() {
        return centroMapLat;
    }

    public void setCentroMapLat(Double centroMapLat) {
        this.centroMapLat = centroMapLat;
    }

    public Double getCentroMapLong() {
        return centroMapLong;
    }

    public void setCentroMapLong(Double centroMapLong) {
        this.centroMapLong = centroMapLong;
    }

    public Long getZoomMap() {
        return zoomMap;
    }

    public void setZoomMap(Long zoomMap) {
        this.zoomMap = zoomMap;
    }

}
