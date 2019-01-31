/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author wilme
 */
@Entity
public class Zona extends CamposComunesdeEntidad implements Serializable {

    @ManyToOne
    private Barrio barrio;
    private String Nombre;
    private Double latitud;
    private Double longitud;
    private Long rangoResguardado;
    private Long numeroTelefono;

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
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

    public Long getRangoResguardado() {
        return rangoResguardado;
    }

    public void setRangoResguardado(Long rangoResguardado) {
        this.rangoResguardado = rangoResguardado;
    }

    public Long getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(Long numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

}
