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
import co.com.acira.modelo.Zona;
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
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polygon;
import org.primefaces.model.map.Polyline;

/**
 *
 * @author wilme
 */
@ViewScoped
@ManagedBean(name = "MbZonas")
public class MbZonas implements Serializable {

    @EJB
    private CommonsBean cb;
    private List<SelectItem> listBarriosActivos;
    private Long idBarrio;

    private List<Zona> zonas;
    private Zona zona;

    private List<LimitesXBarrio> listaCordenadaDeLaZona;

    private MapModel advancedModel;
    private Circle circuloActual;
    private double lat;
    private double lng;
    private Long radius;
    private Boolean popup;

    private Double centerMapLat;
    private Double centerMapLon;

    public MbZonas() {
    }

    @PostConstruct
    public void init() {
        idBarrio = null;
        listBarriosActivos = new LinkedList<>();
        List<Barrio> temp = (List<Barrio>) cb.getByOneField(Barrio.class, "estado", Boolean.TRUE);
        try {
            System.out.println("temp " + temp.size());
        } catch (Exception e) {
        }
        for (Barrio b : temp) {
            listBarriosActivos.add(new SelectItem(b.getId(), b.getNombre()));
        }

        zonas = new ArrayList<>();
        zonas = cb.getAll(Zona.class);
        zona = new Zona();

        advancedModel = new DefaultMapModel();
        popup = Boolean.FALSE;

        centerMapLat = 11.2308168;
        centerMapLon = -74.1838902;

//        if (!barrios.isEmpty()) {
//            for (Barrio b : barrios) {
//                Circle tempCircle = new Circle(new LatLng(b.getLatitud(), b.getLongitud()), b.getRangoResguardado());
//                tempCircle.setStrokeColor("#37ff00");
//                tempCircle.setStrokeOpacity(0.8);
//                tempCircle.setStrokeWeight(2);
//                tempCircle.setFillColor("#37ff00");
//                tempCircle.setFillOpacity(0.35);
//
//                Marker marker = new Marker(new LatLng(b.getLatitud(), b.getLongitud()), b.getNombre());
//
//                advancedModel.addOverlay(tempCircle);
//                advancedModel.addOverlay(marker);
//            }
//
//        }
    }

    public void accionGuardarZona() {
        if (VerificarFormulario()) {
            if (cb.guardar(zona)) {
                mostrarMensaje(FacesMessage.SEVERITY_INFO, "Exitoso", "Se ha guardado la zona");
                init();
            } else {
                mostrarMensaje(FacesMessage.SEVERITY_FATAL, "Error", "Ha fallado al guardar la zona");
            }
        }
    }

