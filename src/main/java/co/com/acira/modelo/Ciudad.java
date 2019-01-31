/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author wilme
 */
@Entity
public class Ciudad extends CamposComunesdeEntidad implements Serializable {

    private Long codigo;
    private String nombre;
    private Double centroLatitud;
    private Double centroLongitud;
    private Integer nivelZoom;

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getCentroLatitud() {
        return centroLatitud;
    }

    public void setCentroLatitud(Double centroLatitud) {
        this.centroLatitud = centroLatitud;
    }

    public Double getCentroLongitud() {
        return centroLongitud;
    }

    public void setCentroLongitud(Double centroLongitud) {
        this.centroLongitud = centroLongitud;
    }

    public Integer getNivelZoom() {
        return nivelZoom;
    }

    public void setNivelZoom(Integer nivelZoom) {
        this.nivelZoom = nivelZoom;
    }

}