    public Boolean VerificarFormulario() {
        Boolean resultado = Boolean.TRUE;
        if (zona.getNombre().trim().length() == 0) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue nombre de zona ");
        } else {
            try {
                Zona u = (Zona) cb.getByOneFieldWithOneResult(Zona.class, "nombre", zona.getNombre());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Nombre de zona ya existe");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbCiudad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (idBarrio != null) {
            try {
                Barrio u = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "id", idBarrio);
                zona.setBarrio(u);
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbBarrio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Selecione un barrio de la lista");
        }

        if (zona.getNumeroTelefono() == null) {
            resultado = Boolean.FALSE;
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Agregue numero de zona");
        } else {
            int temp = (int) Math.log10(zona.getNumeroTelefono()) + 1;
            System.out.println("digitos del cogido de la zona " + temp);
            try {
                Zona u = (Zona) cb.getByOneFieldWithOneResult(Zona.class, "numeroTelefono", zona.getNumeroTelefono());
                if (u != null) {
                    resultado = Boolean.FALSE;
                    mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "ya existe una zona con el mismo numero de telefono");
                }
            } catch (Exception ex) {
                resultado = Boolean.FALSE;
                Logger.getLogger(MbBarrio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resultado;
    }

    public void accionResetZona() {
        this.zona = new Zona();
    }

    public void listenerMenu() {
        if (idBarrio != null) {
            try {
                Barrio b = (Barrio) cb.getByOneFieldWithOneResult(Barrio.class, "id", idBarrio);
                if (b != null) {
                    advancedModel = new DefaultMapModel();
                    listaCordenadaDeLaZona = new ArrayList<>();
                    listaCordenadaDeLaZona = (List<LimitesXBarrio>) cb.getByOneField(LimitesXBarrio.class, "barrio", b);
                    for (LimitesXBarrio lxb : listaCordenadaDeLaZona) {
                        lxb.setMarker(new Marker(new LatLng(lxb.getLatitud(), lxb.getLongitud())));
                    }
                    dibujar2();
                }
            } catch (Exception ex) {
                Logger.getLogger(MbZonas.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            polygon.setStrokeColor("#74e36a");
            polygon.setFillColor("#74e36a");
            polygon.setStrokeOpacity(0.7);
            polygon.setFillOpacity(0.5);
            advancedModel.addOverlay(polygon);
        } else {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Tiene que agregar mas de un punto");
        }
    }

    public void dibujar2() {
        if (listaCordenadaDeLaZona.size() > 2) {
            advancedModel = new DefaultMapModel();
            Polyline polyline = new Polyline();
            for (LimitesXBarrio cdlz : listaCordenadaDeLaZona) {
                polyline.getPaths().add(cdlz.getMarker().getLatlng());
            }
            polyline.getPaths().add(listaCordenadaDeLaZona.get(0).getMarker().getLatlng());
            centerMapLat = listaCordenadaDeLaZona.get(0).getMarker().getLatlng().getLat();
            centerMapLon = listaCordenadaDeLaZona.get(0).getMarker().getLatlng().getLng();
            polyline.setStrokeColor("#74e36a");
            polyline.setStrokeOpacity(0.7);
            polyline.setStrokeWeight(5);
            advancedModel.addOverlay(polyline);
        } else {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "ERROR", "Tiene que agregar mas de un punto");
        }
    }

    public void addCircle() {
        if (zona.getLatitud() == null && zona.getLongitud() == null) {
            System.out.println("add circle - if ");
            circuloActual = new Circle(new LatLng(lat, lng), radius);
            System.out.println("radio en addcricle " + radius);
            advancedModel.addOverlay(circuloActual);

            zona.setRangoResguardado(radius);
            zona.setLatitud(lat);
            zona.setLongitud(lng);

        } else {
            System.out.println("add circle - else");
            advancedModel = new DefaultMapModel();
            circuloActual = new Circle(new LatLng(lat, lng), radius);
            System.out.println("radio en addcricle " + radius);
            advancedModel.addOverlay(circuloActual);

            zona.setRangoResguardado(radius);
            zona.setLatitud(lat);
            zona.setLongitud(lng);
        }
    }

    public void accionCargarBarrio(Zona z) {
        this.zona = z;
        idBarrio = zona.getBarrio().getId();
        listenerMenu();
        circuloActual = new Circle(new LatLng(zona.getLatitud(), zona.getLongitud()), zona.getRangoResguardado());
        circuloActual.setStrokeColor("#FF0000");
        circuloActual.setStrokeOpacity(0.8);
        circuloActual.setFillColor("#4EE960");
        circuloActual.setFillOpacity(0.35);
        System.out.println("radio en addcricle " + radius);
        advancedModel.addOverlay(circuloActual);

    }

    public void mostrarMensaje(FacesMessage.Severity icono, String titulo, String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(icono, titulo, mensaje));
    }

    public Long getIdBarrio() {
        return idBarrio;
    }

    public void setIdBarrio(Long idBarrio) {
        this.idBarrio = idBarrio;
    }

    public List<SelectItem> getListBarriosActivos() {
        return listBarriosActivos;
    }

    public void setListBarriosActivos(List<SelectItem> listBarriosActivos) {
        this.listBarriosActivos = listBarriosActivos;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Long getRadius() {
        return radius;
    }

    public void setRadius(Long radius) {
        this.radius = radius;
    }

    public MapModel getAdvancedModel() {
        return advancedModel;
    }

    public void setAdvancedModel(MapModel advancedModel) {
        this.advancedModel = advancedModel;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public List<Zona> getZonas() {
        return zonas;
    }

    public void setZonas(List<Zona> zonas) {
        this.zonas = zonas;
    }

    public Double getCenterMapLat() {
        return centerMapLat;
    }

    public void setCenterMapLat(Double centerMapLat) {
        this.centerMapLat = centerMapLat;
    }

    public Double getCenterMapLon() {
        return centerMapLon;
    }

    public void setCenterMapLon(Double centerMapLon) {
        this.centerMapLon = centerMapLon;
    }

}
